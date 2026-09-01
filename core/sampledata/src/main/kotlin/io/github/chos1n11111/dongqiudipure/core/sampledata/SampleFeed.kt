package io.github.chos1n11111.dongqiudipure.core.sampledata

import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

/**
 * 资讯流示例数据。
 *
 * 版式示例，非真实内容。接入 Repository 后本文件随 :core:sampledata 一并删除。
 */
object SampleFeed {

    /** 分类标签。真实实现由 M2 归档的官方公开分类驱动，不写死名单。 */
    val categories: List<String> = listOf(
        "推荐", "最新", "英超", "西甲", "中超", "意甲", "德甲", "欧冠",
    )

    val articles: List<ArticleSummary> = listOf(
        ArticleSummary(
            id = ArticleId("a-1001"),
            title = "补时绝杀！曼城主场 3:2 逆转阿森纳，重回积分榜首位",
            source = "示例来源",
            publishedLabel = "12 分钟前",
            commentCount = 2841,
            media = ArticleMedia.Cover(url = null),
        ),
        ArticleSummary(
            id = ArticleId("a-1002"),
            title = "官方：拜仁慕尼黑与凯恩续约至 2029 年",
            source = "示例来源",
            publishedLabel = "1 小时前",
            commentCount = 512,
            media = ArticleMedia.Thumbnail(url = null),
            tag = "转会",
        ),
        ArticleSummary(
            id = ArticleId("a-1003"),
            title = "图集｜欧冠决赛之夜：伊斯坦布尔的 24 个瞬间",
            source = "示例来源",
            publishedLabel = "3 小时前",
            // 服务端未提供评论数。UI 必须渲染为「—」，不得显示 0。
            commentCount = null,
            media = ArticleMedia.Gallery(url = null, photoCount = 24),
        ),
        ArticleSummary(
            id = ArticleId("a-1004"),
            title = "战术板：为什么三中卫体系在本赛季英超集体失灵",
            source = "示例来源",
            publishedLabel = "5 小时前",
            commentCount = 336,
            media = ArticleMedia.Video(url = null, durationLabel = "08:12"),
        ),
        ArticleSummary(
            id = ArticleId("a-1005"),
            title = "赛后评分：全队最高 8.4，替补登场的他改变了比赛",
            source = "示例来源",
            publishedLabel = "6 小时前",
            commentCount = 1204,
            media = ArticleMedia.Thumbnail(url = null),
        ),
        ArticleSummary(
            id = ArticleId("a-1006"),
            title = "伤情通报：主力中卫预计缺席四到六周",
            source = "示例来源",
            publishedLabel = "8 小时前",
            commentCount = 87,
            media = ArticleMedia.None,
        ),
    )

    val articleDetail: ArticleDetail = ArticleDetail(
        id = ArticleId("a-1001"),
        title = "补时绝杀！曼城主场 3:2 逆转阿森纳，重回积分榜首位",
        source = "示例来源",
        publishedLabel = "2026-09-01 20:52",
        blocks = listOf(
            ArticleBlock.Image(url = null, caption = "图｜示例图片位"),
            ArticleBlock.Paragraph(
                "英超第 4 轮一场焦点战在主场结束。主队在 0:2 落后的情况下连入三球，" +
                    "最终 3:2 完成逆转。",
            ),
            ArticleBlock.Paragraph(
                "上半场第 12 分钟，客队率先由右路内切远射破门，取得领先。" +
                    "随后主队通过一次点球机会将比分扳平。",
            ),
            ArticleBlock.Paragraph(
                "下半场第 61 分钟的换人成为转折点：中场控制力提升后，" +
                    "主队在两分钟内连入两球完成反超。",
            ),
        ),
        relatedEntities = listOf(
            EntityRef.Team(TeamId("t-mci"), "曼城"),
            EntityRef.Team(TeamId("t-ars"), "阿森纳"),
            EntityRef.Player(PlayerId("p-haaland"), "哈兰德"),
            EntityRef.Competition(CompetitionId("c-epl"), "英超"),
        ),
        commentCount = 2841,
    )

    val comments: List<Comment> = listOf(
        Comment(
            id = "c-1",
            authorName = "示例用户 A",
            body = "这场逆转的转折点是第 61 分钟的换人，中场彻底改变了节奏。",
            publishedLabel = "3 小时前",
            replyCount = 12,
        ),
        Comment(
            id = "c-2",
            authorName = "示例用户 B",
            body = "防守端问题还是没解决，两个失球都来自同一侧。",
            publishedLabel = "2 小时前",
            replyCount = null,
        ),
        Comment(
            id = "c-3",
            authorName = "示例用户 C",
            body = "补时阶段的那脚射门角度太刁钻了。",
            publishedLabel = "1 小时前",
            replyCount = 4,
        ),
    )
}
