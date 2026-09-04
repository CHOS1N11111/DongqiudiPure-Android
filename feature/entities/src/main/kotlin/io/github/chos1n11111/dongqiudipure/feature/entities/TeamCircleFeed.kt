package io.github.chos1n11111.dongqiudipure.feature.entities

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
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.TeamCirclePost
import io.github.chos1n11111.dongqiudipure.core.model.toAppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TeamCircleFeed(
    posts: LazyPagingItems<TeamCirclePost>,
    modifier: Modifier = Modifier,
) {
    val refresh = posts.loadState.refresh
    PullToRefreshBox(
        isRefreshing = refresh is LoadState.Loading && posts.itemCount > 0,
        onRefresh = posts::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            refresh is LoadState.Loading && posts.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            refresh is LoadState.Error && posts.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DqdErrorState(
                    error = refresh.error.toAppError(),
                    onRetry = posts::retry,
                    forceRetry = true,
                )
            }

            refresh is LoadState.NotLoading && posts.itemCount == 0 -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DqdEmptyState(
                    title = stringResource(R.string.team_circle_empty_title),
                    description = stringResource(R.string.team_circle_empty_description),
                )
            }

            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    count = posts.itemCount,
                    key = posts.itemKey { it.id.raw },
                ) { index ->
                    posts[index]?.let { post ->
                        TeamCirclePostRow(post)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                when (posts.loadState.append) {
                    is LoadState.Loading -> item(key = "circle-loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        }
                    }

                    is LoadState.Error -> item(key = "circle-error") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(DqdSpacing.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            OutlinedButton(onClick = posts::retry) {
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
private fun TeamCirclePostRow(post: TeamCirclePost) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagePlaceholder(
                url = post.authorAvatarUrl,
                cornerRadius = 18.dp,
                modifier = Modifier.size(36.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                post.createdAtLabel?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md)) {
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            post.thumbnailUrls.firstOrNull()?.let { thumbnail ->
                ImagePlaceholder(
                    url = thumbnail,
                    badgeLabel = post.thumbnailUrls.size.takeIf { it > 1 }?.let {
                        stringResource(R.string.entity_news_gallery_count, it)
                    },
                    modifier = Modifier.width(96.dp).height(72.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg)) {
            post.replyCount?.let {
                Text(
                    text = stringResource(R.string.team_circle_replies, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            post.likeCount?.let {
                Text(
                    text = stringResource(R.string.team_circle_likes, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
