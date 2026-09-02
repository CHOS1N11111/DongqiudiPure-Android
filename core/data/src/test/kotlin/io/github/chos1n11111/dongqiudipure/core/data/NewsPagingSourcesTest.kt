package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingSource
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.CommentOrder
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.CommentRequest
import io.github.chos1n11111.dongqiudipure.core.network.CommentThreadRequest
import io.github.chos1n11111.dongqiudipure.core.network.FeedRequest
import io.github.chos1n11111.dongqiudipure.core.network.NewsRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedResponseDto
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsPagingSourcesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `feed page maps items and validates next cursor`() = runBlocking {
        val remote = FakeNewsRemoteDataSource().apply {
            feedResult = ApiResult.Success(feedFixture("feed-success.json"))
        }

        val result = FeedPagingSource(remote, tabId = "1").load(refresh())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("1001"), page.data.map { it.id.raw })
        assertEquals("2026-09-01 10:00:00", page.data.single().publishedLabel)
        assertEquals("Fixture pinned", page.data.single().tag)
        assertEquals(FeedPageKey("fixture-feed-cursor", 2), page.nextKey)
    }

    @Test
    fun `feed rejects next cursor outside the public API host`() = runBlocking {
        val remote = FakeNewsRemoteDataSource().apply {
            feedResult = ApiResult.Success(
                feedFixture("feed-success.json").copy(
                    next = "https://tracker.example/app/tabs/web/1.json?after=x&page=2",
                ),
            )
        }

        val result = FeedPagingSource(remote, tabId = "1").load(refresh())

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `feed rejects a non-positive next page`() = runBlocking {
        val remote = FakeNewsRemoteDataSource().apply {
            feedResult = ApiResult.Success(
                feedFixture("feed-success.json").copy(
                    next = "https://api.dongqiudi.com/app/tabs/web/1.json?after=x&page=0",
                ),
            )
        }

        val result = FeedPagingSource(remote, tabId = "1").load(refresh())

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `recommended comments precede regular comments and duplicate IDs collapse`() = runBlocking {
        val fixture = commentsFixture("comments-success.json")
        val data = requireNotNull(fixture.data)
        val regular = requireNotNull(data.commentList).single()
        val recommended = requireNotNull(data.recommendList).single()
        val remote = FakeNewsRemoteDataSource().apply {
            commentsResult = ApiResult.Success(
                fixture.copy(
                    data = data.copy(
                        recommendList = listOf(recommended, regular),
                    ),
                ),
            )
        }

        val result = CommentPagingSource(
            remote = remote,
            articleId = ArticleId("1001"),
            order = CommentOrder.Recommended,
        ).load(commentRefresh())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("502", "501"), page.data.map { it.id })
        assertEquals(CommentPageKey("fixture-comment-cursor", 2), page.nextKey)
    }

    @Test
    fun `comment without stable ID fails the contract instead of being hidden by deduplication`() = runBlocking {
        val fixture = commentsFixture("comments-success.json")
        val data = requireNotNull(fixture.data)
        val remote = FakeNewsRemoteDataSource().apply {
            commentsResult = ApiResult.Success(
                fixture.copy(
                    data = data.copy(
                        commentList = listOf(
                            CommentDto(
                                id = null,
                                userId = JsonPrimitive(901),
                                content = "Fixture comment with missing ID.",
                                createdAt = "2026-09-01 10:10:00",
                            ),
                        ),
                        recommendList = emptyList(),
                    ),
                ),
            )
        }

        val result = CommentPagingSource(
            remote = remote,
            articleId = ArticleId("1001"),
            order = CommentOrder.Newest,
        ).load(commentRefresh())

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `comment rejects a non-positive next page`() = runBlocking {
        val fixture = commentsFixture("comments-success.json")
        val remote = FakeNewsRemoteDataSource().apply {
            commentsResult = ApiResult.Success(
                fixture.copy(
                    data = requireNotNull(fixture.data).copy(
                        next = "https://api.dongqiudi.com/v2/article/1001/comment?next=x&pn=0",
                    ),
                ),
            )
        }

        val result = CommentPagingSource(
            remote = remote,
            articleId = ArticleId("1001"),
            order = CommentOrder.Newest,
        ).load(commentRefresh())

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `image-only comment remains a valid paging item`() = runBlocking {
        val remote = FakeNewsRemoteDataSource().apply {
            commentsResult = ApiResult.Success(commentsFixture("comments-image-only.json"))
        }

        val result = CommentPagingSource(
            remote = remote,
            articleId = ArticleId("1001"),
            order = CommentOrder.Newest,
        ).load(commentRefresh())

        val comment = (result as PagingSource.LoadResult.Page).data.single()
        assertEquals("", comment.body)
        assertEquals(
            "https://fixture.qunliao.info/comments/503-thumb.jpg",
            comment.attachments.single().url,
        )
        assertEquals(1122f / 1400f, comment.attachments.single().aspectRatio!!, 0.001f)
    }

    @Test
    fun `reply page maps likes and validates comment cursor`() = runBlocking {
        val remote = FakeNewsRemoteDataSource().apply {
            commentThreadResult = ApiResult.Success(commentsFixture("comment-thread-success.json"))
        }

        val result = ReplyPagingSource(
            remote = remote,
            articleId = ArticleId("1001"),
            commentId = "501",
        ).load(replyRefresh())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("601"), page.data.map { it.id })
        assertEquals(4, page.data.single().likeCount)
        assertEquals(ReplyPageKey("fixture-reply-cursor", 1), page.nextKey)
    }

    private fun feedFixture(name: String): FeedResponseDto = json.decodeFromString(
        FeedResponseDto.serializer(),
        fixture(name),
    )

    private fun commentsFixture(name: String): CommentsEnvelopeDto = json.decodeFromString(
        CommentsEnvelopeDto.serializer(),
        fixture(name),
    )

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private fun refresh(): PagingSource.LoadParams.Refresh<FeedPageKey> =
        PagingSource.LoadParams.Refresh(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false,
        )

    private fun commentRefresh(): PagingSource.LoadParams.Refresh<CommentPageKey> =
        PagingSource.LoadParams.Refresh(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false,
        )

    private fun replyRefresh(): PagingSource.LoadParams.Refresh<ReplyPageKey> =
        PagingSource.LoadParams.Refresh(
            key = null,
            loadSize = 20,
            placeholdersEnabled = false,
        )

    private class FakeNewsRemoteDataSource : NewsRemoteDataSource {
        lateinit var feedResult: ApiResult<FeedResponseDto>
        lateinit var commentsResult: ApiResult<CommentsEnvelopeDto>
        lateinit var commentThreadResult: ApiResult<CommentsEnvelopeDto>

        override suspend fun loadFeed(request: FeedRequest): ApiResult<FeedResponseDto> = feedResult

        override suspend fun loadArticle(articleId: ArticleId): ApiResult<ArticleDetailEnvelopeDto> =
            error("Article detail is not used by this paging test")

        override suspend fun loadComments(request: CommentRequest): ApiResult<CommentsEnvelopeDto> =
            commentsResult

        override suspend fun loadCommentThread(
            request: CommentThreadRequest,
        ): ApiResult<CommentsEnvelopeDto> = commentThreadResult
    }

    private companion object {
        const val FIXTURE_ROOT = "contracts/news/2026-09-01"
    }
}
