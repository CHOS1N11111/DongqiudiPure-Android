package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailEnvelopeDto
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
    fun `comment mapper strips markup and resolves author from user list`() {
        val users = listOf(
            CommentUserDto(id = JsonPrimitive(901), username = "Fixture Reader"),
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
    }

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private companion object {
        const val FIXTURE_ROOT = "contracts/news/2026-09-01"
    }
}
