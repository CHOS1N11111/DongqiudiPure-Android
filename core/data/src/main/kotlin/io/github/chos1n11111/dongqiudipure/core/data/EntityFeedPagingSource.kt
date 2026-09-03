package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.AppErrorException
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.EntityFeedRequest
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal data class EntityFeedPageKey(
    val after: String,
    val page: Int,
    val offset: Int,
)

internal class EntityFeedPagingSource(
    private val remote: FootballRemoteDataSource,
    private val entityId: String,
    private val type: String,
) : PagingSource<EntityFeedPageKey, ArticleSummary>() {

    override suspend fun load(
        params: LoadParams<EntityFeedPageKey>,
    ): LoadResult<EntityFeedPageKey, ArticleSummary> {
        val key = params.key
        val request = EntityFeedRequest(
            entityId = entityId,
            type = type,
            after = key?.after,
            page = key?.page,
            offset = key?.offset,
        )
        return when (val result = remote.loadEntityFeed(request)) {
            is ApiResult.Failure -> LoadResult.Error(AppErrorException(result.error))
            is ApiResult.Success -> try {
                if (result.value.code.scalarFootball() != "0") throw ContractViolation()
                val feed = result.value.data ?: throw ContractViolation()
                if (feed.articles == null) throw ContractViolation()
                LoadResult.Page(
                    data = feed.flattenedArticles()
                        .mapNotNull { article -> runCatching { article.toDomain() }.getOrNull() }
                        .distinctBy { it.id },
                    prevKey = null,
                    nextKey = parseEntityFeedNext(feed.next, entityId, type),
                )
            } catch (_: ContractViolation) {
                LoadResult.Error(
                    AppErrorException(AppError.UnsupportedContract(ENTITY_FEED_ENDPOINT)),
                )
            }
        }
    }

    override fun getRefreshKey(
        state: PagingState<EntityFeedPageKey, ArticleSummary>,
    ): EntityFeedPageKey? = null
}

private fun parseEntityFeedNext(
    raw: String?,
    entityId: String,
    type: String,
): EntityFeedPageKey? {
    if (raw.isNullOrBlank()) return null
    val url = raw.toHttpUrlOrNull() ?: throw ContractViolation()
    if (
        url.host != API_HOST ||
        url.encodedPath != ENTITY_FEED_PATH ||
        url.queryParameter("id") != entityId.fullEntityId() ||
        url.queryParameter("type") != type ||
        url.queryParameter("platform") != "android"
    ) {
        throw ContractViolation()
    }
    return EntityFeedPageKey(
        after = url.queryParameter("after")?.takeIf(String::isNotBlank)
            ?: throw ContractViolation(),
        page = url.queryParameter("page")?.toIntOrNull()?.takeIf { it > 0 }
            ?: throw ContractViolation(),
        offset = url.queryParameter("offset")?.toIntOrNull()?.takeIf { it >= 0 }
            ?: throw ContractViolation(),
    )
}

private fun String.fullEntityId(): String = if (length >= 8 && startsWith("50")) {
    this
} else {
    "50${padStart(6, '0')}"
}

private const val API_HOST = "api.dongqiudi.com"
private const val ENTITY_FEED_PATH = "/v3/archive/app/channel/feeds"
private val ENTITY_FEED_ENDPOINT = EndpointId("football.entity-feed")
