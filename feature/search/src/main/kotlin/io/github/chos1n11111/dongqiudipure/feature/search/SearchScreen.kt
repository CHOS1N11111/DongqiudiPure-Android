package io.github.chos1n11111.dongqiudipure.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleSearch

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onCompetitionClick: (CompetitionId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        uiState = uiState,
        onBack = onBack,
        onQueryChange = viewModel::updateQuery,
        onClear = viewModel::clearQuery,
        onRecentClick = viewModel::selectRecent,
        onFilterSelect = viewModel::selectFilter,
        onRetry = viewModel::retry,
        onTeamClick = onTeamClick,
        onArticleClick = onArticleClick,
        onCompetitionClick = onCompetitionClick,
        onPlayerClick = onPlayerClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRecentClick: (String) -> Unit,
    onFilterSelect: (SearchFilter) -> Unit,
    onRetry: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onCompetitionClick: (CompetitionId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        SearchField(
                            query = uiState.query,
                            onQueryChange = onQueryChange,
                            onClear = onClear,
                            focusRequester = focusRequester,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(DqdIcons.ArrowBack),
                                contentDescription = "返回",
                                modifier = Modifier.size(DqdSize.iconMedium),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                if (uiState.results != null) {
                    FilterRow(
                        selected = uiState.filter,
                        onSelect = onFilterSelect,
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val results = uiState.results
            if (results == null) {
                RecentQueries(
                    queries = uiState.recentQueries,
                    onClick = onRecentClick,
                )
            } else {
                SectionContainer(
                    state = results,
                    onRetry = onRetry,
                    emptyTitle = "没有找到相关内容",
                    emptyDescription = "换个关键词试试。当前仅支持搜索资讯、球队、球员和赛事。",
                ) { content ->
                    ResultList(
                        results = content,
                        filter = uiState.filter,
                        onTeamClick = onTeamClick,
                        onArticleClick = onArticleClick,
                        onCompetitionClick = onCompetitionClick,
                        onPlayerClick = onPlayerClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = DqdSpacing.sm)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            // 1.5dp 主色描边：全屏唯一的强调焦点。
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Icon(
            painter = painterResource(DqdIcons.Search),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "搜索球队、球员、赛事或资讯",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(
                    MaterialTheme.typography.bodySmall,
                ).copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .focusRequester(focusRequester),
            )
        }

        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(DqdSize.touchTarget - 20.dp),
            ) {
                Icon(
                    painter = painterResource(DqdIcons.Close),
                    contentDescription = "清空搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(DqdSize.iconSmall),
                )
            }
        }
    }
}

@Composable
private fun FilterRow(selected: SearchFilter, onSelect: (SearchFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SearchFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = DqdSpacing.md, vertical = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentQueries(queries: List<String>, onClick: (String) -> Unit) {
    if (queries.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "搜索历史")
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DqdSpacing.listHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            queries.forEach { query ->
                Text(
                    text = query,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onClick(query) }
                        .padding(horizontal = DqdSpacing.md, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun ResultList(
    results: SearchResults,
    filter: SearchFilter,
    onTeamClick: (TeamId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onCompetitionClick: (CompetitionId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val showAll = filter == SearchFilter.All

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if ((showAll || filter == SearchFilter.Teams) && results.teams.isNotEmpty()) {
            item(key = "h-teams") {
                GroupHeader("球队", "${results.teams.size} 个结果")
            }
            items(results.teams, key = { "t-${it.id.raw}" }) { team ->
                ResultRow(
                    title = team.name,
                    subtitle = SampleSearch.teamSubtitles[team.id.raw],
                    onClick = { onTeamClick(team.id) },
                    leading = {
                        TeamCrest(
                            teamId = team.id,
                            teamName = team.name,
                            size = DqdSize.crestMedium,
                        )
                    },
                )
            }
        }

        if ((showAll || filter == SearchFilter.Players) && results.players.isNotEmpty()) {
            item(key = "h-players") {
                GroupHeader("球员", "${results.players.size} 个结果")
            }
            items(results.players, key = { "p-${it.id.raw}" }) { player ->
                ResultRow(
                    title = player.name,
                    subtitle = player.subtitle,
                    onClick = { onPlayerClick(player.id) },
                    leading = {
                        Box(
                            modifier = Modifier
                                .size(DqdSize.crestMedium)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    },
                )
            }
        }

        if ((showAll || filter == SearchFilter.Competitions) && results.competitions.isNotEmpty()) {
            item(key = "h-comps") {
                GroupHeader("赛事", "${results.competitions.size} 个结果")
            }
            items(results.competitions, key = { "c-${it.id.raw}" }) { competition ->
                ResultRow(
                    title = competition.name,
                    subtitle = competition.subtitle,
                    onClick = { onCompetitionClick(competition.id) },
                    leading = {
                        Box(
                            modifier = Modifier
                                .size(DqdSize.crestMedium)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    },
                )
            }
        }

        if ((showAll || filter == SearchFilter.Articles) && results.articles.isNotEmpty()) {
            item(key = "h-articles") {
                GroupHeader("资讯", "共 ${results.articleTotal} 条")
            }
            items(results.articles, key = { "a-${it.id.raw}" }) { article ->
                ResultRow(
                    title = article.title,
                    subtitle = "${article.source} · ${article.publishedLabel}",
                    onClick = { onArticleClick(article.id) },
                    leading = null,
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = DqdSpacing.listHorizontal,
                end = DqdSpacing.listHorizontal,
                top = DqdSpacing.md,
                bottom = DqdSpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = count,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        leading?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(DqdSize.iconSmall),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Preview(name = "搜索 · 深色", showBackground = true)
@Composable
private fun SearchDarkPreview() {
    DqdTheme(darkTheme = true) {
        SearchScreen(
            uiState = SearchUiState(
                query = "曼城",
                recentQueries = SampleSearch.recentQueries,
                results = SectionState.Content(
                    SearchResults(
                        teams = SampleSearch.teams,
                        players = SampleSearch.players,
                        competitions = SampleSearch.competitions,
                        articles = SampleSearch.articles,
                        articleTotal = SampleSearch.ARTICLE_TOTAL,
                    ),
                ),
            ),
            onBack = {},
            onQueryChange = {},
            onClear = {},
            onRecentClick = {},
            onFilterSelect = {},
            onRetry = {},
            onTeamClick = {},
            onArticleClick = {},
            onCompetitionClick = {},
            onPlayerClick = {},
        )
    }
}
