package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 球员榜单中的一行。
 *
 * 射手榜与助攻榜共用同一个模型：只有 [primaryValue] 的含义不同，
 * 由榜单类型决定表头文案。不为两种榜单各写一套。
 */
data class PlayerRankingRow(
    val rank: Int,
    val player: PlayerRef,
    /** 所属球队。转会期或服务端缺失时为 null。 */
    val team: TeamRef?,
    /** 进球数或助攻数。null = 服务端未提供，不等于 0。 */
    val primaryValue: Int?,
    val appearances: Int?,
)

data class PlayerRankingTable(
    val competition: CompetitionRef,
    val seasonLabel: String,
    /** 表头中 [PlayerRankingRow.primaryValue] 那一列的名称，如「进球」「助攻」。 */
    val valueColumnLabel: String,
    val rows: List<PlayerRankingRow>,
)

/** 球员资料。所有可能缺失的字段都是 nullable，UI 统一降级为「—」。 */
data class PlayerProfile(
    val id: PlayerId,
    val name: String,
    val avatarUrl: String?,
    val team: TeamRef?,
    val position: PlayerPosition,
    val shirtNumber: Int?,
    val nationality: String?,
    val ageLabel: String?,
    val heightLabel: String?,
    val footLabel: String?,
)

/** 一项赛季数据。服务端驱动的开放模型，不写死指标集合。 */
data class PlayerSeasonStat(
    val id: String,
    val label: String,
    val value: String?,
    val displayOrder: Int,
)

/** 履历中的一段。 */
data class CareerEntry(
    val seasonLabel: String,
    val teamName: String,
    val competitionName: String?,
    val appearances: Int?,
    val goals: Int?,
)

/** 阵容名单中的一名成员。按位置分组展示。 */
data class SquadMember(
    val id: PlayerId,
    val name: String,
    val shirtNumber: Int?,
    val position: PlayerPosition,
    val nationality: String?,
    val ageLabel: String?,
)
