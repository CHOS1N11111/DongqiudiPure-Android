package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdEmptyState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdErrorState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.toAppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntityNewsFeed(
    articles: LazyPagingItems<ArticleSummary>,
    onArticleClick: (ArticleId) -> Unit,
    emptyTitle: String,
    emptyDescription: String,
    modifier: Modifier = Modifier,
) {
    val refresh = articles.loadState.refresh
    PullToRefreshBox(
        isRefreshing = refresh is LoadState.Loading && articles.itemCount > 0,
        onRefresh = articles::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            refresh is LoadState.Loading && articles.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            refresh is LoadState.Error && articles.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DqdErrorState(
                    error = refresh.error.toAppError(),
                    onRetry = articles::retry,
                    forceRetry = true,
                )
            }

            refresh is LoadState.NotLoading && articles.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DqdEmptyState(title = emptyTitle, description = emptyDescription)
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    count = articles.itemCount,
                    key = articles.itemKey { it.id.raw },
                ) { index ->
                    val article = articles[index] ?: return@items
                    EntityFeedRow(article, onClick = { onArticleClick(article.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                when (articles.loadState.append) {
                    is LoadState.Loading -> item(key = "entity-feed-loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        }
                    }

                    is LoadState.Error -> item(key = "entity-feed-error") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(DqdSpacing.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            OutlinedButton(onClick = articles::retry) {
                                Text(stringResource(R.string.entity_news_load_more_retry))
                            }
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun EntityFeedRow(
    article: ArticleSummary,
    onClick: () -> Unit,
) {
    val media = article.media
    val mediaUrl = when (media) {
        is ArticleMedia.Cover -> media.url
        is ArticleMedia.Gallery -> media.url
        is ArticleMedia.Thumbnail -> media.url
        is ArticleMedia.Video -> media.url
        ArticleMedia.None -> null
    }
    val badgeLabel = when (media) {
        is ArticleMedia.Gallery -> media.photoCount?.let {
            stringResource(R.string.entity_news_gallery_count, it)
        }
        is ArticleMedia.Video -> stringResource(R.string.entity_news_video)
        else -> null
    }
    val badgeIcon = DqdIcons.Play.takeIf { media is ArticleMedia.Video }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            if (article.source.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    article.authorAvatarUrl?.let { avatarUrl ->
                        ImagePlaceholder(
                            url = avatarUrl,
                            cornerRadius = 14.dp,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Text(
                        text = article.source,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = article.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm)) {
                article.tag?.let { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
                article.topicLabel?.let { topic ->
                    Text(
                        text = stringResource(R.string.entity_news_topic, topic),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                article.commentCount?.let { count ->
                    Text(
                        text = stringResource(R.string.entity_news_comments, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        if (media !is ArticleMedia.None) {
            ImagePlaceholder(
                url = mediaUrl,
                badgeLabel = badgeLabel,
                badgeIcon = badgeIcon,
                modifier = Modifier
                    .width(DqdSize.thumbnailWidth)
                    .height(DqdSize.thumbnailHeight),
            )
        }
    }
}
