package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.feature.home.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary

/**
 * 资讯流条目。
 *
 * 四种媒体形态共用同一套栅格与元数据行，只有图片位的摆放不同 ——
 * 这样切换分类时列表节奏保持一致，不会因为条目类型变化而视觉跳动。
 */
@Composable
fun ArticleRow(
    article: ArticleSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val media = article.media) {
        is ArticleMedia.Cover -> CoverArticleRow(article, media.url, onClick, modifier)
        is ArticleMedia.Thumbnail -> SideArticleRow(article, media.url, null, null, onClick, modifier)
        is ArticleMedia.Gallery -> SideArticleRow(
            article = article,
            url = media.url,
            // 张数缺失时不显示角标，而不是显示「0 张」。
            badgeLabel = media.photoCount?.let { stringResource(R.string.home_gallery_count, it) },
            badgeIcon = null,
            onClick = onClick,
            modifier = modifier,
        )

        is ArticleMedia.Video -> SideArticleRow(
            article = article,
            url = media.url,
            badgeLabel = media.durationLabel,
            badgeIcon = null,
            onClick = onClick,
            modifier = modifier,
        )

        ArticleMedia.None -> SideArticleRow(article, null, null, null, onClick, modifier, showImage = false)
    }
}

@Composable
private fun CoverArticleRow(
    article: ArticleSummary,
    url: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(DqdSpacing.listHorizontal),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        ImagePlaceholder(
            url = url,
            modifier = Modifier
                .fillMaxWidth()
                .height(DqdSize.coverHeight),
        )
        ArticleTitle(article.title)
        ArticleMeta(article)
    }
}

@Composable
private fun SideArticleRow(
    article: ArticleSummary,
    url: String?,
    badgeLabel: String?,
    badgeIcon: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(DqdSpacing.listHorizontal),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            ArticleTitle(article.title)
            ArticleMeta(article)
        }
        if (showImage) {
            ImagePlaceholder(
                url = url,
                badgeLabel = badgeLabel,
                badgeIcon = badgeIcon,
                modifier = Modifier
                    .width(DqdSize.thumbnailWidth)
                    .height(DqdSize.thumbnailHeight),
            )
        }
    }
}

@Composable
private fun ArticleTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ArticleMeta(article: ArticleSummary) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = article.source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        Text(
            text = article.publishedLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // 评论数缺失时显示「—」而不是 0：服务端没提供 ≠ 没有评论。
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (article.commentCount != null) {
                Text(
                    text = stringResource(R.string.home_comment_count, article.commentCount!!),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            } else {
                MissingValue(style = MaterialTheme.typography.labelSmall)
            }
        }

        val tag = article.tag
        if (tag != null) {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp),
            )
        }
    }
}
