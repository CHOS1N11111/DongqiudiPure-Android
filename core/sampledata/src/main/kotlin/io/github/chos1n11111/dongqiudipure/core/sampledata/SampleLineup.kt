package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.Absentee
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayer
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamLineup
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef

/**
 * 阵容示例数据。版式示例，非真实名单。
 *
 * 两支球队刻意采用不同的数据完整度：
 *  - 主队有完整阵型坐标 → 渲染阵型图
 *  - 客队只有名单没有坐标 → 降级为列表，阵型显示为「—」
 *
 * 这是真实世界的常态：很多数据源只给名单不给站位。
 * 客户端不能为了画面完整去猜坐标。
 */
object SampleLineup {

    private fun starter(
        id: String,
        name: String,
        number: Int?,
        position: PlayerPosition,
        row: Int?,
        column: Int?,
    ) = LineupPlayer(PlayerId(id), name, number, position, row, column)

    private val homeStarters = listOf(
        starter("p-h1", "埃德森", 31, PlayerPosition.Goalkeeper, 0, 0),

        starter("p-h2", "沃克", 2, PlayerPosition.Defender, 1, 0),
        starter("p-h3", "迪亚斯", 3, PlayerPosition.Defender, 1, 1),
        starter("p-h4", "阿克", 6, PlayerPosition.Defender, 1, 2),
        starter("p-h5", "格瓦迪奥尔", 24, PlayerPosition.Defender, 1, 3),

        starter("p-h6", "罗德里", 16, PlayerPosition.Midfielder, 2, 0),
        starter("p-h7", "德布劳内", 17, PlayerPosition.Midfielder, 2, 1),
        starter("p-h8", "贝尔纳多", 20, PlayerPosition.Midfielder, 2, 2),

        starter("p-h9", "福登", 47, PlayerPosition.Forward, 3, 0),
        starter("p-h10", "哈兰德", 9, PlayerPosition.Forward, 3, 1),
        // 球衣号码缺失：UI 应显示「—」而不是留空或补 0。
        starter("p-h11", "多库", null, PlayerPosition.Forward, 3, 2),
    )

    private val homeSubs = listOf(
        starter("p-h12", "奥尔特加", 18, PlayerPosition.Goalkeeper, null, null),
        starter("p-h13", "斯通斯", 5, PlayerPosition.Defender, null, null),
        starter("p-h14", "科瓦契奇", 8, PlayerPosition.Midfielder, null, null),
        starter("p-h15", "格拉利什", 10, PlayerPosition.Forward, null, null),
    )

    // 客队：只有名单，没有阵型坐标。
    private val awayStarters = listOf(
        starter("p-a1", "拉亚", 22, PlayerPosition.Goalkeeper, null, null),
        starter("p-a2", "怀特", 4, PlayerPosition.Defender, null, null),
        starter("p-a3", "萨利巴", 2, PlayerPosition.Defender, null, null),
        starter("p-a4", "加布里埃尔", 6, PlayerPosition.Defender, null, null),
        starter("p-a5", "廷贝尔", 12, PlayerPosition.Defender, null, null),
        starter("p-a6", "赖斯", 41, PlayerPosition.Midfielder, null, null),
        starter("p-a7", "厄德高", 8, PlayerPosition.Midfielder, null, null),
        starter("p-a8", "哈弗茨", 29, PlayerPosition.Midfielder, null, null),
        starter("p-a9", "萨卡", 7, PlayerPosition.Forward, null, null),
        starter("p-a10", "热苏斯", 9, PlayerPosition.Forward, null, null),
        starter("p-a11", "马丁内利", 11, PlayerPosition.Forward, null, null),
    )

    private val awaySubs = listOf(
        starter("p-a12", "拉姆斯代尔", 1, PlayerPosition.Goalkeeper, null, null),
        starter("p-a13", "富安健洋", 18, PlayerPosition.Defender, null, null),
        starter("p-a14", "若日尼奥", 20, PlayerPosition.Midfielder, null, null),
    )

    val matchLineup = MatchLineup(
        home = TeamLineup(
            team = TeamRef(TeamId("t-mci"), "曼城", null),
            formation = "4-3-3",
            starters = homeStarters,
            substitutes = homeSubs,
            coach = "示例主教练",
            absentees = listOf(
                Absentee("示例球员 A", "伤病"),
                // 缺阵原因未提供。UI 显示「—」，不猜「伤病」。
                Absentee("示例球员 B", null),
            ),
        ),
        away = TeamLineup(
            team = TeamRef(TeamId("t-ars"), "阿森纳", null),
            // 服务端未提供阵型，UI 显示为缺失。
            formation = null,
            starters = awayStarters,
            substitutes = awaySubs,
            coach = null,
            absentees = emptyList(),
        ),
    )
}
