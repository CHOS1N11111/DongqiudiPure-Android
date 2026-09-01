package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.FormResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchEventKind
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef

/**
 * 比赛示例数据。版式示例，非真实比分。
 *
 * 刻意覆盖了每一种 [MatchStatus] 分支 —— 包括 [MatchStatus.Unknown] ——
 * 这样 UI 在开发期就能看到降级表现，不用等服务端真的返回未知状态。
 */
object SampleMatches {

    private val epl = CompetitionRef(CompetitionId("c-epl"), "英格兰超级联赛", "第 4 轮")
    private val laliga = CompetitionRef(CompetitionId("c-laliga"), "西班牙甲级联赛", "第 3 轮")

    private fun team(id: String, name: String) = TeamRef(TeamId(id), name, crestUrl = null)

    val manCity = team("t-mci", "曼城")
    val arsenal = team("t-ars", "阿森纳")
    private val liverpool = team("t-liv", "利物浦")
    private val chelsea = team("t-che", "切尔西")
    private val manUtd = team("t-mun", "曼联")
    private val spurs = team("t-tot", "托特纳姆热刺")
    private val barcelona = team("t-bar", "巴塞罗那")
    private val villarreal = team("t-vil", "比利亚雷亚尔")
    private val realMadrid = team("t-rma", "皇家马德里")
    private val atletico = team("t-atm", "马德里竞技")

    val liveMatch = MatchSummary(
        id = MatchId("m-2001"),
        competition = epl,
        home = manCity,
        away = arsenal,
        homeScore = 2,
        awayScore = 1,
        status = MatchStatus.Live("67'"),
    )

    val matches: List<MatchSummary> = listOf(
        liveMatch,
        MatchSummary(
            id = MatchId("m-2002"),
            competition = epl,
            home = liverpool,
            away = chelsea,
            homeScore = null,
            awayScore = null,
            status = MatchStatus.NotStarted("22:30"),
        ),
        MatchSummary(
            id = MatchId("m-2003"),
            competition = epl,
            home = manUtd,
            away = spurs,
            homeScore = 3,
            awayScore = 0,
            status = MatchStatus.Finished,
        ),
        MatchSummary(
            id = MatchId("m-2004"),
            competition = laliga,
            home = barcelona,
            away = villarreal,
            homeScore = null,
            awayScore = null,
            status = MatchStatus.Postponed,
        ),
        MatchSummary(
            id = MatchId("m-2005"),
            competition = laliga,
            home = realMadrid,
            away = atletico,
            homeScore = null,
            awayScore = null,
            status = MatchStatus.NotStarted("03:00"),
        ),
        // 服务端返回了当前版本不认识的状态。UI 应原样降级显示，不猜测语义。
        MatchSummary(
            id = MatchId("m-2006"),
            competition = laliga,
            home = team("t-sev", "塞维利亚"),
            away = team("t-bet", "皇家贝蒂斯"),
            homeScore = null,
            awayScore = null,
            status = MatchStatus.Unknown("AWARDED"),
        ),
    )

    val events: List<MatchEvent> = listOf(
        MatchEvent("63'", MatchEventKind.Goal, "德布劳内", "助攻 福登", "2-1", isHome = true),
        MatchEvent("58'", MatchEventKind.YellowCard, "赖斯", "战术犯规", null, isHome = false),
        MatchEvent("54'", MatchEventKind.Substitution, "格拉利什", "多库", null, isHome = true),
        MatchEvent("41'", MatchEventKind.PenaltyGoal, "哈兰德", "点球", "1-1", isHome = true),
        MatchEvent("12'", MatchEventKind.Goal, "萨卡", "助攻 厄德高", "0-1", isHome = false),
    )

    /**
     * 技术统计。
     *
     * 「预期进球」两侧都是 null —— 该赛事未提供此项。
     * UI 必须显示为「—」并保持虚线空槽，不能画成 0 或空白条。
     */
    val stats: List<StatItem> = listOf(
        StatItem("possession", "控球率", "61%", "39%", 0.61f, 0.39f, 1),
        StatItem("shots", "射门", "14", "9", 14f, 9f, 2),
        StatItem("shots_on_target", "射正", "6", "4", 6f, 4f, 3),
        StatItem("corners", "角球", "7", "3", 7f, 3f, 4),
        // 真实的 0，不是缺失。必须与下面的 xG 视觉不同。
        StatItem("offsides", "越位", "0", "2", 0f, 2f, 5),
        StatItem("fouls", "犯规", "11", "14", 11f, 14f, 6),
        StatItem("xg", "预期进球", null, null, null, null, 7),
    )

    val teamProfile = TeamProfile(
        id = TeamId("t-mci"),
        name = "曼彻斯特城",
        crestUrl = null,
        competitionName = "英超",
        venue = "示例球场",
        foundedLabel = "1880",
        recentForm = listOf(
            FormResult.Win,
            FormResult.Win,
            FormResult.Draw,
            FormResult.Win,
            FormResult.Loss,
        ),
    )
}
