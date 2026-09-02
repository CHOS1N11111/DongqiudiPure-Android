package io.github.chos1n11111.dongqiudipure.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FeedResponseDto(
    val id: JsonElement? = null,
    val label: String? = null,
    val page: JsonElement? = null,
    val next: String? = null,
    val articles: List<FeedArticleDto>? = null,
)

@Serializable
data class FeedArticleDto(
    val id: JsonElement? = null,
    val title: String? = null,
    @SerialName("comments_total") val commentsTotal: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("match_image_list") val matchImageList: List<FeedImageDto>? = null,
    @SerialName("showcontent") val showContent: String? = null,
)

@Serializable
data class FeedImageDto(
    val url: String? = null,
    val thumb: String? = null,
    val apithumb: String? = null,
    @SerialName("api_url") val apiUrl: String? = null,
)

@Serializable
data class ArticleDetailEnvelopeDto(
    val code: JsonElement? = null,
    val message: String? = null,
    val data: ArticleDetailDto? = null,
)

@Serializable
data class ArticleDetailDto(
    @SerialName("article_id") val articleId: JsonElement? = null,
    val title: String? = null,
    val time: String? = null,
    @SerialName("show_time") val showTime: JsonElement? = null,
    val writer: String? = null,
    val source: String? = null,
    val body: String? = null,
    val thumb: String? = null,
    val infos: ArticleInfosDto? = null,
)

@Serializable
data class ArticleInfosDto(
    val channels: List<ArticleChannelDto>? = null,
)

@Serializable
data class ArticleChannelDto(
    val id: JsonElement? = null,
    val href: String? = null,
    val tag: String? = null,
    val thumb: String? = null,
)

@Serializable
data class CommentsEnvelopeDto(
    val errCode: JsonElement? = null,
    val errMesg: String? = null,
    val errMsg: String? = null,
    val message: String? = null,
    val data: CommentsDataDto? = null,
)

@Serializable
data class CommentsDataDto(
    val prev: String? = null,
    val next: String? = null,
    @SerialName("comment_list") val commentList: List<CommentDto>? = null,
    @SerialName("recommend_list") val recommendList: List<CommentDto>? = null,
    @SerialName("comment_info") val commentInfo: CommentDto? = null,
    @SerialName("reply_list") val replyList: List<CommentDto>? = null,
    @SerialName("user_list") val userList: List<CommentUserDto>? = null,
    @SerialName("comment_total") val commentTotal: JsonElement? = null,
    @SerialName("article_id") val articleId: JsonElement? = null,
    val article: CommentArticleDto? = null,
)

@Serializable
data class CommentDto(
    val id: JsonElement? = null,
    val up: JsonElement? = null,
    @SerialName("user_id") val userId: JsonElement? = null,
    @SerialName("article_id") val articleId: JsonElement? = null,
    val content: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("show_time") val showTime: JsonElement? = null,
    @SerialName("reply_total") val replyTotal: JsonElement? = null,
    val attachments: List<CommentAttachmentDto>? = null,
)

@Serializable
data class CommentArticleDto(
    val id: JsonElement? = null,
)

@Serializable
data class CommentAttachmentDto(
    val url: String? = null,
    val thumb: String? = null,
    val width: JsonElement? = null,
    val height: JsonElement? = null,
)

@Serializable
data class CommentUserDto(
    val id: JsonElement? = null,
    val username: String? = null,
    val avatar: String? = null,
)
