package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 积分榜行。
 *
 * 所有数值 nullable：并列排名、扣分、无榜单赛事和「不适用」在服务端有不同表示，
 * 客户端不能把它们统一压成 0（PLAN.md M6 退出条件：
 * 「排名、数据缺失和『不适用』具有不同语义」）。
 */
data class StandingRow(
    val rank: Int,
    val team: TeamRef,
    val played: Int?,
    val won: Int?,
    val drawn: Int?,
    val lost: Int?,
    val goalDifference: Int?,
    val points: Int?,
    val zone: StandingZone?,
)

/**
 * 榜单分区。
 *
 * UI 必须用「色条 + 具名分隔行 + 图例」三重编码呈现，不能只靠颜色 ——
 * PRODUCT.md §8 要求「颜色不是唯一状态提示」。
 */
enum class StandingZone {
    ChampionsLeague,
    EuropaLeague,
    ConferenceLeague,
    Promotion,
    Relegation,
}

data class StandingTable(
    val competition: CompetitionRef,
    val seasonLabel: String,
    val rows: List<StandingRow>,
    val groups: List<StandingGroup> = emptyList(),
    val knockoutStages: List<KnockoutStage> = emptyList(),
    val matchStages: List<CompetitionMatchStage> = emptyList(),
)

data class StandingGroup(
    val name: String,
    val rows: List<StandingRow>,
)

data class KnockoutTie(
    val home: TeamRef?,
    val away: TeamRef?,
    val scoreLabel: String?,
    val winner: String?,
    val matchIds: List<MatchId>,
)

data class KnockoutStage(
    val name: String,
    val ties: List<KnockoutTie>,
)

data class CompetitionMatchStage(
    val name: String,
    val matches: List<MatchSummary>,
)
