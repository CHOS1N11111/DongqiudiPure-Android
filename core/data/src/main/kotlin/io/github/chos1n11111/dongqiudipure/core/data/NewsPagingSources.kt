package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.AppErrorException
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentOrder
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.CommentRequest
import io.github.chos1n11111.dongqiudipure.core.network.FeedRequest
import io.github.chos1n11111.dongqiudipure.core.network.NewsRemoteDataSource
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal data class FeedPageKey(val after: String, val page: Int)
internal data class CommentPageKey(val next: String, val page: Int)

internal class FeedPagingSource(
    private val remote: NewsRemoteDataSource,
    private val tabId: String,
) : PagingSource<FeedPageKey, ArticleSummary>() {

    override suspend fun load(params: LoadParams<FeedPageKey>): LoadResult<FeedPageKey, ArticleSummary> {
        val key = params.key
        return when (
            val result = remote.loadFeed(
                FeedRequest(tabId = tabId, after = key?.after, page = key?.page),
            )
        ) {
            is ApiResult.Failure -> LoadResult.Error(AppErrorException(result.error))
            is ApiResult.Success -> try {
                val articles = result.value.articles ?: throw ContractViolation()
                LoadResult.Page(
                    data = articles.map { it.toDomain() }.distinctBy { it.id },
                    prevKey = null,
                    nextKey = parseFeedNext(result.value.next, tabId),
                )
            } catch (_: ContractViolation) {
                LoadResult.Error(
                    AppErrorException(AppError.UnsupportedContract(FEED_ENDPOINT)),
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<FeedPageKey, ArticleSummary>): FeedPageKey? = null
}

internal class CommentPagingSource(
    private val remote: NewsRemoteDataSource,
    private val articleId: ArticleId,
    private val order: CommentOrder,
) : PagingSource<CommentPageKey, Comment>() {

    override suspend fun load(params: LoadParams<CommentPageKey>): LoadResult<CommentPageKey, Comment> {
        val key = params.key
        return when (
            val result = remote.loadComments(
                CommentRequest(articleId = articleId, next = key?.next, page = key?.page),
            )
        ) {
            is ApiResult.Failure -> LoadResult.Error(AppErrorException(result.error))
            is ApiResult.Success -> try {
                val data = result.value.data ?: throw ContractViolation()
                val regular = data.commentList ?: throw ContractViolation()
                val recommended = data.recommendList ?: throw ContractViolation()
                val users = (data.userList ?: throw ContractViolation()).byId()
                val rows = if (key == null && order == CommentOrder.Recommended) {
                    recommended + regular
                } else {
                    regular
                }
                LoadResult.Page(
                    data = rows.map { it.toDomain(users) }.distinctBy { it.id },
                    prevKey = null,
                    nextKey = parseCommentNext(data.next, articleId),
                )
            } catch (_: ContractViolation) {
                LoadResult.Error(
                    AppErrorException(AppError.UnsupportedContract(COMMENTS_ENDPOINT)),
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<CommentPageKey, Comment>): CommentPageKey? = null
}

private fun parseFeedNext(raw: String?, tabId: String): FeedPageKey? {
    if (raw.isNullOrBlank()) return null
    val url = raw.toHttpUrlOrNull() ?: throw ContractViolation()
    if (url.host != API_HOST || url.encodedPath != "/app/tabs/web/$tabId.json") {
        throw ContractViolation()
    }
    return FeedPageKey(
        after = url.queryParameter("after")?.takeIf(String::isNotBlank) ?: throw ContractViolation(),
        page = url.queryParameter("page")?.toIntOrNull()?.takeIf { it > 0 }
            ?: throw ContractViolation(),
    )
}

private fun parseCommentNext(raw: String?, articleId: ArticleId): CommentPageKey? {
    if (raw.isNullOrBlank()) return null
    val url = raw.toHttpUrlOrNull() ?: throw ContractViolation()
    if (url.host != API_HOST || url.encodedPath != "/v2/article/${articleId.raw}/comment") {
        throw ContractViolation()
    }
    return CommentPageKey(
        next = url.queryParameter("next")?.takeIf(String::isNotBlank) ?: throw ContractViolation(),
        page = url.queryParameter("pn")?.toIntOrNull()?.takeIf { it > 0 }
            ?: throw ContractViolation(),
    )
}

private const val API_HOST = "api.dongqiudi.com"
private val FEED_ENDPOINT = EndpointId("news.feed")
private val COMMENTS_ENDPOINT = EndpointId("news.comments")
