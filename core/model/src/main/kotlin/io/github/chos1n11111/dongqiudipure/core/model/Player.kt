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
    val englishName: String? = null,
    val team: TeamRef?,
    val position: PlayerPosition,
    val shirtNumber: Int?,
    val nationality: String?,
    val ageLabel: String?,
    val birthdayLabel: String? = null,
    val heightLabel: String?,
    val weightLabel: String? = null,
    val footLabel: String?,
    val marketValueLabel: String? = null,
    val contractUntil: String? = null,
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
    val starts: Int? = null,
    val assists: Int? = null,
    val yellowCards: Int? = null,
    val redCards: Int? = null,
)

/** 阵容名单中的一名成员。按位置分组展示。 */
data class SquadMember(
    val id: PlayerId,
    val name: String,
    val shirtNumber: Int?,
    val position: PlayerPosition,
    val nationality: String?,
    val ageLabel: String?,
    val avatarUrl: String? = null,
    val roleLabel: String? = null,
    val stats: List<PlayerSeasonStat> = emptyList(),
    val salaryLabel: String? = null,
    val isCaptain: Boolean = false,
)

data class PlayerHonor(
    val name: String,
    val logoUrl: String?,
    val times: String?,
    val seasons: List<String>,
)

data class PlayerTransfer(
    val date: String?,
    val type: String?,
    val fee: String?,
    val fromTeam: TeamRef?,
    val toTeam: TeamRef?,
)

data class PlayerInjury(
    val type: String,
    val teamName: String?,
    val startDate: String?,
    val endDate: String?,
    val gamesMissed: Int?,
)

data class PlayerAbility(
    val overall: Int?,
    val version: String?,
    val attributes: List<PlayerSeasonStat>,
)

data class PlayerOverview(
    val profile: PlayerProfile,
    val honors: List<PlayerHonor>,
    val transfers: List<PlayerTransfer>,
    val injuries: List<PlayerInjury>,
    val marketValues: List<MarketValuePoint> = emptyList(),
)
