package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdEmptyState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdErrorState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.toAppError

@Composable
fun CommentDetailRoute(
    articleId: ArticleId,
    commentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CommentDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(articleId, commentId) { viewModel.load(articleId, commentId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val replies = viewModel.replies.collectAsLazyPagingItems()

    CommentDetailScreen(
        uiState = uiState,
        replyCount = replies.itemCount,
        replyAt = { replies[it] },
        replyKey = { index -> replies.peek(index)?.id ?: "reply-$index" },
        refreshState = replies.loadState.refresh,
        appendState = replies.loadState.append,
        onBack = onBack,
        onRetryParent = viewModel::retryParent,
        onRetryReplies = replies::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentDetailScreen(
    uiState: CommentDetailUiState,
    replyCount: Int,
    replyAt: (Int) -> io.github.chos1n11111.dongqiudipure.core.model.Comment?,
    replyKey: (Int) -> Any,
    refreshState: LoadState,
    appendState: LoadState,
    onBack: () -> Unit,
    onRetryParent: () -> Unit,
    onRetryReplies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.comment_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = stringResource(DesignR.string.ds_action_back),
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(key = "parent") {
                SectionContainer(
                    state = uiState.parent,
                    onRetry = onRetryParent,
                    loading = { CommentSkeleton() },
                ) { parent ->
                    CommentRow(comment = parent, showReplyCount = true)
                }
            }
            item(key = "replies-header") {
                Text(
                    text = stringResource(R.string.comment_replies_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            if (replyCount == 0) {
                when (refreshState) {
                    is LoadState.Loading -> items(3, key = { "reply-skeleton-$it" }) {
                        CommentSkeleton()
                    }
                    is LoadState.Error -> item(key = "reply-error") {
                        DqdErrorState(
                            error = refreshState.error.toAppError(),
                            onRetry = onRetryReplies,
                        )
                    }
                    is LoadState.NotLoading -> item(key = "reply-empty") {
                        DqdEmptyState(
                            title = stringResource(R.string.comment_replies_empty_title),
                            description = stringResource(R.string.comment_replies_empty_description),
                        )
                    }
                }
            } else {
                items(count = replyCount, key = replyKey) { index ->
                    val reply = replyAt(index) ?: return@items
                    CommentRow(comment = reply, showReplyCount = false)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                when (appendState) {
                    is LoadState.Loading -> item(key = "reply-append-loading") {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        }
                    }
                    is LoadState.Error -> item(key = "reply-append-error") {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(DqdSpacing.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            OutlinedButton(onClick = onRetryReplies) {
                                Text(stringResource(R.string.article_comments_load_more_retry))
                            }
                        }
                    }
                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}
