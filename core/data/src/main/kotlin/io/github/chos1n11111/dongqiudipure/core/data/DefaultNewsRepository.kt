package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentOrder
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.NewsRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultNewsRepository @Inject constructor(
    private val remote: NewsRemoteDataSource,
) : NewsRepository, ArticleRepository {

    override val categories: List<NewsCategory> = listOf(
        NewsCategory(id = "1", label = "头条"),
        NewsCategory(id = "3", label = "英超"),
        NewsCategory(id = "4", label = "意甲"),
        NewsCategory(id = "5", label = "西甲"),
        NewsCategory(id = "6", label = "德甲"),
        NewsCategory(id = "56", label = "中超"),
        NewsCategory(id = "114", label = "世界杯"),
    )

    override fun pagedFeed(category: NewsCategory): Flow<PagingData<ArticleSummary>> =
        Pager(PAGING_CONFIG) { FeedPagingSource(remote, category.id) }.flow

    override suspend fun loadArticle(articleId: ArticleId): DataResult<ArticleDetail> =
        when (val result = remote.loadArticle(articleId)) {
            is ApiResult.Failure -> DataResult.Failure(result.error)
            is ApiResult.Success -> try {
                DataResult.Success(requireNotNull(result.value.data).toDomain(articleId))
            } catch (_: ContractViolation) {
                DataResult.Failure(AppError.UnsupportedContract(ARTICLE_ENDPOINT))
            }
        }

    override fun pagedComments(
        articleId: ArticleId,
        order: CommentOrder,
    ): Flow<PagingData<Comment>> = Pager(PAGING_CONFIG) {
        CommentPagingSource(remote, articleId, order)
    }.flow

    private companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false,
        )
        val ARTICLE_ENDPOINT = EndpointId("news.article")
    }
}
