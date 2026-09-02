package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFootballRepository @Inject constructor(
    private val remote: FootballRemoteDataSource,
) : MatchRepository, StandingsRepository {

    override val supportedCompetitions: List<CompetitionRef> = SUPPORTED_COMPETITIONS
    private val cachedMatches = mutableMapOf<MatchId, MatchSummary>()

    override suspend fun loadMatches(date: LocalDate): DataResult<List<MatchSummary>> =
        when (val result = loadMatchWindow()) {
            is DataResult.Failure -> result
            is DataResult.Success -> DataResult.Success(
                result.value.filter { it.date == date }.map { it.match },
            )
        }

    override suspend fun loadMatch(matchId: MatchId): DataResult<MatchSummary?> {
        cachedMatches[matchId]?.let { return DataResult.Success(it) }
        return when (val result = loadMatchWindow()) {
            is DataResult.Failure -> result
            is DataResult.Success -> DataResult.Success(
                result.value.firstOrNull { it.match.id == matchId }?.match,
            )
        }
    }

    override suspend fun loadStandings(
        competitionId: CompetitionId,
    ): DataResult<StandingTable?> {
        val competition = supportedCompetitions.firstOrNull { it.id == competitionId }
            ?: return DataResult.Failure(AppError.UnsupportedContract(STANDINGS_ENDPOINT))
        val seasons = when (val result = remote.loadSeasons(competitionId)) {
            is ApiResult.Failure -> return DataResult.Failure(result.error)
            is ApiResult.Success -> result.value
        }
        if (seasons.isEmpty()) return DataResult.Success(null)
        return try {
            val current = seasons.first()
            val seasonId = SeasonId(current.seasonId.scalarFootball().requiredRepositoryValue())
            val seasonName = current.seasonName.requiredRepositoryValue()
            when (val result = remote.loadStandings(seasonId)) {
                is ApiResult.Failure -> DataResult.Failure(result.error)
                is ApiResult.Success -> {
                    val table = result.value.toDomain(competition, seasonName)
                    DataResult.Success(table.takeIf { it.rows.isNotEmpty() })
                }
            }
        } catch (_: ContractViolation) {
            DataResult.Failure(AppError.UnsupportedContract(STANDINGS_ENDPOINT))
        }
    }

    private suspend fun loadMatchWindow(): DataResult<List<DatedMatch>> =
        when (val result = remote.loadImportantMatches()) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> try {
                val supportedIds = supportedCompetitions.mapTo(mutableSetOf()) { it.id.raw }
                val matches = (result.value.list ?: throw ContractViolation())
                    .asSequence()
                    .filter { it.relateType == "match" && it.competitionType == "soccer" }
                    .filter { it.competitionId.scalarFootball() in supportedIds }
                    .map { it.toDomain(ZoneId.systemDefault()) }
                    .sortedBy { it.kickoff }
                    .toList()
                matches.forEach { cachedMatches[it.match.id] = it.match }
                DataResult.Success(matches)
            } catch (_: ContractViolation) {
                DataResult.Failure(AppError.UnsupportedContract(MATCHES_ENDPOINT))
            }
        }

    private fun String?.requiredRepositoryValue(): String =
        this?.trim()?.takeIf(String::isNotEmpty) ?: throw ContractViolation()

    private companion object {
        val MATCHES_ENDPOINT = EndpointId("football.matches")
        val STANDINGS_ENDPOINT = EndpointId("football.standings")
        val SUPPORTED_COMPETITIONS = listOf(
            CompetitionRef(CompetitionId("4"), "英超", null),
            CompetitionRef(CompetitionId("3"), "西甲", null),
            CompetitionRef(CompetitionId("9"), "意甲", null),
            CompetitionRef(CompetitionId("5"), "德甲", null),
            CompetitionRef(CompetitionId("12"), "法甲", null),
            CompetitionRef(CompetitionId("43"), "中超", null),
        )
    }
}
