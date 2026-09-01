package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 资讯流条目。
 *
 * 所有可能缺失的字段都是 nullable，且必须经 UI 层的 MissingValue 渲染，
 * 不得在页面里写 `?: 0` 把缺失伪造成零值（PRODUCT.md §2.4「完整但不伪造」）。
 */
data class ArticleSummary(
    val id: ArticleId,
    val title: String,
    val source: String,
    val publishedLabel: String,
    /** null 表示服务端未提供评论数，不等于 0 条评论。 */
    val commentCount: Int?,
    val media: ArticleMedia,
    val tag: String? = null,
)

/**
 * 条目的媒体形态。决定资讯流用哪种版式渲染。
 *
 * [Gallery] 与 [Video] 只在通过 contract gate 后才会出现
 * （FEATURES.md 中它们当前为「待验证」）。
 */
sealed interface ArticleMedia {

    data object None : ArticleMedia

    /** 大图版式。 */
    data class Cover(val url: String?) : ArticleMedia

    /** 右侧缩略图版式。 */
    data class Thumbnail(val url: String?) : ArticleMedia

    data class Gallery(val url: String?, val photoCount: Int?) : ArticleMedia

    data class Video(val url: String?, val durationLabel: String?) : ArticleMedia
}

/** 文章正文中的一个块。HTML 富文本与普通 JSON 分开解析（ARCHITECTURE.md §7）。 */
sealed interface ArticleBlock {
    data class Paragraph(val text: String) : ArticleBlock
    data class Image(val url: String?, val caption: String?) : ArticleBlock
}

data class ArticleDetail(
    val id: ArticleId,
    val title: String,
    val source: String,
    val publishedLabel: String,
    val blocks: List<ArticleBlock>,
    /** 文章关联的公开实体，是通往资料页的主要路径。 */
    val relatedEntities: List<EntityRef>,
    val commentCount: Int?,
)

/** 指向某个公开实体的引用。只带稳定 ID 与展示所需的最少字段。 */
sealed interface EntityRef {
    val displayName: String

    data class Team(val id: TeamId, override val displayName: String) : EntityRef
    data class Player(val id: PlayerId, override val displayName: String) : EntityRef
    data class Competition(val id: CompetitionId, override val displayName: String) : EntityRef
}

data class Comment(
    val id: String,
    val authorName: String,
    val body: String,
    val publishedLabel: String,
    val replyCount: Int?,
)
