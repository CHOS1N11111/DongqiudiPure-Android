package io.github.chos1n11111.dongqiudipure.core.model

data class CompetitionCatalogGroup(
    val name: String,
    val competitions: List<CompetitionRef>,
)

enum class RankingSection { Standings, Players, Teams }

data class RankingMetric(
    val id: String,
    val name: String,
)

data class RankingRow(
    val rankLabel: String,
    val name: String,
    val imageUrl: String?,
    val value: String?,
    val playerId: PlayerId? = null,
    val team: TeamRef? = null,
)

data class StatisticRankingTable(
    val competition: CompetitionRef,
    val seasonLabel: String,
    val valueColumnLabel: String,
    val rows: List<RankingRow>,
)

data class TeamStatistics(
    val seasonLabel: String?,
    val rankLabel: String?,
    val recordLabel: String?,
    val recentForm: List<FormResult>,
    val categories: List<TeamStatisticCategory>,
    val keyPlayers: List<TeamKeyPlayer>,
)

data class TeamStatisticCategory(
    val name: String,
    val values: List<PlayerSeasonStat>,
)

data class TeamKeyPlayer(
    val metric: String,
    val player: PlayerRef,
    val value: String?,
)
