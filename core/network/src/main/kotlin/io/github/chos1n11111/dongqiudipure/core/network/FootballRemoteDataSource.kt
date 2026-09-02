package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.DataMenuEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CompetitionScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerAbilityEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingTypesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamMembersEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamSampleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamStatisticDto
import java.time.LocalDate

interface FootballRemoteDataSource {
    suspend fun loadImportantMatches(startDate: LocalDate): ApiResult<MatchListEnvelopeDto>
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
    suspend fun loadTeamStatistics(teamId: TeamId): ApiResult<TeamStatisticDto>
    suspend fun loadTeamMembers(teamId: TeamId): ApiResult<TeamMembersEnvelopeDto>
    suspend fun loadTeamSchedule(teamId: TeamId): ApiResult<TeamScheduleEnvelopeDto>
    suspend fun loadPlayerDetail(playerId: PlayerId): ApiResult<PlayerDetailDto>
    suspend fun loadPlayerStatistics(playerId: PlayerId): ApiResult<PlayerStatisticsDto>
    suspend fun loadPlayerAbility(playerId: PlayerId): ApiResult<PlayerAbilityEnvelopeDto>
}
