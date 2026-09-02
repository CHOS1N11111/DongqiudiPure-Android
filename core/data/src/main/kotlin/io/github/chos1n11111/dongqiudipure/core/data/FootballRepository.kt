package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingSection
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
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
}

interface StandingsRepository {
    val defaultCompetitions: List<CompetitionRef>
    suspend fun loadStandings(competition: CompetitionRef): DataResult<StandingTable?>
    suspend fun loadRankingMetrics(
        competitionId: CompetitionId,
        section: RankingSection,
    ): DataResult<List<RankingMetric>>

    suspend fun loadRanking(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric,
    ): DataResult<StatisticRankingTable?>
}

interface FootballEntityRepository {
    suspend fun loadTeamProfile(teamId: TeamId): DataResult<TeamProfile?>
    suspend fun loadTeamStatistics(teamId: TeamId): DataResult<TeamStatistics?>
    suspend fun loadTeamSquad(teamId: TeamId): DataResult<List<SquadMember>>
    suspend fun loadTeamSchedule(teamId: TeamId): DataResult<List<MatchSummary>>
    suspend fun loadPlayerOverview(playerId: PlayerId): DataResult<PlayerOverview?>
    suspend fun loadPlayerCareer(playerId: PlayerId): DataResult<List<CareerEntry>>
    suspend fun loadPlayerAbility(playerId: PlayerId): DataResult<PlayerAbility?>
}
