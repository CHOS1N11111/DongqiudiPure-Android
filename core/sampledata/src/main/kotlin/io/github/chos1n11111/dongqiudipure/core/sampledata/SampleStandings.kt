package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef

/** 积分榜示例数据。版式示例，非真实排名。 */
object SampleStandings {

    private fun row(
        rank: Int,
        id: String,
        name: String,
        played: Int?,
        won: Int?,
        drawn: Int?,
        lost: Int?,
        goalDifference: Int?,
        points: Int?,
        zone: StandingZone? = null,
    ) = StandingRow(
        rank = rank,
        team = TeamRef(TeamId(id), name, crestUrl = null),
        played = played,
        won = won,
        drawn = drawn,
        lost = lost,
        goalDifference = goalDifference,
        points = points,
        zone = zone,
    )

    val premierLeague = StandingTable(
        competition = CompetitionRef(CompetitionId("c-epl"), "英超", null),
        seasonLabel = "2025/26",
        rows = listOf(
            row(1, "t-mci", "曼城", 4, 3, 1, 0, 7, 10, StandingZone.ChampionsLeague),
            row(2, "t-liv", "利物浦", 4, 3, 0, 1, 5, 9, StandingZone.ChampionsLeague),
            row(3, "t-ars", "阿森纳", 4, 2, 2, 0, 4, 8, StandingZone.ChampionsLeague),
            row(4, "t-che", "切尔西", 4, 2, 1, 1, 2, 7, StandingZone.ChampionsLeague),
            row(5, "t-mun", "曼联", 4, 2, 1, 1, 1, 7, StandingZone.EuropaLeague),
            row(6, "t-tot", "热刺", 4, 2, 0, 2, 0, 6),
            row(7, "t-new", "纽卡斯尔", 4, 1, 2, 1, -1, 5),
            row(8, "t-whu", "西汉姆联", 4, 1, 1, 2, -3, 4),
            // 该队本赛季数据尚未同步：净胜球与积分缺失。
            // UI 必须显示为「—」，不得补 0，否则会被误读为「0 分」。
            row(9, "t-bha", "布莱顿", 4, 1, 1, 2, null, null),
            row(18, "t-ips", "伊普斯维奇", 4, 0, 2, 2, -5, 2, StandingZone.Relegation),
            row(19, "t-sou", "南安普顿", 4, 0, 1, 3, -7, 1, StandingZone.Relegation),
            row(20, "t-lei", "莱斯特城", 4, 0, 1, 3, -9, 1, StandingZone.Relegation),
        ),
    )
}
