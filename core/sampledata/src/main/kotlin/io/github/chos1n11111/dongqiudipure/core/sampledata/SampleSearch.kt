package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef

/** 搜索示例数据。版式示例，非真实结果。 */
object SampleSearch {

    val recentQueries: List<String> = listOf("曼城", "英超积分榜", "哈兰德", "欧冠赛程")

    data class PlayerHit(
        val id: PlayerId,
        val name: String,
        val subtitle: String,
    )

    data class CompetitionHit(
        val id: CompetitionId,
        val name: String,
        val subtitle: String,
    )

    val teams: List<TeamRef> = listOf(
        TeamRef(TeamId("t-mci"), "曼彻斯特城", null),
        TeamRef(TeamId("t-mciw"), "曼城女足", null),
        TeamRef(TeamId("t-mun"), "曼彻斯特联", null),
    )

    val teamSubtitles: Map<String, String> = mapOf(
        "t-mci" to "英超 · 英格兰",
        "t-mciw" to "英格兰女超",
        "t-mun" to "英超 · 英格兰",
    )

    val players: List<PlayerHit> = listOf(
        PlayerHit(PlayerId("p-haaland"), "哈兰德", "曼城 · 前锋 · 9 号"),
        PlayerHit(PlayerId("p-foden"), "福登", "曼城 · 中场 · 47 号"),
    )

    val competitions: List<CompetitionHit> = listOf(
        CompetitionHit(CompetitionId("c-epl"), "英格兰超级联赛", "英格兰 · 2025/26"),
    )

    val articles: List<ArticleSummary> = listOf(
        ArticleSummary(
            id = ArticleId("a-1001"),
            title = "补时绝杀！曼城主场 3:2 逆转阿森纳",
            source = "示例来源",
            publishedLabel = "12 分钟前",
            commentCount = 2841,
            media = ArticleMedia.None,
        ),
        ArticleSummary(
            id = ArticleId("a-1005"),
            title = "赛后评分：全队最高 8.4，替补登场的他改变了比赛",
            source = "示例来源",
            publishedLabel = "6 小时前",
            commentCount = 1204,
            media = ArticleMedia.None,
        ),
    )

    const val ARTICLE_TOTAL = 1264
}
