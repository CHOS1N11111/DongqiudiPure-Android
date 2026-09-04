package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleLinkTarget
import io.github.chos1n11111.dongqiudipure.core.model.CommentBodyPart
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleAccountDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleChannelDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleInfosDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentUserDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedArticleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedImageDto
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `article HTML becomes stable paragraphs images and entity links`() {
        val envelope = json.decodeFromString(
            ArticleDetailEnvelopeDto.serializer(),
            fixture("article-success.json"),
        )

        val article = requireNotNull(envelope.data).toDomain(ArticleId("1001"))

        assertEquals("Fixture match report", article.title)
        assertEquals(
            listOf(
                ArticleBlock.Paragraph("Fixture opening paragraph."),
                ArticleBlock.Paragraph("Fixture section"),
                ArticleBlock.Image(
                    url = "https://fixture.qunliao.info/article/1001.jpg",
                    caption = "Fixture image",
                ),
            ),
            article.blocks,
        )
        assertEquals(
            listOf(
                EntityRef.Team(TeamId("42"), "Fixture FC"),
                EntityRef.Player(PlayerId("7"), "Fixture Player"),
            ),
            article.relatedEntities,
        )
    }

    @Test
    fun `article mapper preserves gif ratio internal link and entity thumbnail`() {
        val article = ArticleDetailDto(
            articleId = JsonPrimitive("1001"),
            title = "Fixture report",
            time = "2026-09-03 04:37",
            writer = "Fixture Desk",
            body = """
                <p><img data-gif-src="https://img1.qunliao.info/goal.gif"
                    data-src="https://img1.qunliao.info/goal.jpg"
                    data-width="640" data-height="360"></p>
                <div class="video" src="https://img.qunliao.info/highlight.mp4"
                    thumb="https://img.qunliao.info/highlight.jpg"
                    data-width="1280" data-height="720"></div>
                <p><a href="dongqiudi://v1/main/match/matchinfo/54473222">查看比赛</a></p>
            """.trimIndent(),
            infos = ArticleInfosDto(
                channels = listOf(
                    ArticleChannelDto(
                        href = "dongqiudi:///team/50000804",
                        tag = "拜仁慕尼黑",
                        thumb = "https://sd.qunliao.info/bayern.png",
                    ),
                ),
            ),
        ).toDomain(ArticleId("1001"))

        assertEquals(
            ArticleBlock.Image(
                url = "https://img1.qunliao.info/goal.gif",
                caption = null,
                aspectRatio = 16f / 9f,
            ),
            article.blocks[0],
        )
        assertEquals(
            ArticleBlock.Video(
                url = "https://img.qunliao.info/highlight.mp4",
                posterUrl = "https://img.qunliao.info/highlight.jpg",
                aspectRatio = 16f / 9f,
            ),
            article.blocks[1],
        )
        assertEquals(
            ArticleBlock.Link(
                text = "查看比赛",
                target = ArticleLinkTarget.Match(MatchId("54473222")),
            ),
            article.blocks[2],
        )
        assertEquals(
            EntityRef.Team(
                id = TeamId("50000804"),
                displayName = "拜仁慕尼黑",
                imageUrl = "https://sd.qunliao.info/bayern.png",
            ),
            article.relatedEntities.single(),
        )
    }

    @Test
    fun `video article uses its real account when source and writer are blank`() {
        val article = ArticleDetailDto(
            articleId = JsonPrimitive("6203328"),
            title = "Video report",
            time = "2026-08-20 19:33",
            writer = "",
            source = "",
            account = ArticleAccountDto(name = "Arsenal account"),
            body = """
                <p><div class="video"
                    src="https://img.qunliao.info/video.mp4"
                    thumb="https://img.qunliao.info/video.mp4?vframe/jpg/offset/1"
                    data-width="720" data-height="1280"></div></p>
            """.trimIndent(),
        ).toDomain(ArticleId("6203328"))

        assertEquals("Arsenal account", article.source)
        assertEquals(
            ArticleBlock.Video(
                url = "https://img.qunliao.info/video.mp4",
                posterUrl = "https://img.qunliao.info/video.mp4?vframe/jpg/offset/1",
                aspectRatio = 720f / 1280f,
            ),
            article.blocks.single(),
        )
    }

    @Test
    fun `feed mapper accepts only observed HTTPS media hosts`() {
        val article = FeedArticleDto(
            id = JsonPrimitive(1001),
            title = "Fixture title",
            commentsTotal = JsonPrimitive("3"),
            createdAt = "2026-09-01 10:00:00",
            authorName = "Fixture Desk",
            matchImageList = listOf(FeedImageDto(apiUrl = "https://tracker.example/image.jpg")),
            showContent = "Fixture pinned",
        ).toDomain()

        assertEquals(3, article.commentCount)
        assertEquals(ArticleMedia.None, article.media)
        assertEquals("2026-09-01 10:00:00", article.publishedLabel)
        assertEquals("Fixture pinned", article.tag)
    }

    @Test
    fun `feed mapper does not treat content layout type as pinned`() {
        val article = FeedArticleDto(
            id = JsonPrimitive(1002),
            title = "Popular fixture",
            commentsTotal = JsonPrimitive(800),
            createdAt = "2026-09-01 11:00:00",
            authorName = "Fixture Desk",
            top = false,
            topnew = JsonPrimitive(4),
        ).toDomain()

        assertEquals(null, article.tag)
    }

    @Test
    fun `entity feed mapper preserves observed video and gallery media types`() {
        val video = FeedArticleDto(
            id = JsonPrimitive(1003),
            title = "Video fixture",
            slideThumb = "https://bdimg7.qunliao.info/video.png",
            isVideo = true,
        ).toDomain()
        val gallery = FeedArticleDto(
            id = JsonPrimitive(1004),
            title = "Gallery fixture",
            matchImageList = listOf(
                FeedImageDto(apiUrl = "https://img1.qunliao.info/one.png"),
                FeedImageDto(apiUrl = "https://img1.qunliao.info/two.png"),
            ),
        ).toDomain()

        assertEquals(
            ArticleMedia.Video("https://bdimg7.qunliao.info/video.png", null),
            video.media,
        )
        assertEquals(
            ArticleMedia.Gallery("https://img1.qunliao.info/one.png", 2),
            gallery.media,
        )
    }

    @Test
    fun `comment mapper strips markup and resolves author from user list`() {
        val users = listOf(
            CommentUserDto(
                id = JsonPrimitive(901),
                username = "Fixture Reader",
                avatar = "https://img1.dongqiudi.com/avatar/901.jpg",
                teamIcon = "https://sd.qunliao.info/teams/42.png",
            ),
        ).byId()

        val comment = CommentDto(
            id = JsonPrimitive(501),
            userId = JsonPrimitive(901),
            content = "<p>Readable <strong>fixture</strong> comment.</p>",
            createdAt = "2026-09-01 10:10:00",
            replyTotal = JsonPrimitive("2"),
            up = JsonPrimitive("9"),
        ).toDomain(users)

        assertEquals("501", comment.id)
        assertEquals("Fixture Reader", comment.authorName)
        assertEquals("Readable fixture comment.", comment.body)
        assertEquals(2, comment.replyCount)
        assertEquals(9, comment.likeCount)
        assertEquals("https://img1.dongqiudi.com/avatar/901.jpg", comment.avatarUrl)
        assertEquals("https://sd.qunliao.info/teams/42.png", comment.teamCrestUrl)
    }

    @Test
    fun `thread mapper uses comment total as reply count and validates article`() {
        val envelope = json.decodeFromString(
            CommentsEnvelopeDto.serializer(),
            fixture("comment-thread-success.json"),
        )

        val parent = requireNotNull(envelope.data).toThreadParent(ArticleId("1001"))

        assertEquals("501", parent.id)
        assertEquals(2, parent.replyCount)
        assertEquals(12, parent.likeCount)
    }

    @Test
    fun `comment mapper preserves an inline emoji without text`() {
        val users = listOf(
            CommentUserDto(id = JsonPrimitive(901), username = "Fixture Reader"),
        ).byId()

        val comment = CommentDto(
            id = JsonPrimitive(501),
            userId = JsonPrimitive(901),
            content = "<img class=\"face\" src=\"https://fixture.qunliao.info/emoji.png\">",
            createdAt = "2026-09-01 10:10:00",
        ).toDomain(users)

        assertEquals("[表情]", comment.body)
        assertEquals(
            CommentBodyPart.InlineImage(
                url = "https://fixture.qunliao.info/emoji.png",
                contentDescription = "[表情]",
            ),
            comment.bodyParts.single(),
        )
    }

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private companion object {
        const val FIXTURE_ROOT = "contracts/news/2026-09-01"
    }
}
