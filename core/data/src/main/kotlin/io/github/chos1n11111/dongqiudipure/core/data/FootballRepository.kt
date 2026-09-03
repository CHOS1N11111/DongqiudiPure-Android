package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingData
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
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
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamScheduleData
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadData
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface FootballCatalogRepository {
    val importantCompetitions: List<CompetitionRef>
    val defaultRankingCompetitions: List<CompetitionRef>
    suspend fun loadCompetitionCatalog(): DataResult<List<CompetitionCatalogGroup>>
}

interface MatchRepository {
    suspend fun loadMatches(
        date: LocalDate,
        competition: CompetitionRef? = null,
    ): DataResult<List<MatchSummary>>

    suspend fun loadMatch(matchId: MatchId): DataResult<MatchSummary?>
    suspend fun loadMatchOverview(matchId: MatchId): DataResult<MatchOverview>
    suspend fun loadMatchLineup(matchId: MatchId): DataResult<MatchLineupBundle?>
    suspend fun loadMatchUserRatings(
        matchId: MatchId,
        playerIds: List<PlayerId>,
    ): DataResult<Map<PlayerId, String>>
    suspend fun loadMatchAnalysis(matchId: MatchId): DataResult<MatchAnalysis>
}

interface StandingsRepository {
    val defaultCompetitions: List<CompetitionRef>
    suspend fun loadSeasons(competitionId: CompetitionId): DataResult<List<SeasonOption>>
    suspend fun loadStandings(
        competition: CompetitionRef,
        seasonId: SeasonId? = null,
    ): DataResult<StandingTable?>
    suspend fun loadRankingMetrics(
        competitionId: CompetitionId,
        section: RankingSection,
        seasonId: SeasonId? = null,
    ): DataResult<List<RankingMetric>>

    suspend fun loadRanking(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric,
        seasonId: SeasonId? = null,
    ): DataResult<StatisticRankingTable?>
}

interface FootballEntityRepository {
    suspend fun loadTeamProfile(teamId: TeamId): DataResult<TeamProfile?>
    suspend fun loadTeamStatistics(
        teamId: TeamId,
        seasonId: String? = null,
    ): DataResult<TeamStatistics?>
    suspend fun loadTeamSquad(
        teamId: TeamId,
        seasonId: String? = null,
    ): DataResult<TeamSquadData>
    suspend fun loadTeamSchedule(
        teamId: TeamId,
        seasonId: String? = null,
    ): DataResult<TeamScheduleData>
    suspend fun loadTeamTransfers(
        teamId: TeamId,
        windowId: String? = null,
    ): DataResult<TeamTransferData>
    fun pagedTeamNews(teamId: TeamId): Flow<PagingData<ArticleSummary>>
    suspend fun loadPlayerOverview(playerId: PlayerId): DataResult<PlayerOverview?>
    suspend fun loadPlayerCareer(playerId: PlayerId): DataResult<List<CareerEntry>>
    suspend fun loadPlayerStatistics(playerId: PlayerId): DataResult<PlayerStatisticsData>
    suspend fun loadPlayerMatches(playerId: PlayerId, page: Int): DataResult<PlayerMatchPage>
    fun pagedPlayerNews(playerId: PlayerId): Flow<PagingData<ArticleSummary>>
    suspend fun loadPlayerHeatMap(
        playerId: PlayerId,
        seasonId: String,
        teamId: TeamId,
    ): DataResult<PlayerHeatMap>
    suspend fun loadPlayerShotMap(
        playerId: PlayerId,
        matchId: MatchId,
    ): DataResult<PlayerShotMap>
    suspend fun loadPlayerAbility(playerId: PlayerId): DataResult<PlayerAbility?>
}
