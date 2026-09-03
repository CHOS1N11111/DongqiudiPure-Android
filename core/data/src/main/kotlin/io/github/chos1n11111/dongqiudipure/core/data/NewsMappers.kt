package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleLinkTarget
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentAttachment
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleChannelDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentAttachmentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentUserDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedArticleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedResponseDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

internal class ContractViolation : Exception()

internal fun FeedResponseDto.flattenedArticles(
    includeRecommendations: Boolean = true,
    footballOnly: Boolean = false,
): List<FeedArticleDto> {
    val recommendationIds = if (includeRecommendations) {
        emptySet()
    } else {
        recommend.orEmpty().mapNotNull { it.id.scalarString() }.toSet()
    }
    return ((if (includeRecommendations) recommend.orEmpty() else emptyList()) + articles.orEmpty() +
        contents.orEmpty().flatMap { it.articles.orEmpty() })
        .filterNot { it.id.scalarString() in recommendationIds }
        .filterNot { article ->
            article.tabIds.orEmpty().any { it.scalarString() == BETTING_TAB_ID }
        }
        .filter { article -> !footballOnly || article.isFootballOrUnclassified() }
}

internal fun FeedArticleDto.toDomain(): ArticleSummary {
    val rawId = id.scalarString().required()
    val image = matchImageList.orEmpty().firstOrNull()
    val imageUrl = safeMediaUrl(
        image?.apiUrl ?: image?.apithumb ?: image?.thumb ?: image?.url ?: thumb,
    )
    return ArticleSummary(
        id = ArticleId(rawId),
        title = title.required(),
        source = authorName?.trim()?.takeIf(String::isNotEmpty)
            ?: author?.name?.trim()?.takeIf(String::isNotEmpty)
            ?: "",
        publishedLabel = createdAt?.trim()?.takeIf(String::isNotEmpty)
            ?: publishedAt?.trim()?.takeIf(String::isNotEmpty)
            ?: "",
        commentCount = commentsTotal.scalarIntOrNull(),
        media = imageUrl?.let { ArticleMedia.Thumbnail(it) } ?: ArticleMedia.None,
        tag = showContent?.trim()?.takeIf(String::isNotEmpty)
            ?: if (top == true) "置顶" else null,
    )
}

internal fun ArticleDetailDto.toDomain(expectedId: ArticleId): ArticleDetail {
    val actualId = ArticleId(articleId.scalarString().required())
    if (actualId != expectedId) throw ContractViolation()
    return ArticleDetail(
        id = actualId,
        title = title.required(),
        source = source?.takeIf(String::isNotBlank) ?: writer.required(),
        publishedLabel = time.required(),
        blocks = parseArticleBody(body.orEmpty()),
        relatedEntities = infos?.channels.orEmpty()
            .mapNotNull(ArticleChannelDto::toEntityRef)
            .distinct(),
        commentCount = null,
    )
}

internal fun CommentDto.toDomain(users: Map<String, CommentUserDto>): Comment {
    val rawUserId = userId.scalarString().required()
    val user = users[rawUserId] ?: throw ContractViolation()
    val plainContent = plainCommentText(content)
    val commentAttachments = attachments.orEmpty().map(CommentAttachmentDto::toDomain)
    if (plainContent.isEmpty() && commentAttachments.isEmpty()) throw ContractViolation()
    return Comment(
        id = id.scalarString().required(),
        authorName = user.username.required(),
        body = plainContent,
        publishedLabel = createdAt.required(),
        replyCount = replyTotal.scalarIntOrNull(),
        likeCount = up.scalarIntOrNull(),
        attachments = commentAttachments,
    )
}

internal fun io.github.chos1n11111.dongqiudipure.core.network.dto.CommentsDataDto
    .toThreadParent(expectedArticleId: ArticleId): Comment {
    val parent = commentInfo ?: throw ContractViolation()
    val actualArticleId = parent.articleId.scalarString()
        ?: article?.id.scalarString()
        ?: throw ContractViolation()
    if (actualArticleId != expectedArticleId.raw) throw ContractViolation()
    return parent.toDomain((userList ?: throw ContractViolation()).byId()).copy(
        replyCount = commentTotal.scalarIntOrNull(),
    )
}

internal fun List<CommentUserDto>.byId(): Map<String, CommentUserDto> =
    associateBy { it.id.scalarString().required() }

private fun parseArticleBody(html: String): List<ArticleBlock> {
    if (html.isBlank()) return emptyList()
    return buildList {
        Jsoup.parseBodyFragment(html).body().children().forEach { element ->
            when (element.tagName().lowercase()) {
                "p" -> {
                    val image = element.selectFirst("img")
                    if (image != null) add(image.toImageBlock())
                    val links = element.select("a[href]")
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        val onlyLink = links.singleOrNull()?.takeIf { it.text().trim() == text }
                        val target = onlyLink?.toArticleLinkTarget(text)
                        add(
                            if (target != null) ArticleBlock.Link(text, target)
                            else ArticleBlock.Paragraph(text),
                        )
                    }
                }

                "img" -> add(element.toImageBlock())
                "h1", "h2", "h3", "h4", "h5", "h6", "blockquote" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) add(ArticleBlock.Paragraph(text))
                }

                else -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) add(ArticleBlock.Paragraph(text))
                }
            }
        }
    }
}

private fun Element.toImageBlock(): ArticleBlock.Image = ArticleBlock.Image(
    url = safeMediaUrl(
        attr("data-gif-src")
            .ifBlank { attr("data-src") }
            .ifBlank { attr("orig-src") }
            .ifBlank { attr("src") },
    ),
    caption = attr("alt").takeIf(String::isNotBlank),
    aspectRatio = imageAspectRatio(),
)

private fun Element.imageAspectRatio(): Float? {
    val width = attr("data-width").ifBlank { attr("width") }.toFloatOrNull()
    val height = attr("data-height").ifBlank { attr("height") }.toFloatOrNull()
    return if (width != null && height != null && width > 0f && height > 0f) {
        width / height
    } else {
        null
    }
}

private fun Element.toArticleLinkTarget(label: String): ArticleLinkTarget? {
    val raw = attr("href").trim()
    MATCH_LINK.matchEntire(raw)?.groupValues?.get(1)?.let {
        return ArticleLinkTarget.Match(MatchId(it))
    }
    DATA_RANKING_LINK.matchEntire(raw)?.groupValues?.get(1)?.let {
        return ArticleLinkTarget.CompetitionCatalog(it)
    }
    ENTITY_LINK.matchEntire(raw)?.let { match ->
        val id = match.groupValues[2]
        val entity = when (match.groupValues[1]) {
            "team" -> EntityRef.Team(TeamId(id), label)
            "player" -> EntityRef.Player(PlayerId(id), label)
            "competition" -> EntityRef.Competition(CompetitionId(id), label)
            else -> return null
        }
        return ArticleLinkTarget.Entity(entity)
    }
    val uri = runCatching { java.net.URI(raw) }.getOrNull()
    return raw.takeIf { uri?.scheme == "https" }?.let(ArticleLinkTarget::External)
}

private fun CommentAttachmentDto.toDomain(): CommentAttachment {
    val pixelWidth = width.scalarIntOrNull()
    val pixelHeight = height.scalarIntOrNull()
    return CommentAttachment(
        url = safeMediaUrl(thumb?.takeIf(String::isNotBlank) ?: url),
        aspectRatio = if (pixelWidth != null && pixelHeight != null &&
            pixelWidth > 0 && pixelHeight > 0
        ) {
            pixelWidth.toFloat() / pixelHeight
        } else {
            null
        },
    )
}

private fun plainCommentText(html: String?): String {
    val body = Jsoup.parseBodyFragment(html.orEmpty()).body()
    body.select("img").forEach { image ->
        val fallback = if (image.hasClass("face")) "[表情]" else "[图片]"
        image.replaceWith(TextNode(image.attr("alt").trim().ifEmpty { fallback }))
    }
    return body.text().trim()
}

private fun ArticleChannelDto.toEntityRef(): EntityRef? {
    val match = ENTITY_LINK.matchEntire(href.orEmpty()) ?: return null
    val name = tag?.takeIf(String::isNotBlank) ?: return null
    val rawId = match.groupValues[2]
    val imageUrl = safeMediaUrl(thumb)
    return when (match.groupValues[1]) {
        "team" -> EntityRef.Team(TeamId(rawId), name, imageUrl)
        "player" -> EntityRef.Player(PlayerId(rawId), name, imageUrl)
        "competition" -> EntityRef.Competition(CompetitionId(rawId), name, imageUrl)
        else -> null
    }
}

private fun FeedArticleDto.isFootballOrUnclassified(): Boolean {
    if (tabIds.orEmpty().any { it.scalarString() == FOOTBALL_TAB_ID }) return true
    val primitive = category as? JsonPrimitive ?: return true
    if (!primitive.isString) return true
    return primitive.contentOrNull.equals("足球", ignoreCase = true)
}

private fun safeMediaUrl(raw: String?): String? {
    val value = raw?.takeIf(String::isNotBlank) ?: return null
    val uri = runCatching { java.net.URI(value) }.getOrNull() ?: return null
    return value.takeIf {
        uri.scheme == "https" &&
            (uri.host == "qunliao.info" || uri.host?.endsWith(".qunliao.info") == true)
    }
}

private fun JsonElement?.scalarString(): String? = (this as? JsonPrimitive)?.contentOrNull

private fun JsonElement?.scalarIntOrNull(): Int? {
    if (this == null) return null
    return scalarString()?.toIntOrNull() ?: throw ContractViolation()
}

private fun String?.required(): String =
    this?.trim()?.takeIf(String::isNotEmpty) ?: throw ContractViolation()

private val MATCH_LINK = Regex(
    "^dongqiudi://(?:v1/main/match/matchinfo/|/?(?:game|match)/)(\\d+)$",
)
private val DATA_RANKING_LINK = Regex("^dongqiudi:///?data_ranking/(\\d+)$")
private val ENTITY_LINK = Regex("^dongqiudi:///?((?:team|player|competition))/(\\d+)$")
private const val BETTING_TAB_ID = "58"
private const val FOOTBALL_TAB_ID = "253"
