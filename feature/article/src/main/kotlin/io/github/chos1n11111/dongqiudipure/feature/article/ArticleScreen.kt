package io.github.chos1n11111.dongqiudipure.feature.article

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdEmptyState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdErrorState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.OriginalAspectImage
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionAction
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleLinkTarget
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.toAppError
import kotlinx.coroutines.flow.flowOf

@Composable
fun ArticleRoute(
    articleId: ArticleId,
    onBack: () -> Unit,
    onEntityClick: (EntityRef) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onCompetitionClick: (CompetitionId) -> Unit,
    onCommentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleViewModel = hiltViewModel(),
) {
    LaunchedEffect(articleId) { viewModel.load(articleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val comments = viewModel.comments.collectAsLazyPagingItems()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val shareTitle = stringResource(R.string.article_share_title)

    ArticleScreen(
        uiState = uiState,
        comments = comments,
        onBack = onBack,
        onShare = {
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(
                    Intent.EXTRA_TEXT,
                    "https://www.dongqiudi.com/articles/${articleId.raw}.html",
            )
            context.startActivity(
                Intent.createChooser(intent, shareTitle),
            )
        },
        onEntityClick = onEntityClick,
        onLinkClick = { target ->
            when (target) {
                is ArticleLinkTarget.Match -> onMatchClick(target.id)
                is ArticleLinkTarget.Competition -> onCompetitionClick(target.id)
                is ArticleLinkTarget.Entity -> onEntityClick(target.value)
                is ArticleLinkTarget.External -> runCatching { uriHandler.openUri(target.url) }
                is ArticleLinkTarget.CompetitionCatalog -> Unit
            }
        },
        onCommentClick = onCommentClick,
        onRetryDetail = viewModel::retryDetail,
        onRetryComments = comments::retry,
        onSortToggle = {
            viewModel.selectSort(
                if (uiState.commentSort == CommentSort.Hottest) {
                    CommentSort.Newest
                } else {
                    CommentSort.Hottest
                },
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    uiState: ArticleUiState,
    comments: LazyPagingItems<Comment>,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEntityClick: (EntityRef) -> Unit,
    onLinkClick: (ArticleLinkTarget) -> Unit,
    onCommentClick: (String) -> Unit,
    onRetryDetail: () -> Unit,
    onRetryComments: () -> Unit,
    onSortToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DqdIcons.ArrowBack),
                            contentDescription = stringResource(DesignR.string.ds_action_back),
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            painter = painterResource(DqdIcons.Share),
                            contentDescription = stringResource(DesignR.string.ds_action_share),
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
            item(key = "article-detail") {
                SectionContainer(
                    state = uiState.detail,
                    onRetry = onRetryDetail,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    loading = { ArticleSkeleton() },
                ) { detail ->
                    ArticleBody(
                        detail = detail,
                        onEntityClick = onEntityClick,
                        onLinkClick = onLinkClick,
                    )
                }
            }

            item(key = "comments-header") {
                CommentsHeader(
                    sort = uiState.commentSort,
                    onSortToggle = onSortToggle,
                )
            }

            val refresh = comments.loadState.refresh
            if (comments.itemCount == 0) {
                when (refresh) {
                    is LoadState.Loading -> items(3, key = { "comment-skeleton-$it" }) {
                        CommentSkeleton()
                    }

                    is LoadState.Error -> item(key = "comments-error") {
                        DqdErrorState(
                            error = refresh.error.toAppError(),
                            onRetry = onRetryComments,
                        )
                    }

                    is LoadState.NotLoading -> item(key = "comments-empty") {
                        DqdEmptyState(
                            title = stringResource(R.string.article_comments_empty_title),
                            description = stringResource(
                                R.string.article_comments_empty_description,
                            ),
                        )
                    }
                }
            } else {
                items(
                    count = comments.itemCount,
                    key = comments.itemKey { it.id },
                ) { index ->
                    val comment = comments[index] ?: return@items
                    CommentRow(comment = comment, onClick = { onCommentClick(comment.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                when (comments.loadState.append) {
                    is LoadState.Loading -> item(key = "comments-append-loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp))
                        }
                    }

                    is LoadState.Error -> item(key = "comments-append-error") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DqdSpacing.md),
                            contentAlignment = Alignment.Center,
                        ) {
                            OutlinedButton(onClick = onRetryComments) {
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

@Composable
private fun CommentsHeader(
    sort: CommentSort,
    onSortToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = DqdSpacing.listHorizontal,
                end = DqdSpacing.listHorizontal,
                top = DqdSpacing.lg,
                bottom = DqdSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.article_comments_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier.clickable(onClick = onSortToggle)) {
            SectionAction(label = stringResource(sort.labelRes))
        }
    }
}

@Composable
private fun ArticleBody(
    detail: ArticleDetail,
    onEntityClick: (EntityRef) -> Unit,
    onLinkClick: (ArticleLinkTarget) -> Unit,
) {
    Column {
        Column(
            modifier = Modifier.padding(
                start = DqdSpacing.lg,
                end = DqdSpacing.lg,
                bottom = DqdSpacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        ) {
            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm)) {
                Text(
                    text = detail.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = detail.publishedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        detail.blocks.forEach { block ->
            when (block) {
                is ArticleBlock.Paragraph -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = DqdSpacing.lg,
                        vertical = DqdSpacing.sm,
                    ),
                )

                is ArticleBlock.Image -> Column {
                    OriginalAspectImage(
                        url = block.url,
                        aspectRatio = block.aspectRatio,
                    )
                    block.caption?.let { caption ->
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = DqdSpacing.lg,
                                vertical = DqdSpacing.sm,
                            ),
                        )
                    }
                }

                is ArticleBlock.Link -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLinkClick(block.target) }
                        .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.sm),
                )
            }
        }

        RelatedEntities(
            entities = detail.relatedEntities,
            onEntityClick = onEntityClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedEntities(
    entities: List<EntityRef>,
    onEntityClick: (EntityRef) -> Unit,
) {
    if (entities.isEmpty()) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = DqdSpacing.lg,
                end = DqdSpacing.lg,
                top = DqdSpacing.md,
                bottom = DqdSpacing.lg,
            ),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        entities.forEach { entity ->
            EntityChip(entity = entity, onClick = { onEntityClick(entity) })
        }
    }
}

@Composable
private fun EntityChip(entity: EntityRef, onClick: () -> Unit) {
    val typeLabel = stringResource(
        when (entity) {
            is EntityRef.Team -> R.string.article_entity_type_team
            is EntityRef.Player -> R.string.article_entity_type_player
            is EntityRef.Competition -> R.string.article_entity_type_competition
        },
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.sm, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        ImagePlaceholder(
            url = entity.imageUrl,
            cornerRadius = if (entity is EntityRef.Player) 18.dp else 4.dp,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = typeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = entity.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun CommentRow(
    comment: Comment,
    onClick: (() -> Unit)? = null,
    showReplyCount: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (comment.body.isNotEmpty()) {
                Text(
                    text = comment.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            comment.attachments.forEach { attachment ->
                ImagePlaceholder(
                    url = attachment.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio((attachment.aspectRatio ?: 4f / 3f).coerceIn(0.75f, 1.8f)),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.publishedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (comment.likeCount != null) {
                    Text(
                        text = stringResource(R.string.article_like_count, comment.likeCount!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showReplyCount && comment.replyCount != null) {
                    Text(
                        text = stringResource(R.string.article_reply_count, comment.replyCount!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (showReplyCount) {
                    MissingValue(style = MaterialTheme.typography.labelSmall)
                }
                if (onClick != null) {
                    Icon(
                        painter = painterResource(DqdIcons.ChevronRight),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DqdSize.iconSmall),
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleSkeleton() {
    Column(
        modifier = Modifier.padding(DqdSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(24.dp))
        SkeletonBox(Modifier.fillMaxWidth(0.8f).height(24.dp))
        SkeletonBox(Modifier.fillMaxWidth(0.35f).height(12.dp))
        SkeletonBox(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
    }
}

@Composable
internal fun CommentSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        SkeletonBox(Modifier.size(30.dp), shape = CircleShape)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            SkeletonBox(Modifier.fillMaxWidth(0.25f).height(12.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(14.dp))
            SkeletonBox(Modifier.fillMaxWidth(0.7f).height(14.dp))
        }
    }
}

@Preview(name = "文章 · 深色", showBackground = true)
@Composable
private fun ArticleDarkPreview() {
    val previewComments = remember {
        flowOf(
            PagingData.from(
                listOf(
                    Comment("preview", "看球用户", "这场比赛值得期待。", "今天 12:30", 3, 12),
                ),
            ),
        )
    }
    DqdTheme(darkTheme = true) {
        ArticleScreen(
            uiState = ArticleUiState(
                detail = SectionState.Content(
                    ArticleDetail(
                        id = ArticleId("preview"),
                        title = "新赛季焦点战前瞻",
                        source = "懂球帝",
                        publishedLabel = "今天 11:00",
                        blocks = listOf(
                            ArticleBlock.Paragraph("两支球队将迎来新赛季首次直接交锋。"),
                        ),
                        relatedEntities = emptyList(),
                        commentCount = 1,
                    ),
                ),
            ),
            comments = previewComments.collectAsLazyPagingItems(),
            onBack = {},
            onShare = {},
            onEntityClick = {},
            onLinkClick = {},
            onCommentClick = {},
            onRetryDetail = {},
            onRetryComments = {},
            onSortToggle = {},
        )
    }
}
