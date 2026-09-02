package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdEmptyState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdErrorState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.ArticleMedia
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import io.github.chos1n11111.dongqiudipure.core.model.toAppError
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeRoute(
    onArticleClick: (ArticleId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val articles = viewModel.feed.collectAsLazyPagingItems()
    HomeScreen(
        uiState = uiState,
        articles = articles,
        onArticleClick = onArticleClick,
        onCategorySelect = viewModel::selectCategory,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    articles: LazyPagingItems<ArticleSummary>,
    onArticleClick: (ArticleId) -> Unit,
    onCategorySelect: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("DongqiudiPure") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                CategoryTabs(
                    categories = uiState.categories,
                    selected = uiState.selectedCategory,
                    onSelect = onCategorySelect,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        val refresh = articles.loadState.refresh
        PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading && articles.itemCount > 0,
            onRefresh = articles::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                refresh is LoadState.Loading && articles.itemCount == 0 -> FeedSkeleton()
                refresh is LoadState.Error && articles.itemCount == 0 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    DqdErrorState(
                        error = refresh.error.toAppError(),
                        onRetry = articles::retry,
                    )
                }

                refresh is LoadState.NotLoading && articles.itemCount == 0 -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    DqdEmptyState(
                        title = stringResource(R.string.home_feed_empty_title),
                        description = stringResource(
                            R.string.home_feed_empty_description,
                            uiState.selectedCategory.label,
                        ),
                    )
                }

                else -> FeedList(articles = articles, onArticleClick = onArticleClick)
            }
        }
    }
}

@Composable
private fun FeedList(
    articles: LazyPagingItems<ArticleSummary>,
    onArticleClick: (ArticleId) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id.raw },
        ) { index ->
            val article = articles[index] ?: return@items
            ArticleRow(
                article = article,
                onClick = { onArticleClick(article.id) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        when (articles.loadState.append) {
            is LoadState.Loading -> item(key = "append-loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                }
            }

            is LoadState.Error -> item(key = "append-error") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DqdSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    OutlinedButton(onClick = articles::retry) {
                        Text(stringResource(R.string.home_feed_load_more_retry))
                    }
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<NewsCategory>,
    selected: NewsCategory,
    onSelect: (NewsCategory) -> Unit,
) {
    if (categories.isEmpty()) return
    val selectedIndex = categories.indexOf(selected).coerceAtLeast(0)

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        edgePadding = DqdSpacing.sm,
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onSelect(category) },
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = category.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
private fun FeedSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DqdSpacing.listHorizontal),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                ) {
                    SkeletonBox(Modifier.fillMaxWidth().height(15.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.62f).height(15.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.4f).height(11.dp))
                }
                SkeletonBox(
                    Modifier
                        .width(DqdSize.thumbnailWidth)
                        .height(DqdSize.thumbnailHeight),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview(name = "资讯 · 深色", showBackground = true)
@Composable
private fun HomeScreenDarkPreview() {
    val category = NewsCategory("1", "头条")
    val previewFlow = remember { flowOf(PagingData.from(previewArticles())) }
    DqdTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState(listOf(category), category),
            articles = previewFlow.collectAsLazyPagingItems(),
            onArticleClick = {},
            onCategorySelect = {},
        )
    }
}

private fun previewArticles(): List<ArticleSummary> = listOf(
    ArticleSummary(
        id = ArticleId("preview-1"),
        title = "联赛新赛季赛程公布，多支球队迎来关键开局",
        source = "懂球帝",
        publishedLabel = "今天 10:30",
        commentCount = 128,
        media = ArticleMedia.Thumbnail(url = null),
    ),
)
