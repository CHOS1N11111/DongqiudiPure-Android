package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingData
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentOrder
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    val categories: List<NewsCategory>
    fun pagedFeed(category: NewsCategory): Flow<PagingData<ArticleSummary>>
}

interface ArticleRepository {
    suspend fun loadArticle(articleId: ArticleId): DataResult<ArticleDetail>
    fun pagedComments(
        articleId: ArticleId,
        order: CommentOrder,
    ): Flow<PagingData<Comment>>
}
