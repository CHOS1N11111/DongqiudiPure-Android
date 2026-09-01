package io.github.chos1n11111.dongqiudipure.feature.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionAction
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleBlock
import io.github.chos1n11111.dongqiudipure.core.model.ArticleDetail
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.Comment
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@Composable
fun ArticleRoute(
    articleId: ArticleId,
    onBack: () -> Unit,
    onEntityClick: (EntityRef) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleViewModel = viewModel(),
) {
    LaunchedEffect(articleId) { viewModel.load(articleId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleScreen(
        uiState = uiState,
        onBack = onBack,
        onEntityClick = onEntityClick,
        onRetryDetail = viewModel::retryDetail,
        onRetryComments = viewModel::retryComments,
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
    onBack: () -> Unit,
    onEntityClick: (EntityRef) -> Unit,
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
                    // 第一阶段唯一的操作入口。
                    // 点赞 / 收藏属于 M14 的远端写操作，此处刻意不放置。
                    IconButton(onClick = { /* TODO(share): 接入系统分享 */ }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionContainer(
                state = uiState.detail,
                onRetry = onRetryDetail,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                loading = { ArticleSkeleton() },
            ) { detail ->
                ArticleBody(detail = detail, onEntityClick = onEntityClick)
            }

            // 评论是独立 section：加载更慢，失败也不影响上面的正文。
            SectionContainer(
                state = uiState.comments,
                onRetry = onRetryComments,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.article_comments_title),
                trailing = {
                    Box(modifier = Modifier.clickable(onClick = onSortToggle)) {
                        SectionAction(label = stringResource(uiState.commentSort.labelRes))
                    }
                },
                emptyTitle = stringResource(R.string.article_comments_empty_title),
                emptyDescription = stringResource(R.string.article_comments_empty_description),
            ) { comments ->
                Column {
                    comments.forEach { comment ->
                        CommentRow(comment)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleBody(
    detail: ArticleDetail,
    onEntityClick: (EntityRef) -> Unit,
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
                    ImagePlaceholder(
                        url = block.url,
                        cornerRadius = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                    )
                    val caption = block.caption
                    if (caption != null) {
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
            }
        }

        RelatedEntities(
            entities = detail.relatedEntities,
            onEntityClick = onEntityClick,
        )
    }
}

/**
 * 关联实体。
 *
 * 文章通往资料页的主要路径 —— 只传稳定 ID，资料由目标页的 Repository 加载。
 */
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
        // 类型标签让 chip 自解释：不必靠图标猜这是球队还是球员。
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
private fun CommentRow(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
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
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.publishedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // 回复数缺失时显示「—」，不显示「0 条回复」。
                if (comment.replyCount != null) {
                    Text(
                        text = stringResource(R.string.article_reply_count, comment.replyCount!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    MissingValue(style = MaterialTheme.typography.labelSmall)
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
        SkeletonBox(Modifier.fillMaxWidth().height(190.dp))
    }
}

@Preview(name = "文章 · 深色", showBackground = true)
@Composable
private fun ArticleDarkPreview() {
    DqdTheme(darkTheme = true) {
        ArticleScreen(
            uiState = ArticleUiState(
                detail = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.articleDetail,
                ),
                comments = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.comments,
                ),
            ),
            onBack = {},
            onEntityClick = {},
            onRetryDetail = {},
            onRetryComments = {},
            onSortToggle = {},
        )
    }
}
