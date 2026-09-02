package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.network.di.NewsNetworkModule
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OkHttpNewsRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var remote: OkHttpNewsRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        remote = OkHttpNewsRemoteDataSource(
            client = NewsNetworkModule.provideOkHttpClient(),
            json = NewsNetworkModule.provideJson(),
            baseUrl = server.url("/"),
        )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `feed first page uses anonymous public request contract`() = runBlocking {
        server.enqueue(jsonResponse(fixture("feed-success.json")))

        val result = remote.loadFeed(FeedRequest(tabId = "1"))

        val response = (result as ApiResult.Success).value
        assertEquals("Fixture headlines", response.label)
        assertEquals(1, response.articles?.size)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/app/tabs/web/1.json", request.target)
        assertEquals("application/json", request.headers["Accept"])
        assertEquals("DongqiudiPure-Android/0.1", request.headers["User-Agent"])
        assertEquals(null, request.headers["Authorization"])
        assertEquals(null, request.headers["Cookie"])
    }

    @Test
    fun `feed next page preserves cursor parameters`() = runBlocking {
        server.enqueue(jsonResponse(fixture("feed-empty.json")))

        remote.loadFeed(FeedRequest(tabId = "1", after = "fixture-feed-cursor", page = 2))

        assertEquals(
            "/app/tabs/web/1.json?after=fixture-feed-cursor&page=2&child_tab_id=0&user_pay_type=",
            server.takeRequest().target,
        )
    }

    @Test
    fun `article detail decodes success envelope`() = runBlocking {
        server.enqueue(jsonResponse(fixture("article-success.json")))

        val result = remote.loadArticle(ArticleId("1001"))

        val response = (result as ApiResult.Success).value
        assertEquals("Fixture match report", response.data?.title)
        assertEquals("/v2/article/detail/1001", server.takeRequest().target)
    }

    @Test
    fun `article business error is a server error`() = runBlocking {
        server.enqueue(jsonResponse(fixture("article-business-error.json")))

        val result = remote.loadArticle(ArticleId("1001"))

        assertEquals(
            AppError.Server("40401", "Fixture article unavailable"),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `missing business code is a parse error instead of false success`() = runBlocking {
        server.enqueue(jsonResponse("""{"data":{"article_id":1001}}"""))

        val result = remote.loadArticle(ArticleId("1001"))

        assertEquals(
            AppError.Parse(EndpointId("news.article")),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `comments first and next pages use their distinct query contracts`() = runBlocking {
        server.enqueue(jsonResponse(fixture("comments-success.json")))
        server.enqueue(jsonResponse(fixture("comments-empty.json")))

        val first = remote.loadComments(CommentRequest(ArticleId("1001")))
        val next = remote.loadComments(
            CommentRequest(
                articleId = ArticleId("1001"),
                next = "fixture-comment-cursor",
                page = 2,
            ),
        )

        assertEquals(1, (first as ApiResult.Success).value.data?.commentList?.size)
        assertEquals(0, (next as ApiResult.Success).value.data?.commentList?.size)
        assertEquals(
            "/v2/article/1001/comment?size=20&platform=web",
            server.takeRequest().target,
        )
        assertEquals(
            "/v2/article/1001/comment?sort=down&next=fixture-comment-cursor&pn=2&platform=h5&version=0",
            server.takeRequest().target,
        )
    }

    @Test
    fun `comment thread uses anonymous reply paging contract`() = runBlocking {
        server.enqueue(jsonResponse(fixture("comment-thread-success.json")))
        server.enqueue(jsonResponse(fixture("comment-thread-empty.json")))

        val first = remote.loadCommentThread(CommentThreadRequest(commentId = "501"))
        val next = remote.loadCommentThread(
            CommentThreadRequest(
                commentId = "501",
                next = "fixture-reply-cursor",
                page = 1,
            ),
        )

        assertEquals(1, (first as ApiResult.Success).value.data?.replyList?.size)
        assertEquals(0, (next as ApiResult.Success).value.data?.replyList?.size)
        assertEquals(
            "/v2/comment/501?size=20&sort=up&platform=web",
            server.takeRequest().target,
        )
        assertEquals(
            "/v2/comment/501?size=20&sort=up&next=fixture-reply-cursor&pn=1",
            server.takeRequest().target,
        )
    }

    @Test
    fun `malformed success body maps to endpoint parse error`() = runBlocking {
        server.enqueue(jsonResponse(fixture("malformed.json")))

        val result = remote.loadFeed(FeedRequest(tabId = "1"))

        assertEquals(
            AppError.Parse(EndpointId("news.feed")),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `http error body retains public business code`() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(400)
                .addHeader("Content-Type", "application/json")
                .body(fixture("server-error.json"))
                .build(),
        )

        val result = remote.loadComments(CommentRequest(ArticleId("1001")))

        assertTrue(result is ApiResult.Failure)
        assertEquals(
            AppError.Server("FIXTURE_ERROR", "Fixture request failed"),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `public client does not follow redirects outside the API contract`() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(302)
                .addHeader("Location", server.url("/redirected"))
                .build(),
        )

        val result = remote.loadFeed(FeedRequest(tabId = "1"))

        assertEquals(AppError.Http(302), (result as ApiResult.Failure).error)
        assertEquals(1, server.requestCount)
    }

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private fun jsonResponse(body: String): MockResponse = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()

    private companion object {
        const val FIXTURE_ROOT = "contracts/news/2026-09-01"
    }
}
