package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 球员位置。[Unknown] 保留服务端原值，不猜测归类。
 *
 * 不带展示文案：位置名是界面文字，属于 UI 层的字符串资源
 * （见 :core:designsystem 的 `playerPositionLabel`）。
 * 纯领域模块不持有本地化文本。
 */
enum class PlayerPosition {
    Goalkeeper,
    Defender,
    Midfielder,
    Forward,
    Unknown,
}

data class PlayerRef(
    val id: PlayerId,
    val name: String,
    val avatarUrl: String?,
)

/**
 * 阵容中的一名球员。
 *
 * [gridRow] / [gridColumn] 是阵型图上的坐标。**服务端未提供时必须为 null** ——
 * 此时 UI 降级为纯列表，而不是按位置猜一个站位画到球场上。
 * 猜出来的阵型图看起来很完整，但那是伪造（PRODUCT.md §2.4）。
 */
data class LineupPlayer(
    val id: PlayerId,
    val name: String,
    val shirtNumber: Int?,
    val position: PlayerPosition,
    val gridRow: Int?,
    val gridColumn: Int?,
    val avatarUrl: String? = null,
    val ratingLabel: String? = null,
    val isMvp: Boolean = false,
    val isCaptain: Boolean = false,
    val nationality: String? = null,
    val events: List<LineupPlayerEvent> = emptyList(),
)

data class LineupPlayerEvent(
    val type: String,
    val minuteLabel: String?,
    val iconUrl: String? = null,
)

/** 缺阵球员。[reason] 为 null 表示服务端未说明原因。 */
data class Absentee(
    val name: String,
    val reason: String?,
)

data class TeamLineup(
    val team: TeamRef,
    /** 如 "4-3-3"。服务端未提供时为 null，UI 显示为缺失而不是留空。 */
    val formation: String?,
    val starters: List<LineupPlayer>,
    val substitutes: List<LineupPlayer>,
    val coach: String?,
    val absentees: List<Absentee>,
    val coachRole: String? = null,
    val coachAvatarUrl: String? = null,
    val marketValueLabel: String? = null,
    val averageAgeLabel: String? = null,
) {
    /**
     * 是否具备绘制阵型图的完整坐标。
     *
     * 只要有一名首发缺坐标就整体降级为列表 —— 半张阵型图比没有更容易误导。
     */
    val hasFormationGrid: Boolean
        get() = starters.isNotEmpty() &&
            starters.all { it.gridRow != null && it.gridColumn != null }
}

data class MatchLineup(
    val home: TeamLineup,
    val away: TeamLineup,
)

data class MatchLineupBundle(
    val actual: MatchLineup?,
    val forecast: MatchLineup?,
    val info: MatchInfo,
)
