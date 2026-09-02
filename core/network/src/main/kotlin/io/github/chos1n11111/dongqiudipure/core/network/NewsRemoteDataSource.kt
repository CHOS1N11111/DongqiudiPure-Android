package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedResponseDto

data class FeedRequest(
    val tabId: String,
    val after: String? = null,
    val page: Int? = null,
    val fresh: Boolean = false,
)

data class CommentRequest(
    val articleId: ArticleId,
    val next: String? = null,
    val page: Int? = null,
)

data class CommentThreadRequest(
    val commentId: String,
    val next: String? = null,
    val page: Int? = null,
)

interface NewsRemoteDataSource {
    suspend fun loadFeed(request: FeedRequest): ApiResult<FeedResponseDto>
    suspend fun loadArticle(articleId: ArticleId): ApiResult<ArticleDetailEnvelopeDto>
    suspend fun loadComments(request: CommentRequest): ApiResult<CommentsEnvelopeDto>
    suspend fun loadCommentThread(request: CommentThreadRequest): ApiResult<CommentsEnvelopeDto>
}
