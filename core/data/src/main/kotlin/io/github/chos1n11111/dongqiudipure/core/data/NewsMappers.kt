package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CommentAttachment
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleChannelDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentAttachmentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentUserDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedArticleDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

internal class ContractViolation : Exception()

internal fun FeedArticleDto.toDomain(): ArticleSummary {
    val rawId = id.scalarString().required()
    val image = matchImageList.orEmpty().firstOrNull()
    val imageUrl = safeMediaUrl(image?.apiUrl ?: image?.apithumb ?: image?.thumb ?: image?.url)
    return ArticleSummary(
        id = ArticleId(rawId),
        title = title.required(),
        source = authorName.required(),
        publishedLabel = createdAt.required(),
        commentCount = commentsTotal.scalarIntOrNull(),
        media = imageUrl?.let { ArticleMedia.Thumbnail(it) } ?: ArticleMedia.None,
        tag = showContent?.trim()?.takeIf(String::isNotEmpty),
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
        attachments = commentAttachments,
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
                    val text = element.text().trim()
                    if (text.isNotEmpty()) add(ArticleBlock.Paragraph(text))
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
    url = safeMediaUrl(attr("data-src").ifBlank { attr("orig-src") }.ifBlank { attr("src") }),
    caption = attr("alt").takeIf(String::isNotBlank),
)

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
    return when (match.groupValues[1]) {
        "team" -> EntityRef.Team(TeamId(rawId), name)
        "player" -> EntityRef.Player(PlayerId(rawId), name)
        "competition" -> EntityRef.Competition(CompetitionId(rawId), name)
        else -> null
    }
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

private val ENTITY_LINK = Regex("^dongqiudi:///((?:team|player|competition))/(\\d+)$")
