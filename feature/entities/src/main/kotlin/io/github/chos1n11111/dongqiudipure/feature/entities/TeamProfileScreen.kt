package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.FormBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchStatusBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile

@Composable
fun TeamProfileRoute(
    teamId: TeamId,
    onBack: () -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamProfileViewModel = viewModel(),
) {
    LaunchedEffect(teamId) { viewModel.load(teamId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onMatchClick = onMatchClick,
        onPlayerClick = onPlayerClick,
        onArticleClick = onArticleClick,
        onTabSelect = viewModel::selectTab,
        onRetry = viewModel::retryAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProfileScreen(
    uiState: TeamProfileUiState,
    onBack: () -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onTabSelect: (TeamTab) -> Unit,
    onRetry: () -> Unit,
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
                state = uiState.profile,
                onRetry = onRetry,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                loading = { ProfileHeaderSkeleton() },
            ) { profile ->
                ProfileHeader(profile)
            }

            PrimaryScrollableTabRow(
                selectedTabIndex = TeamTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = DqdSpacing.sm,
            ) {
                TeamTab.entries.forEach { tab ->
                    val isSelected = tab == uiState.selectedTab
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelect(tab) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = stringResource(tab.labelRes),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            when (uiState.selectedTab) {
                TeamTab.Squad -> {
                    SectionContainer(
                        state = uiState.squad,
                        onRetry = onRetry,
                        emptyTitle = stringResource(R.string.team_squad_empty_title),
                        emptyDescription = stringResource(R.string.team_squad_empty_description),
                    ) { squad -> SquadList(squad = squad, onPlayerClick = onPlayerClick) }
                    return@Column
                }

                TeamTab.Fixtures -> {
                    SectionContainer(
                        state = uiState.fixtures,
                        onRetry = onRetry,
                        emptyTitle = stringResource(R.string.team_fixtures_empty_title),
                        emptyDescription = stringResource(R.string.team_fixtures_empty_description),
                    ) { fixtures ->
                        TeamFixtureList(fixtures = fixtures, onMatchClick = onMatchClick)
                    }
                    return@Column
                }

                TeamTab.Stats -> {
                    SectionContainer(
                        state = uiState.detailedStats,
                        onRetry = onRetry,
                        emptyTitle = stringResource(R.string.team_stats_empty_title),
                        emptyDescription = stringResource(R.string.team_stats_empty_description),
                    ) { stats -> TeamStatsGrid(stats = stats) }
                    return@Column
                }

                TeamTab.News -> {
                    SectionContainer(
                        state = uiState.news,
                        onRetry = onRetry,
                        emptyTitle = stringResource(R.string.team_news_empty_title),
                        emptyDescription = stringResource(R.string.team_news_empty_description),
                    ) { news ->
                        TeamNewsList(news = news, onArticleClick = onArticleClick)
                    }
                    return@Column
                }

                TeamTab.Overview -> Unit
            }

            // 近期战绩
            SectionContainer(
                state = uiState.profile,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.team_recent_form),
            ) { profile ->
                Row(
                    modifier = Modifier.padding(
                        start = DqdSpacing.listHorizontal,
                        end = DqdSpacing.listHorizontal,
                        bottom = DqdSpacing.md,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    profile.recentForm.forEach { FormBadge(result = it) }
                }
            }

            // 本赛季数据
            SectionContainer(
                state = uiState.seasonStats,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.team_season_stats),
                loading = { SeasonStatsSkeleton() },
            ) { stats ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        stats.forEach { stat ->
                            SeasonStatTile(stat = stat, modifier = Modifier.weight(1f))
                        }
                    }
                    val missing = stats.filter { it.value == null }
                    if (missing.isNotEmpty()) {
                        MissingHint(missing.joinToString("、") { it.label })
                    }
                }
            }

            // 下一场
            SectionContainer(
                state = uiState.nextMatch,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.team_next_match),
                emptyTitle = stringResource(R.string.team_next_match_empty_title),
                emptyDescription = stringResource(R.string.team_next_match_empty_description),
            ) { match ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMatchClick(match.id) }
                        .padding(DqdSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
                ) {
                    MatchStatusBadge(status = match.status, modifier = Modifier.width(48.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        listOf(match.home, match.away).forEach { team ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                            ) {
                                TeamCrest(
                                    teamId = team.id,
                                    teamName = team.name,
                                    crestUrl = team.crestUrl,
                                )
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                    Icon(
                        painter = painterResource(DqdIcons.ChevronRight),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DqdSize.iconSmall),
                    )
                }
            }

            Box(modifier = Modifier.height(DqdSpacing.xl))
        }
    }
}

@Composable
private fun ProfileHeader(profile: TeamProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
    ) {
        TeamCrest(
            teamId = profile.id,
            teamName = profile.name,
            crestUrl = profile.crestUrl,
            size = 56.dp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = listOfNotNull(
                    profile.competitionName,
                    profile.venue,
                    profile.foundedLabel?.let { stringResource(R.string.team_founded, it) },
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SeasonStatTile(stat: SeasonStat, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = DqdSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ValueText(
            value = stat.value,
            style = DqdTheme.dataText.scoreLarge.copy(
                fontSize = androidx.compose.ui.unit.TextUnit(
                    22f,
                    androidx.compose.ui.unit.TextUnitType.Sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Text(
            text = stat.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 缺失项的解释。
 *
 * 只画一个「—」不够 —— 用户需要知道那是「服务端没给」而不是「应用坏了」。
 */
@Composable
private fun MissingHint(labels: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DqdSpacing.listHorizontal,
                vertical = DqdSpacing.sm,
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(DqdIcons.Info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(12.dp),
        )
        Text(
            text = stringResource(R.string.team_missing_hint, labels),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Composable
private fun ProfileHeaderSkeleton() {
    Row(
        modifier = Modifier.padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(56.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBox(Modifier.width(120.dp).height(22.dp))
            SkeletonBox(Modifier.width(180.dp).height(13.dp))
        }
    }
}

@Composable
private fun SeasonStatsSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(3) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SkeletonBox(Modifier.width(40.dp).height(24.dp))
                SkeletonBox(Modifier.width(32.dp).height(12.dp))
            }
        }
    }
}

@Preview(name = "球队主页 · 深色", showBackground = true)
@Composable
private fun TeamProfileDarkPreview() {
    DqdTheme(darkTheme = true) {
        TeamProfileScreen(
            uiState = TeamProfileUiState(
                profile = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.teamProfile,
                ),
                seasonStats = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleTeamStats
                        .overview.map { (label, value) -> SeasonStat(label, value) },
                ),
                nextMatch = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.matches[1],
                ),
            ),
            onBack = {},
            onMatchClick = {},
            onPlayerClick = {},
            onArticleClick = {},
            onTabSelect = {},
            onRetry = {},
        )
    }
}
