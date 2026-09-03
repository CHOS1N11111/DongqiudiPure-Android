package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchAnalysis
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineupBundle
import io.github.chos1n11111.dongqiudipure.core.model.MatchOverview
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHeatMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPage
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShotMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticsData
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingSection
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamScheduleData
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadGroup
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@Singleton
class DefaultFootballRepository @Inject constructor(
    private val remote: FootballRemoteDataSource,
) : MatchRepository, StandingsRepository, FootballCatalogRepository, FootballEntityRepository {

    override val importantCompetitions: List<CompetitionRef> = IMPORTANT_COMPETITIONS
    override val defaultRankingCompetitions: List<CompetitionRef> = DEFAULT_RANKING_COMPETITIONS
    override val defaultCompetitions: List<CompetitionRef> = DEFAULT_RANKING_COMPETITIONS

    private val cachedMatches = mutableMapOf<MatchId, MatchSummary>()
    private val cachedSeasons = mutableMapOf<CompetitionId, CurrentSeason?>()
    private val cachedSeasonOptions = mutableMapOf<CompetitionId, List<SeasonOption>>()
    private val cachedCompetitionSchedules = mutableMapOf<SeasonId, List<DatedMatch>>()
    private var cachedCatalog: List<CompetitionCatalogGroup>? = null

    override suspend fun loadCompetitionCatalog(): DataResult<List<CompetitionCatalogGroup>> {
        cachedCatalog?.let { return DataResult.Success(it) }
        return when (val result = remote.loadCompetitionCatalog()) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(CATALOG_ENDPOINT) {
                result.value.toDomain().also { cachedCatalog = it }
            }
        }
    }

    override suspend fun loadMatches(
        date: LocalDate,
        competition: CompetitionRef?,
    ): DataResult<List<MatchSummary>> {
        if (competition != null) return loadCompetitionMatches(date, competition)
        return when (val result = loadMatchWindow(date)) {
            is DataResult.Failure -> result
            is DataResult.Success -> DataResult.Success(
                result.value
                    .asSequence()
                    .filter { it.date == date && it.match.competition.id.raw in IMPORTANT_IDS }
                    .map { it.match }
                    .toList(),
            )
        }
    }

    override suspend fun loadMatch(matchId: MatchId): DataResult<MatchSummary?> {
        cachedMatches[matchId]?.let { return DataResult.Success(it) }
        return when (val result = remote.loadMatchDetail(matchId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(MATCH_DETAIL_ENDPOINT) {
                result.value.matchSample?.toDomain(ZoneId.systemDefault())?.match?.also {
                    cachedMatches[it.id] = it
                }
            }
        }
    }

    override suspend fun loadMatchOverview(matchId: MatchId): DataResult<MatchOverview> = coroutineScope {
        val overviewRequest = async { remote.loadMatchOverview(matchId) }
        val newsRequest = async { remote.loadMatchNews(matchId) }
        when (val result = overviewRequest.await()) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(MATCH_OVERVIEW_ENDPOINT) {
                val relatedNews = when (val news = newsRequest.await()) {
                    is ApiResult.Failure -> emptyList()
                    is ApiResult.Success -> news.value.toDomain()
                }
                result.value.toDomain().copy(relatedNews = relatedNews)
            }
        }
    }

    override suspend fun loadMatchLineup(matchId: MatchId): DataResult<MatchLineupBundle?> =
        when (val result = remote.loadMatchLineup(matchId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(MATCH_LINEUP_ENDPOINT) {
                result.value.toDomain()
            }
        }

    override suspend fun loadMatchUserRatings(
        matchId: MatchId,
        playerIds: List<PlayerId>,
    ): DataResult<Map<PlayerId, String>> = coroutineScope {
        val uniqueIds = playerIds.distinct()
        if (uniqueIds.isEmpty()) return@coroutineScope DataResult.Success(emptyMap())
        val semaphore = Semaphore(USER_RATING_REQUEST_CONCURRENCY)
        val responses = uniqueIds.map { playerId ->
            async {
                semaphore.withPermit {
                    playerId to remote.loadPlayerMatches(playerId, page = 1)
                }
            }
        }.awaitAll()
        val successful = responses.mapNotNull { (playerId, result) ->
            when (result) {
                is ApiResult.Failure -> null
                is ApiResult.Success -> playerId to result.value
            }
        }
        if (successful.isEmpty()) {
            val error = responses.firstNotNullOf { (_, result) ->
                when (result) {
                    is ApiResult.Failure -> result.error
                    is ApiResult.Success -> null
                }
            }
            return@coroutineScope DataResult.Failure(error)
        }
        DataResult.Success(
            successful.mapNotNull { (playerId, matches) ->
                matches.userRatingFor(matchId)?.let { playerId to it }
            }.toMap(),
        )
    }

    override suspend fun loadMatchAnalysis(matchId: MatchId): DataResult<MatchAnalysis> =
        when (val result = remote.loadMatchAnalysis(matchId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(MATCH_ANALYSIS_ENDPOINT) {
                result.value.toDomain()
            }
        }

    override suspend fun loadSeasons(
        competitionId: CompetitionId,
    ): DataResult<List<SeasonOption>> {
        cachedSeasonOptions[competitionId]?.let { return DataResult.Success(it) }
        return when (val result = remote.loadSeasons(competitionId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(SEASONS_ENDPOINT) {
                result.value.mapIndexedNotNull { index, season ->
                    val id = season.seasonId.scalarFootball() ?: return@mapIndexedNotNull null
                    val name = season.seasonName.requiredRepositoryValue()
                    SeasonOption(id, name, index == 0)
                }.also { options ->
                    cachedSeasonOptions[competitionId] = options
                    cachedSeasons[competitionId] = options.firstOrNull()?.let {
                        CurrentSeason(SeasonId(it.id), it.label)
                    }
                }
            }
        }
    }

    override suspend fun loadStandings(
        competition: CompetitionRef,
        seasonId: SeasonId?,
    ): DataResult<StandingTable?> {
        val season = when (val result = resolveSeason(competition.id, seasonId)) {
            is DataResult.Failure -> return result
            is DataResult.Success -> result.value ?: return DataResult.Success(null)
        }
        return when (val result = remote.loadStandings(season.id)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(STANDINGS_ENDPOINT) {
                result.value.toDomain(competition, season.name).takeIf {
                    it.rows.isNotEmpty() || it.groups.isNotEmpty() ||
                        it.knockoutStages.isNotEmpty() || it.matchStages.isNotEmpty()
                }
            }
        }
    }

    override suspend fun loadRankingMetrics(
        competitionId: CompetitionId,
        section: RankingSection,
        seasonId: SeasonId?,
    ): DataResult<List<RankingMetric>> {
        val entity = section.entityName() ?: return DataResult.Success(emptyList())
        val season = when (val result = resolveSeason(competitionId, seasonId)) {
            is DataResult.Failure -> return result
            is DataResult.Success -> result.value ?: return DataResult.Success(emptyList())
        }
        return when (val result = remote.loadRankingTypes(season.id, entity)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(RANKING_TYPES_ENDPOINT) { result.value.toDomain() }
        }
    }

    override suspend fun loadRanking(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric,
        seasonId: SeasonId?,
    ): DataResult<StatisticRankingTable?> {
        val entity = section.entityName() ?: return DataResult.Success(null)
        val season = when (val result = resolveSeason(competition.id, seasonId)) {
            is DataResult.Failure -> return result
            is DataResult.Success -> result.value ?: return DataResult.Success(null)
        }
        return when (val result = remote.loadRanking(season.id, entity, metric.id)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(RANKING_ENDPOINT) {
                result.value.toDomain(competition, season.name, entity)
                    .takeIf { it.rows.isNotEmpty() }
            }
        }
    }

    override suspend fun loadTeamProfile(teamId: TeamId): DataResult<TeamProfile?> =
        when (val result = remote.loadTeamDetail(teamId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(TEAM_DETAIL_ENDPOINT) { result.value.toDomain() }
        }

    override suspend fun loadTeamStatistics(
        teamId: TeamId,
        seasonId: String?,
    ): DataResult<TeamStatistics?> =
        when (val result = remote.loadTeamStatistics(teamId, seasonId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(TEAM_STATISTICS_ENDPOINT) {
                result.value.toDomain().let { statistics ->
                    statistics.copy(
                        selectedSeasonId = seasonId
                            ?: statistics.seasons.firstOrNull { it.isCurrent }?.id
                            ?: statistics.seasons.firstOrNull()?.id,
                    )
                }
            }
        }

    override suspend fun loadTeamSquad(teamId: TeamId): DataResult<List<TeamSquadGroup>> =
        when (val result = remote.loadTeamMembers(teamId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(TEAM_MEMBERS_ENDPOINT) { result.value.toDomain() }
        }

    override suspend fun loadTeamSchedule(
        teamId: TeamId,
        seasonId: String?,
    ): DataResult<TeamScheduleData> =
        when (val result = remote.loadTeamSchedule(teamId, seasonId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(TEAM_SCHEDULE_ENDPOINT) {
                result.value.toDomain(ZoneId.systemDefault(), seasonId).also {
                    cacheMatches(it.matches)
                }
            }
        }

    override suspend fun loadTeamTransfers(
        teamId: TeamId,
        windowId: String?,
    ): DataResult<TeamTransferData> = when (val result = remote.loadTeamTransfers(teamId, windowId)) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(TEAM_TRANSFERS_ENDPOINT) {
            result.value.toDomain(windowId)
        }
    }

    override suspend fun loadTeamNews(teamId: TeamId): DataResult<List<ArticleSummary>> =
        loadEntityNews(teamId.raw, "team")

    override suspend fun loadPlayerOverview(playerId: PlayerId): DataResult<PlayerOverview?> =
        when (val result = remote.loadPlayerDetail(playerId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(PLAYER_DETAIL_ENDPOINT) { result.value.toDomain() }
        }

    override suspend fun loadPlayerCareer(playerId: PlayerId): DataResult<List<CareerEntry>> =
        when (val result = remote.loadPlayerStatistics(playerId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(PLAYER_STATISTICS_ENDPOINT) {
                result.value.toCareerEntries()
            }
        }

    override suspend fun loadPlayerStatistics(
        playerId: PlayerId,
    ): DataResult<PlayerStatisticsData> = when (val result = remote.loadPlayerStatistics(playerId)) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(PLAYER_STATISTICS_ENDPOINT) { result.value.toDomain() }
    }

    override suspend fun loadPlayerMatches(
        playerId: PlayerId,
        page: Int,
    ): DataResult<PlayerMatchPage> = when (val result = remote.loadPlayerMatches(playerId, page)) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(PLAYER_MATCHES_ENDPOINT) {
            result.value.toDomain(ZoneId.systemDefault()).also { mapped ->
                cacheMatches(mapped.matches.map { it.match })
            }
        }
    }

    override suspend fun loadPlayerNews(playerId: PlayerId): DataResult<List<ArticleSummary>> =
        loadEntityNews(playerId.raw, "player")

    override suspend fun loadPlayerHeatMap(
        playerId: PlayerId,
        seasonId: String,
        teamId: TeamId,
    ): DataResult<PlayerHeatMap> = when (
        val result = remote.loadPlayerHeatMap(playerId, seasonId, teamId)
    ) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(PLAYER_HEAT_MAP_ENDPOINT) { result.value.toDomain() }
    }

    override suspend fun loadPlayerShotMap(
        playerId: PlayerId,
        matchId: MatchId,
    ): DataResult<PlayerShotMap> = when (val result = remote.loadPlayerShotMap(playerId, matchId)) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(PLAYER_SHOT_MAP_ENDPOINT) {
            result.value.toDomain(matchId)
        }
    }

    override suspend fun loadPlayerAbility(playerId: PlayerId): DataResult<PlayerAbility?> =
        when (val result = remote.loadPlayerAbility(playerId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> mapContract(PLAYER_ABILITY_ENDPOINT) { result.value.toDomain() }
        }

    private suspend fun currentSeason(competitionId: CompetitionId): DataResult<CurrentSeason?> {
        if (cachedSeasons.containsKey(competitionId)) {
            return DataResult.Success(cachedSeasons[competitionId])
        }

        return when (val result = loadSeasons(competitionId)) {
            is DataResult.Failure -> result
            is DataResult.Success -> DataResult.Success(
                result.value.firstOrNull()?.let { CurrentSeason(SeasonId(it.id), it.label) },
            )
        }
    }

    private suspend fun loadEntityNews(
        entityId: String,
        type: String,
    ): DataResult<List<ArticleSummary>> = when (val result = remote.loadEntityFeed(entityId, type)) {
        is ApiResult.Failure -> DataResult.Failure(result.error)
        is ApiResult.Success -> mapContract(ENTITY_FEED_ENDPOINT) {
            if (result.value.code.scalarFootball() != "0") throw ContractViolation()
            val feed = result.value.data ?: throw ContractViolation()
            feed.flattenedArticles().mapNotNull { article ->
                runCatching { article.toDomain() }.getOrNull()
            }
        }
    }

    private suspend fun resolveSeason(
        competitionId: CompetitionId,
        requested: SeasonId?,
    ): DataResult<CurrentSeason?> {
        if (requested == null) return currentSeason(competitionId)
        return when (val result = loadSeasons(competitionId)) {
            is DataResult.Failure -> result
            is DataResult.Success -> DataResult.Success(
                result.value.firstOrNull { it.id == requested.raw }
                    ?.let { CurrentSeason(requested, it.label) },
            )
        }
    }

    private suspend fun loadCompetitionMatches(
        date: LocalDate,
        competition: CompetitionRef,
    ): DataResult<List<MatchSummary>> {
        val season = when (val result = currentSeason(competition.id)) {
            is DataResult.Failure -> return result
            is DataResult.Success -> result.value ?: return DataResult.Success(emptyList())
        }
        val datedMatches = cachedCompetitionSchedules[season.id] ?: when (
            val result = remote.loadCompetitionSchedule(season.id)
        ) {
            is ApiResult.Failure -> return DataResult.Failure(result.error)
            is ApiResult.Success -> when (
                val mapped = mapContract(COMPETITION_SCHEDULE_ENDPOINT) {
                    result.value.toDomain(competition, ZoneId.systemDefault())
                }
            ) {
                is DataResult.Failure -> return mapped
                is DataResult.Success -> mapped.value.also {
                    cachedCompetitionSchedules[season.id] = it
                    cacheMatches(it.map(DatedMatch::match))
                }
            }
        }
        return DataResult.Success(
            datedMatches.asSequence()
                .filter {
                    it.date == date && it.match.competition.id == competition.id
                }
                .map(DatedMatch::match)
                .toList(),
        )
    }

    private suspend fun loadMatchWindow(date: LocalDate): DataResult<List<DatedMatch>> {
        val first = remote.loadImportantMatches(date.minusDays(1))
        val second = remote.loadImportantMatches(date)
        if (first is ApiResult.Failure && second is ApiResult.Failure) {
            return DataResult.Failure(first.error)
        }
        return try {
            val matches = listOf(first, second)
                .mapNotNull { result ->
                    when (result) {
                        is ApiResult.Failure -> null
                        is ApiResult.Success -> result.value
                    }
                }
                .flatMap { it.list ?: throw ContractViolation() }
                .asSequence()
                .filter { it.relateType == "match" && it.competitionType == "soccer" }
                .map { it.toDomain(ZoneId.systemDefault()) }
                .distinctBy { it.match.id }
                .sortedBy { it.kickoff }
                .toList()
            cacheMatches(matches.map { it.match })
            DataResult.Success(matches)
        } catch (_: ContractViolation) {
            DataResult.Failure(AppError.UnsupportedContract(MATCHES_ENDPOINT))
        }
    }

    private fun cacheMatches(matches: List<MatchSummary>) {
        matches.forEach { cachedMatches[it.id] = it }
    }

    private inline fun <T> mapContract(endpoint: EndpointId, block: () -> T): DataResult<T> =
        try {
            DataResult.Success(block())
        } catch (_: ContractViolation) {
            DataResult.Failure(AppError.UnsupportedContract(endpoint))
        }

    private fun RankingSection.entityName(): String? = when (this) {
        RankingSection.Standings -> null
        RankingSection.Players -> "person"
        RankingSection.Teams -> "team"
    }

    private fun String?.requiredRepositoryValue(): String =
        this?.trim()?.takeIf(String::isNotEmpty) ?: throw ContractViolation()

    private data class CurrentSeason(val id: SeasonId, val name: String)

    private companion object {
        val MATCHES_ENDPOINT = EndpointId("football.matches")
        val MATCH_DETAIL_ENDPOINT = EndpointId("football.match-detail")
        val MATCH_OVERVIEW_ENDPOINT = EndpointId("football.match-overview")
        val MATCH_LINEUP_ENDPOINT = EndpointId("football.match-lineup")
        val MATCH_ANALYSIS_ENDPOINT = EndpointId("football.match-analysis")
        val CATALOG_ENDPOINT = EndpointId("football.catalog")
        val SEASONS_ENDPOINT = EndpointId("football.seasons")
        val STANDINGS_ENDPOINT = EndpointId("football.standings")
        val COMPETITION_SCHEDULE_ENDPOINT = EndpointId("football.competition-schedule")
        val RANKING_TYPES_ENDPOINT = EndpointId("football.ranking-types")
        val RANKING_ENDPOINT = EndpointId("football.ranking")
        val TEAM_DETAIL_ENDPOINT = EndpointId("football.team-detail")
        val TEAM_STATISTICS_ENDPOINT = EndpointId("football.team-statistics")
        val TEAM_MEMBERS_ENDPOINT = EndpointId("football.team-members")
        val TEAM_SCHEDULE_ENDPOINT = EndpointId("football.team-schedule")
        val TEAM_TRANSFERS_ENDPOINT = EndpointId("football.team-transfers")
        val ENTITY_FEED_ENDPOINT = EndpointId("football.entity-feed")
        val PLAYER_DETAIL_ENDPOINT = EndpointId("football.player-detail")
        val PLAYER_STATISTICS_ENDPOINT = EndpointId("football.player-statistics")
        val PLAYER_MATCHES_ENDPOINT = EndpointId("football.player-matches")
        val PLAYER_HEAT_MAP_ENDPOINT = EndpointId("football.player-heat-map")
        val PLAYER_SHOT_MAP_ENDPOINT = EndpointId("football.player-shot-map")
        val PLAYER_ABILITY_ENDPOINT = EndpointId("football.player-ability")
        const val USER_RATING_REQUEST_CONCURRENCY = 6

        val DEFAULT_RANKING_COMPETITIONS = listOf(
            CompetitionRef(CompetitionId("4"), "英超", null),
            CompetitionRef(CompetitionId("3"), "西甲", null),
            CompetitionRef(CompetitionId("9"), "意甲", null),
            CompetitionRef(CompetitionId("5"), "德甲", null),
            CompetitionRef(CompetitionId("12"), "法甲", null),
            CompetitionRef(CompetitionId("43"), "中超", null),
        )

        val IMPORTANT_COMPETITIONS = listOf(
            *DEFAULT_RANKING_COMPETITIONS.toTypedArray(),
            CompetitionRef(CompetitionId("82"), "足总杯", null),
            CompetitionRef(CompetitionId("83"), "英联杯", null),
            CompetitionRef(CompetitionId("119"), "国王杯", null),
            CompetitionRef(CompetitionId("116"), "意大利杯", null),
            CompetitionRef(CompetitionId("92"), "德国杯", null),
            CompetitionRef(CompetitionId("158"), "法国杯", null),
            CompetitionRef(CompetitionId("6"), "欧冠", null),
            CompetitionRef(CompetitionId("14"), "欧联", null),
            CompetitionRef(CompetitionId("61"), "世界杯", null),
            CompetitionRef(CompetitionId("225"), "亚洲杯", null),
            CompetitionRef(CompetitionId("18"), "欧洲杯", null),
            CompetitionRef(CompetitionId("262"), "美洲杯", null),
            CompetitionRef(CompetitionId("251"), "中国足协杯", null),
            CompetitionRef(CompetitionId("226"), "亚冠精英", null),
            CompetitionRef(CompetitionId("228"), "亚冠二级", null),
        ).distinctBy { it.id }
        val IMPORTANT_IDS = IMPORTANT_COMPETITIONS.mapTo(mutableSetOf()) { it.id.raw }
    }
}
