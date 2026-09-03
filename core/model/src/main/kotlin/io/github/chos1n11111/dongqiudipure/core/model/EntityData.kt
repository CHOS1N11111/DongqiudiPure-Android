package io.github.chos1n11111.dongqiudipure.core.model

data class SeasonOption(
    val id: String,
    val label: String,
    val isCurrent: Boolean,
)

enum class TeamMemberGroupKind {
    Coaches,
    Staff,
    Forwards,
    Midfielders,
    Defenders,
    Goalkeepers,
    Other,
}

data class TeamSquadGroup(
    val title: String,
    val kind: TeamMemberGroupKind,
    val members: List<SquadMember>,
    val statisticLabels: List<String> = emptyList(),
)

data class TeamSquadData(
    val seasons: List<SeasonOption>,
    val selectedSeasonId: String?,
    val groups: List<TeamSquadGroup>,
)

data class TeamFact(
    val label: String,
    val value: String,
)

data class TeamHonor(
    val name: String,
    val imageUrl: String?,
    val timesLabel: String?,
    val seasons: List<String>,
)

data class HistoricalCoach(
    val player: PlayerRef,
    val startDate: String?,
    val endDate: String?,
    val recordLabel: String?,
    val winRateLabel: String?,
    val durationLabel: String? = null,
)

data class FootballCharacteristics(
    val styles: List<String>,
    val veryStrong: List<String>,
    val strong: List<String>,
    val weak: List<String>,
    val veryWeak: List<String>,
)

data class TeamRankHistoryPoint(
    val seasonLabel: String,
    val rank: Int,
    val teamCount: Int?,
)

data class TeamRecordEntry(
    val rank: Int,
    val player: PlayerRef,
    val countLabel: String,
    val birthdayLabel: String?,
    val nationality: String?,
)

data class TeamRankingTrendPoint(
    val weekLabel: String,
    val rank: Int,
    val dateLabel: String?,
    val match: TeamRankingTrendMatch? = null,
)

data class TeamRankingTrendMatch(
    val home: TeamRef,
    val away: TeamRef,
    val homeScore: Int?,
    val awayScore: Int?,
)

data class TeamScheduleData(
    val seasons: List<SeasonOption>,
    val selectedSeasonId: String?,
    val matches: List<MatchSummary>,
)

data class TeamTransferWindow(
    val id: String,
    val label: String,
    val isCurrent: Boolean,
)

data class TeamTransferEntry(
    val player: PlayerRef,
    val ageLabel: String?,
    val roleLabel: String?,
    val nationality: String?,
    val feeLabel: String?,
    val dateLabel: String?,
    val fromTeam: TeamRef?,
    val toTeam: TeamRef?,
)

data class TeamTransferGroup(
    val title: String,
    val valueLabel: String?,
    val entries: List<TeamTransferEntry>,
)

data class TeamTransferData(
    val windows: List<TeamTransferWindow>,
    val selectedWindowId: String?,
    val groups: List<TeamTransferGroup>,
)

enum class PlayerStatisticScope {
    Total,
    League,
    Cup,
    NationalTeam,
}

data class PlayerStatSection(
    val name: String,
    val values: List<PlayerSeasonStat>,
)

data class PlayerStatisticEntry(
    val id: String,
    val season: SeasonOption,
    val competition: CompetitionRef?,
    val team: TeamRef,
    val summary: List<PlayerSeasonStat>,
    val sections: List<PlayerStatSection>,
)

data class PlayerStatisticsData(
    val defaultScope: PlayerStatisticScope,
    val entries: Map<PlayerStatisticScope, List<PlayerStatisticEntry>>,
)

data class PlayerMatchPerformance(
    val match: MatchSummary,
    val minutesLabel: String?,
    val goals: Int?,
    val assists: Int?,
    val cardsLabel: String?,
    val ratingLabel: String?,
    val userRatingLabel: String?,
)

data class PlayerMatchPage(
    val matches: List<PlayerMatchPerformance>,
    val page: Int,
    val totalPages: Int,
)

data class HeatPoint(
    val x: Float,
    val y: Float,
)

data class PlayerHeatMap(
    val points: List<HeatPoint>,
    val direction: String?,
)

data class PlayerShot(
    val minuteLabel: String?,
    val x: Float?,
    val y: Float?,
    val outcome: String?,
    val situation: String?,
    val shotType: String?,
    val expectedGoalsLabel: String?,
)

data class PlayerShotSummary(
    val expectedGoalsLabel: String?,
    val total: Int?,
    val goals: Int?,
    val onTarget: Int?,
    val offTarget: Int?,
)

data class PlayerShotMap(
    val matchId: MatchId,
    val summary: PlayerShotSummary?,
    val shots: List<PlayerShot>,
)

data class MarketValuePoint(
    val dateLabel: String,
    val valueLabel: String,
    val value: Long?,
    val team: TeamRef?,
)
