package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState

@Composable
fun HomeRoute(
    onArticleClick: (ArticleId) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onArticleClick = onArticleClick,
        onSearchClick = onSearchClick,
        onCategorySelect = viewModel::selectCategory,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onArticleClick: (ArticleId) -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelect: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("DongqiudiPure") },
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                painter = painterResource(DqdIcons.Search),
                                // 图标按钮必须有可访问名称
                                contentDescription = "搜索",
                                modifier = Modifier.size(DqdSize.iconMedium),
                            )
                        }
                    },
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
        SectionContainer(
            state = uiState.feed,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            emptyTitle = "该分类暂无资讯",
            emptyDescription = "「${uiState.selectedCategory}」下目前没有已收录的内容，可以切换其他分类。",
            loading = { FeedSkeleton() },
        ) { articles ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = articles,
                    // 稳定 key：刷新与分页时列表不跳动、不丢失滚动位置。
                    key = { it.id.raw },
                ) { article ->
                    ArticleRow(
                        article = article,
                        onClick = { onArticleClick(article.id) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
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
                // 选中态必须与未选中态在颜色**和**字重上都不同：
                // 只靠颜色区分不满足「颜色不是唯一状态提示」。
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

/**
 * 资讯流骨架。
 *
 * 缩略图占位使用与真实条目相同的 112×74dp —— 内容到达时零位移。
 */
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
    DqdTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState(
                categories = io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.categories,
                selectedCategory = "推荐",
                feed = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.articles,
                ),
            ),
            onArticleClick = {},
            onSearchClick = {},
            onCategorySelect = {},
            onRetry = {},
        )
    }
}

@Preview(name = "资讯 · 浅色", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    DqdTheme(darkTheme = false) {
        HomeScreen(
            uiState = HomeUiState(
                categories = io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.categories,
                selectedCategory = "推荐",
                feed = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed.articles,
                ),
            ),
            onArticleClick = {},
            onSearchClick = {},
            onCategorySelect = {},
            onRetry = {},
        )
    }
}
