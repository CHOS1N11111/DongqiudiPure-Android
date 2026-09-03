package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.DataMenuEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.EntityFeedEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CompetitionScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchAnalysisDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchNewsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchOverviewDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerAbilityEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerHeatMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerMatchesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerShotMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingTypesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamMembersEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamSampleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamStatisticDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamTransferEnvelopeDto
import java.time.LocalDate

interface FootballRemoteDataSource {
    suspend fun loadImportantMatches(startDate: LocalDate): ApiResult<MatchListEnvelopeDto>
    suspend fun loadMatchDetail(matchId: MatchId): ApiResult<MatchDetailEnvelopeDto>
    suspend fun loadMatchOverview(matchId: MatchId): ApiResult<MatchOverviewDto>
    suspend fun loadMatchNews(matchId: MatchId): ApiResult<MatchNewsEnvelopeDto>
    suspend fun loadMatchLineup(matchId: MatchId): ApiResult<MatchLineupEnvelopeDto>
    suspend fun loadMatchAnalysis(matchId: MatchId): ApiResult<MatchAnalysisDto>
    suspend fun loadCompetitionCatalog(): ApiResult<DataMenuEnvelopeDto>
    suspend fun loadSeasons(competitionId: CompetitionId): ApiResult<List<SeasonDto>>
    suspend fun loadStandings(seasonId: SeasonId): ApiResult<StandingEnvelopeDto>
    suspend fun loadCompetitionSchedule(
        seasonId: SeasonId,
    ): ApiResult<CompetitionScheduleEnvelopeDto>
    suspend fun loadRankingTypes(
        seasonId: SeasonId,
        entity: String,
    ): ApiResult<RankingTypesEnvelopeDto>
    suspend fun loadRanking(
        seasonId: SeasonId,
        entity: String,
        metric: String,
    ): ApiResult<RankingDetailEnvelopeDto>
    suspend fun loadTeamSample(teamId: TeamId): ApiResult<TeamSampleDto>
    suspend fun loadTeamDetail(teamId: TeamId): ApiResult<TeamDetailDto>
    suspend fun loadTeamStatistics(
        teamId: TeamId,
        seasonId: String? = null,
    ): ApiResult<TeamStatisticDto>
    suspend fun loadTeamMembers(teamId: TeamId): ApiResult<TeamMembersEnvelopeDto>
    suspend fun loadTeamSchedule(
        teamId: TeamId,
        seasonId: String? = null,
    ): ApiResult<TeamScheduleEnvelopeDto>
    suspend fun loadTeamTransfers(
        teamId: TeamId,
        windowId: String? = null,
    ): ApiResult<TeamTransferEnvelopeDto>
    suspend fun loadEntityFeed(entityId: String, type: String): ApiResult<EntityFeedEnvelopeDto>
    suspend fun loadPlayerDetail(playerId: PlayerId): ApiResult<PlayerDetailDto>
    suspend fun loadPlayerStatistics(playerId: PlayerId): ApiResult<PlayerStatisticsDto>
    suspend fun loadPlayerMatches(
        playerId: PlayerId,
        page: Int,
    ): ApiResult<PlayerMatchesEnvelopeDto>
    suspend fun loadPlayerHeatMap(
        playerId: PlayerId,
        seasonId: String,
        teamId: TeamId,
    ): ApiResult<PlayerHeatMapDto>
    suspend fun loadPlayerShotMap(
        playerId: PlayerId,
        matchId: MatchId,
    ): ApiResult<PlayerShotMapDto>
    suspend fun loadPlayerAbility(playerId: PlayerId): ApiResult<PlayerAbilityEnvelopeDto>
}
