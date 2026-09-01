package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRankingRow
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef

/** 球员相关示例数据。版式示例，非真实数据。 */
object SamplePlayers {

    private val epl = CompetitionRef(CompetitionId("c-epl"), "英超", null)

    private fun team(id: String, name: String) = TeamRef(TeamId(id), name, null)

    private fun row(
        rank: Int,
        id: String,
        name: String,
        teamId: String?,
        teamName: String?,
        value: Int?,
        apps: Int?,
    ) = PlayerRankingRow(
        rank = rank,
        player = PlayerRef(PlayerId(id), name, null),
        team = if (teamId != null && teamName != null) team(teamId, teamName) else null,
        primaryValue = value,
        appearances = apps,
    )

    val scorers = PlayerRankingTable(
        competition = epl,
        seasonLabel = "2025/26",
        valueColumnLabel = "进球",
        rows = listOf(
            row(1, "p-haaland", "哈兰德", "t-mci", "曼城", 6, 4),
            row(2, "p-salah", "萨拉赫", "t-liv", "利物浦", 5, 4),
            row(3, "p-saka", "萨卡", "t-ars", "阿森纳", 4, 4),
            row(4, "p-foden", "福登", "t-mci", "曼城", 3, 4),
            row(5, "p-palmer", "帕尔默", "t-che", "切尔西", 3, 3),
            // 转会窗口期间所属球队未确定：显示为「—」，不猜一个队。
            row(6, "p-x1", "示例球员 C", null, null, 3, 4),
            // 出场数未提供，但进球数有 —— 两列的缺失是独立的。
            row(7, "p-x2", "示例球员 D", "t-tot", "热刺", 2, null),
        ),
    )

    val assists = PlayerRankingTable(
        competition = epl,
        seasonLabel = "2025/26",
        valueColumnLabel = "助攻",
        rows = listOf(
            row(1, "p-debruyne", "德布劳内", "t-mci", "曼城", 5, 4),
            row(2, "p-odegaard", "厄德高", "t-ars", "阿森纳", 4, 4),
            row(3, "p-trent", "阿诺德", "t-liv", "利物浦", 3, 4),
            row(4, "p-foden", "福登", "t-mci", "曼城", 3, 4),
            row(5, "p-x3", "示例球员 E", "t-new", "纽卡斯尔", 2, 4),
        ),
    )

    val profile = PlayerProfile(
        id = PlayerId("p-haaland"),
        name = "哈兰德",
        avatarUrl = null,
        team = team("t-mci", "曼城"),
        position = PlayerPosition.Forward,
        shirtNumber = 9,
        nationality = "挪威",
        ageLabel = "25 岁",
        heightLabel = "195 cm",
        // 惯用脚未提供。
        footLabel = null,
    )

    val profileStats: List<PlayerSeasonStat> = listOf(
        PlayerSeasonStat("apps", "出场", "4", 1),
        PlayerSeasonStat("goals", "进球", "6", 2),
        PlayerSeasonStat("assists", "助攻", "1", 3),
        PlayerSeasonStat("minutes", "出场时间", "342′", 4),
        PlayerSeasonStat("yellow", "黄牌", "0", 5),
        // 该赛事未提供预期进球。与上面的「黄牌 0」必须视觉可区分。
        PlayerSeasonStat("xg", "预期进球", null, 6),
    )

    val career: List<CareerEntry> = listOf(
        CareerEntry("2025/26", "曼城", "英超", 4, 6),
        CareerEntry("2024/25", "曼城", "英超", 31, 27),
        CareerEntry("2023/24", "曼城", "英超", 31, 27),
        // 历史赛季的数据未收录：出场与进球都缺失，不补 0。
        CareerEntry("2019/20", "示例俱乐部", null, null, null),
    )

    /** 球队阵容名单。按位置分组展示。 */
    val squad: List<SquadMember> = listOf(
        SquadMember(PlayerId("p-h1"), "埃德森", 31, PlayerPosition.Goalkeeper, "巴西", "32 岁"),
        SquadMember(PlayerId("p-h12"), "奥尔特加", 18, PlayerPosition.Goalkeeper, "德国", "31 岁"),

        SquadMember(PlayerId("p-h2"), "沃克", 2, PlayerPosition.Defender, "英格兰", "35 岁"),
        SquadMember(PlayerId("p-h3"), "迪亚斯", 3, PlayerPosition.Defender, "葡萄牙", "28 岁"),
        SquadMember(PlayerId("p-h13"), "斯通斯", 5, PlayerPosition.Defender, "英格兰", "31 岁"),
        SquadMember(PlayerId("p-h5"), "格瓦迪奥尔", 24, PlayerPosition.Defender, "克罗地亚", "23 岁"),

        SquadMember(PlayerId("p-h6"), "罗德里", 16, PlayerPosition.Midfielder, "西班牙", "29 岁"),
        SquadMember(PlayerId("p-debruyne"), "德布劳内", 17, PlayerPosition.Midfielder, "比利时", "34 岁"),
        SquadMember(PlayerId("p-h8"), "贝尔纳多", 20, PlayerPosition.Midfielder, "葡萄牙", "31 岁"),

        SquadMember(PlayerId("p-haaland"), "哈兰德", 9, PlayerPosition.Forward, "挪威", "25 岁"),
        SquadMember(PlayerId("p-foden"), "福登", 47, PlayerPosition.Forward, "英格兰", "25 岁"),
        // 号码与年龄均未提供。
        SquadMember(PlayerId("p-h11"), "多库", null, PlayerPosition.Forward, "比利时", null),
    )
}

/**
 * 球队维度的示例统计。
 *
 * 指标名（`label`）在真实实现中**来自服务端**（`StatItem.name` / `PlayerSeasonStat.label`），
 * 不是客户端选定的界面文案 —— 所以留在示例数据里，不抽成字符串资源。
 * 抽成资源等于客户端声称拥有这些指标名，与「统计指标由服务端驱动」矛盾。
 */
object SampleTeamStats {

    /** 球队主页「本赛季数据」的三块概览。 */
    val overview: List<Pair<String, String?>> = listOf(
        "积分" to "10",
        "进球" to "11",
        // 该赛事未提供预期进球。必须保持 null。
        "预期进球" to null,
    )

    /** 球队「数据」分栏的完整指标表。 */
    val detailed: List<io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat> = listOf(
        stat("played", "场次", "4", 1),
        stat("won", "胜", "3", 2),
        stat("drawn", "平", "1", 3),
        stat("lost", "负", "0", 4),
        stat("gf", "进球", "11", 5),
        stat("ga", "失球", "4", 6),
        stat("clean", "零封", "2", 7),
        stat("possession", "场均控球", "61%", 8),
        // 该赛事未提供这两项。与上面的「负 0」必须视觉可区分。
        stat("xg", "预期进球", null, 9),
        stat("xga", "预期失球", null, 10),
    )

    private fun stat(id: String, label: String, value: String?, order: Int) =
        io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat(id, label, value, order)
}
