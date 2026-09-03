package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.FormBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.FootballCharacteristics
import io.github.chos1n11111.dongqiudipure.core.model.HistoricalCoach
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamRankingTrendPoint
import io.github.chos1n11111.dongqiudipure.core.model.TeamRecordEntry
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferEntry

@Composable
fun TeamProfileRoute(
    teamId: TeamId,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeamProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(teamId) { viewModel.load(teamId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val news = viewModel.news.collectAsLazyPagingItems()

    TeamProfileScreen(
        uiState = uiState,
        news = news,
        onBack = onBack,
        onArticleClick = onArticleClick,
        onMatchClick = onMatchClick,
        onTeamClick = onTeamClick,
        onPlayerClick = onPlayerClick,
        onTabSelect = viewModel::selectTab,
        onScheduleSeasonSelect = viewModel::selectScheduleSeason,
        onSquadSeasonSelect = viewModel::selectSquadSeason,
        onStatisticsSeasonSelect = viewModel::selectStatisticsSeason,
        onTransferWindowSelect = viewModel::selectTransferWindow,
        onRetry = viewModel::retryAll,
        onRetryTab = viewModel::retrySelectedTab,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProfileScreen(
    uiState: TeamProfileUiState,
    news: LazyPagingItems<io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary>,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onTabSelect: (TeamTab) -> Unit,
    onScheduleSeasonSelect: (String) -> Unit,
    onSquadSeasonSelect: (String) -> Unit,
    onStatisticsSeasonSelect: (String) -> Unit,
    onTransferWindowSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onRetryTab: () -> Unit,
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
                .padding(padding),
        ) {
            SectionContainer(
                state = uiState.profile,
                onRetry = onRetry,
                forceRetry = true,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                loading = { ProfileHeaderSkeleton() },
            ) { profile -> ProfileHeader(profile) }

            PrimaryScrollableTabRow(
                selectedTabIndex = TeamTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = DqdSpacing.sm,
            ) {
                TeamTab.entries.forEach { tab ->
                    val selected = tab == uiState.selectedTab
                    Tab(
                        selected = selected,
                        onClick = { onTabSelect(tab) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = stringResource(tab.labelRes),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            if (uiState.selectedTab == TeamTab.Dynamic) {
                EntityNewsFeed(
                    articles = news,
                    onArticleClick = onArticleClick,
                    emptyTitle = stringResource(R.string.team_news_empty_title),
                    emptyDescription = stringResource(R.string.team_news_empty_description),
                    modifier = Modifier.weight(1f),
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (uiState.selectedTab) {
                        TeamTab.Dynamic -> Unit
                        TeamTab.Schedule -> SectionContainer(
                            state = uiState.schedule,
                            onRetry = onRetryTab,
                            forceRetry = true,
                            emptyTitle = stringResource(R.string.team_fixtures_empty_title),
                            emptyDescription = stringResource(R.string.team_fixtures_empty_description),
                        ) { schedule ->
                            TeamScheduleContent(
                                matches = schedule.matches,
                                seasons = schedule.seasons,
                                selectedSeasonId = schedule.selectedSeasonId,
                                onSeasonSelect = onScheduleSeasonSelect,
                                onMatchClick = onMatchClick,
                            )
                        }

                        TeamTab.Players -> SectionContainer(
                            state = uiState.squad,
                            onRetry = onRetryTab,
                            forceRetry = true,
                            emptyTitle = stringResource(R.string.team_squad_empty_title),
                            emptyDescription = stringResource(R.string.team_squad_empty_description),
                        ) { squad ->
                            SeasonPicker(squad.seasons, squad.selectedSeasonId, onSquadSeasonSelect)
                            SquadList(squad.groups, onPlayerClick)
                        }

                        TeamTab.Data -> TeamInfoContent(
                            profileState = uiState.profile,
                            statisticsState = uiState.statistics,
                            transfersState = uiState.transfers,
                            onStatisticsSeasonSelect = onStatisticsSeasonSelect,
                            onTransferWindowSelect = onTransferWindowSelect,
                            onPlayerClick = onPlayerClick,
                            onTeamClick = onTeamClick,
                            onRetry = onRetryTab,
                            showProfile = false,
                            showStatistics = true,
                            showTransfers = false,
                        )

                        TeamTab.Info -> TeamInfoContent(
                            profileState = uiState.profile,
                            statisticsState = uiState.statistics,
                            transfersState = uiState.transfers,
                            onStatisticsSeasonSelect = onStatisticsSeasonSelect,
                            onTransferWindowSelect = onTransferWindowSelect,
                            onPlayerClick = onPlayerClick,
                            onTeamClick = onTeamClick,
                            onRetry = onRetryTab,
                            showProfile = true,
                            showStatistics = false,
                            showTransfers = false,
                        )

                        TeamTab.Transfers -> TeamInfoContent(
                            profileState = uiState.profile,
                            statisticsState = uiState.statistics,
                            transfersState = uiState.transfers,
                            onStatisticsSeasonSelect = onStatisticsSeasonSelect,
                            onTransferWindowSelect = onTransferWindowSelect,
                            onPlayerClick = onPlayerClick,
                            onTeamClick = onTeamClick,
                            onRetry = onRetryTab,
                            showProfile = false,
                            showStatistics = false,
                            showTransfers = true,
                        )
                    }
                    Box(modifier = Modifier.height(DqdSpacing.xl))
                }
            }
        }
    }
}

@Composable
private fun TeamScheduleContent(
    matches: List<io.github.chos1n11111.dongqiudipure.core.model.MatchSummary>,
    seasons: List<SeasonOption>,
    selectedSeasonId: String?,
    onSeasonSelect: (String) -> Unit,
    onMatchClick: (MatchId) -> Unit,
) {
    SeasonPicker(seasons, selectedSeasonId, onSeasonSelect)
    if (matches.isEmpty()) {
        InlineEmpty(stringResource(R.string.team_schedule_filter_empty))
    } else {
        TeamFixtureList(matches, onMatchClick)
    }
}

@Composable
private fun TeamInfoContent(
    profileState: SectionState<TeamProfile>,
    statisticsState: SectionState<TeamStatistics>,
    transfersState: SectionState<TeamTransferData>,
    onStatisticsSeasonSelect: (String) -> Unit,
    onTransferWindowSelect: (String) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onRetry: () -> Unit,
    showProfile: Boolean,
    showStatistics: Boolean,
    showTransfers: Boolean,
) {
    if (showProfile) SectionContainer(
        state = profileState,
        onRetry = onRetry,
        forceRetry = true,
        title = stringResource(R.string.team_details),
    ) { profile ->
        TeamFacts(profile)
        if (profile.rankHistory.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_rank_history))
            RankingChart(
                values = profile.rankHistory.map { it.seasonLabel to it.rank },
                maxRank = profile.rankHistory.mapNotNull { it.teamCount }.maxOrNull(),
            )
        }
        if (profile.historicalCoaches.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_historical_coaches))
            var coachesExpanded by remember(profile.id) { mutableStateOf(false) }
            val visibleCoaches = if (coachesExpanded) {
                profile.historicalCoaches
            } else {
                profile.historicalCoaches.take(9)
            }
            visibleCoaches.forEach { coach ->
                HistoricalCoachRow(coach, onPlayerClick)
            }
            if (profile.historicalCoaches.size > 9) {
                Text(
                    text = stringResource(
                        if (coachesExpanded) R.string.team_collapse_all else R.string.team_expand_all,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { coachesExpanded = !coachesExpanded }
                        .padding(vertical = DqdSpacing.md),
                )
            }
        }
        if (profile.topScorers.isNotEmpty() || profile.appearanceLeaders.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_records))
            TeamRecords(profile, onPlayerClick)
        }
        if (profile.honors.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_honors))
            TeamHonors(profile)
        }
    }

    if (showStatistics) SectionContainer(
        state = statisticsState,
        onRetry = onRetry,
        forceRetry = true,
        title = stringResource(R.string.team_statistics),
        emptyTitle = stringResource(R.string.team_stats_empty_title),
        emptyDescription = stringResource(R.string.team_stats_empty_description),
    ) { statistics ->
        SeasonPicker(
            statistics.seasons,
            statistics.selectedSeasonId,
            onStatisticsSeasonSelect,
        )
        TeamStatisticsContent(statistics, onPlayerClick)
    }

    if (showTransfers) SectionContainer(
        state = transfersState,
        onRetry = onRetry,
        forceRetry = true,
        title = stringResource(R.string.team_transfers),
        emptyTitle = stringResource(R.string.team_transfers_empty_title),
        emptyDescription = stringResource(R.string.team_transfers_empty_description),
    ) { transfers ->
        val seasons = transfers.windows.map {
            SeasonOption(it.id, it.label, it.isCurrent)
        }
        SeasonPicker(seasons, transfers.selectedWindowId, onTransferWindowSelect)
        TeamTransferList(transfers, onPlayerClick, onTeamClick)
    }

}

@Composable
private fun ProfileHeader(profile: TeamProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
        ) {
            TeamCrest(
                teamId = profile.id,
                teamName = profile.name,
                crestUrl = profile.crestUrl,
                size = 60.dp,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                profile.englishName?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val subtitle = listOfNotNull(
                    profile.country,
                    profile.city,
                    profile.competitionName,
                ).joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val ranking = listOfNotNull(
                    profile.rankLabel,
                    profile.marketValueLabel?.let {
                        "${stringResource(R.string.team_fact_value)}$it"
                    },
                )
                    .distinct()
                    .joinToString(" · ")
                if (ranking.isNotEmpty()) {
                    Text(
                        text = ranking,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
        val summaries = listOfNotNull(
            profile.leagueRankLabel?.let {
                stringResource(R.string.team_stat_rank) to it
            },
            profile.leagueRecordLabel?.let {
                stringResource(R.string.team_stat_record) to it
            },
        )
        if (summaries.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                summaries.forEach { (label, value) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(value, style = DqdTheme.dataText.statValue)
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (profile.recentForm.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.team_recent_form),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = DqdSpacing.sm),
                )
                profile.recentForm.forEach { result ->
                    FormBadge(result, Modifier.padding(horizontal = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun TeamFacts(profile: TeamProfile) {
    val fallbackFacts = listOfNotNull(
        profile.foundedLabel?.let { stringResource(R.string.team_fact_founded) to it },
        profile.venue?.let { venue ->
            stringResource(R.string.team_fact_stadium) to listOfNotNull(
                venue,
                profile.venueCapacity?.let { stringResource(R.string.team_capacity, it) },
            ).joinToString(" · ")
        },
    )
    val facts = profile.facts.map { it.label to it.value }.ifEmpty { fallbackFacts }
    facts.forEach { (label, value) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(72.dp),
            )
            Text(text = value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun TeamStatisticsContent(statistics: TeamStatistics, onPlayerClick: (PlayerId) -> Unit) {
    val summaries = listOfNotNull(
        statistics.rankLabel?.let { stringResource(R.string.team_stat_rank) to it },
        statistics.recordLabel?.let { stringResource(R.string.team_stat_record) to it },
    )
    if (summaries.isNotEmpty() || statistics.rankingTrend.isNotEmpty()) {
        SectionHeader(stringResource(R.string.team_league_data))
    }
    if (summaries.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            summaries.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f).padding(DqdSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(value, style = DqdTheme.dataText.scoreMedium)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    if (statistics.rankingTrend.isNotEmpty()) {
        RankingChart(statistics.rankingTrend.map { it.weekLabel to it.rank })
        RankingTrendSnapshot(statistics.rankingTrend.last())
    }
    statistics.categories.forEach { category ->
        SectionHeader(category.name)
        TeamStatsGrid(category.values, columns = 4)
    }
    statistics.characteristics?.let {
        SectionHeader(stringResource(R.string.team_tactical_characteristics))
        TeamCharacteristicsContent(it)
    }
    if (statistics.keyPlayers.isNotEmpty()) {
        SectionHeader(stringResource(R.string.team_key_players))
        statistics.keyPlayers.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                rowItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPlayerClick(item.player.id) }
                            .padding(vertical = DqdSpacing.sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        PlayerAvatar(item.player.id, item.player.name, item.player.avatarUrl, 42.dp)
                        Text(
                            item.player.name,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        ValueText(item.value, style = DqdTheme.dataText.tableCellStrong)
                        Text(
                            item.metric,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                repeat(4 - rowItems.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RankingTrendSnapshot(point: TeamRankingTrendPoint) {
    val match = point.match ?: return
    Column(
        Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = listOfNotNull(
                stringResource(R.string.team_ranking_value, point.rank),
                stringResource(R.string.team_round_value, point.weekLabel),
                point.dateLabel,
            ).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            Text(match.home.name, style = MaterialTheme.typography.labelSmall)
            TeamCrest(
                teamId = match.home.id,
                teamName = match.home.name,
                crestUrl = match.home.crestUrl,
                size = 24.dp,
            )
            Text(
                text = if (match.homeScore != null && match.awayScore != null) {
                    "${match.homeScore}-${match.awayScore}"
                } else {
                    "—"
                },
                style = DqdTheme.dataText.tableCellStrong,
            )
            TeamCrest(
                teamId = match.away.id,
                teamName = match.away.name,
                crestUrl = match.away.crestUrl,
                size = 24.dp,
            )
            Text(match.away.name, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TeamCharacteristicsContent(characteristics: FootballCharacteristics) {
    val rows = listOf(
        stringResource(R.string.team_styles) to characteristics.styles,
        stringResource(R.string.team_very_strong) to characteristics.veryStrong,
        stringResource(R.string.team_strong) to characteristics.strong,
        stringResource(R.string.team_weak) to characteristics.weak,
        stringResource(R.string.team_very_weak) to characteristics.veryWeak,
    ).filter { it.second.isNotEmpty() }
    rows.forEach { (label, values) ->
        Row(
            Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(58.dp),
            )
            Text(values.joinToString("、"), style = MaterialTheme.typography.bodySmall)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RankingChart(
    values: List<Pair<String, Int>>,
    maxRank: Int? = null,
) {
    if (values.isEmpty()) return
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val ceiling = (maxRank ?: values.maxOf { it.second }).coerceAtLeast(1)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(148.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = DqdSpacing.md, vertical = DqdSpacing.lg),
    ) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        val path = Path()
        values.forEachIndexed { index, (_, rank) ->
            val x = if (values.size == 1) size.width / 2 else size.width * index / values.lastIndex
            val y = if (ceiling == 1) size.height / 2 else size.height * (rank - 1) / (ceiling - 1)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(lineColor, 3.dp.toPx(), Offset(x, y))
        }
        drawPath(path, lineColor, style = Stroke(2.dp.toPx()))
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.listHorizontal, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOfNotNull(values.firstOrNull(), values.getOrNull(values.size / 2), values.lastOrNull())
            .distinctBy { it.first }
            .forEach { (label, rank) ->
                Text(
                    "$label  第${rank}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
    }
}

@Composable
private fun TeamRecords(profile: TeamProfile, onPlayerClick: (PlayerId) -> Unit) {
    var scorersSelected by remember { mutableStateOf(profile.topScorers.isNotEmpty()) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        if (profile.topScorers.isNotEmpty()) {
            ChoiceChip(
                label = stringResource(R.string.team_record_scorers),
                selected = scorersSelected,
                onClick = { scorersSelected = true },
                modifier = Modifier.weight(1f),
                centered = true,
            )
        }
        if (profile.appearanceLeaders.isNotEmpty()) {
            ChoiceChip(
                label = stringResource(R.string.team_record_appearances),
                selected = !scorersSelected,
                onClick = { scorersSelected = false },
                modifier = Modifier.weight(1f),
                centered = true,
            )
        }
    }
    val entries = if (scorersSelected) profile.topScorers else profile.appearanceLeaders
    entries.forEach { entry -> TeamRecordRow(entry, onPlayerClick) }
}

@Composable
private fun TeamRecordRow(entry: TeamRecordEntry, onPlayerClick: (PlayerId) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick(entry.player.id) }
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(entry.rank.toString(), style = DqdTheme.dataText.tableCell, modifier = Modifier.width(22.dp))
        PlayerAvatar(entry.player.id, entry.player.name, entry.player.avatarUrl, 34.dp)
        Column(Modifier.weight(1f)) {
            Text(entry.player.name, style = MaterialTheme.typography.bodySmall)
            Text(
                listOfNotNull(entry.birthdayLabel, entry.nationality).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(entry.countLabel, style = DqdTheme.dataText.tableCellStrong)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun TeamHonors(profile: TeamProfile) {
    profile.honors.forEach { honor ->
        Column(Modifier.fillMaxWidth().padding(DqdSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(honor.name, style = MaterialTheme.typography.bodySmall)
                ValueText(honor.timesLabel, style = DqdTheme.dataText.statValue)
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = DqdSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
            ) {
                honor.seasons.ifEmpty { listOf("") }.forEach { season ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ImagePlaceholder(
                            url = honor.imageUrl,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(44.dp),
                        )
                        if (season.isNotEmpty()) Text(season, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun HistoricalCoachRow(coach: HistoricalCoach, onPlayerClick: (PlayerId) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick(coach.player.id) }
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        PlayerAvatar(coach.player.id, coach.player.name, coach.player.avatarUrl, 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(coach.player.name, style = MaterialTheme.typography.bodySmall)
            coach.durationLabel?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                listOfNotNull(coach.startDate, coach.endDate).joinToString(" - "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            coach.recordLabel?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            coach.winRateLabel?.let {
                Text(
                    stringResource(R.string.team_win_rate, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TeamTransferList(
    data: TeamTransferData,
    onPlayerClick: (PlayerId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
) {
    if (data.groups.isEmpty()) {
        InlineEmpty(stringResource(R.string.team_transfers_window_empty))
        return
    }
    data.groups.forEach { group ->
        SectionHeader(group.title)
        group.entries.forEach { entry -> TransferRow(entry, onPlayerClick, onTeamClick) }
    }
}

@Composable
private fun TransferRow(
    entry: TeamTransferEntry,
    onPlayerClick: (PlayerId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick(entry.player.id) }
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        PlayerAvatar(entry.player.id, entry.player.name, entry.player.avatarUrl, 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.player.name, style = MaterialTheme.typography.bodySmall)
            Text(
                listOfNotNull(entry.ageLabel, entry.roleLabel, entry.nationality).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val counterpart = entry.fromTeam ?: entry.toTeam
            counterpart?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onTeamClick(it.id) },
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            ValueText(entry.feeLabel, style = DqdTheme.dataText.tableCellStrong)
            entry.dateLabel?.let {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SeasonPicker(
    options: List<SeasonOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    if (options.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val selectedIndex = options.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 } ?: 0
    val selected = options[selectedIndex]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onClick = { options.getOrNull(selectedIndex + 1)?.let { onSelect(it.id) } },
            enabled = selectedIndex < options.lastIndex,
        ) {
            Icon(
                painterResource(DqdIcons.ChevronRight),
                contentDescription = "上一赛季",
                modifier = Modifier.rotate(180f),
            )
        }
        Box {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(selected.label, style = MaterialTheme.typography.labelLarge)
                Icon(
                    painterResource(DqdIcons.ChevronRight),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).rotate(90f),
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            expanded = false
                            onSelect(option.id)
                        },
                    )
                }
            }
        }
        IconButton(
            onClick = { options.getOrNull(selectedIndex - 1)?.let { onSelect(it.id) } },
            enabled = selectedIndex > 0,
        ) {
            Icon(
                painterResource(DqdIcons.ChevronRight),
                contentDescription = "下一赛季",
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.md, vertical = 7.dp),
    )
}

@Composable
private fun Pagination(page: Int, pages: Int, onPageSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onPageSelect(page - 1) }, enabled = page > 1) {
            Icon(
                painterResource(DqdIcons.ArrowBack),
                contentDescription = stringResource(R.string.team_previous_page),
            )
        }
        Text(stringResource(R.string.team_page_number, page, pages), style = DqdTheme.dataText.tableCell)
        IconButton(onClick = { onPageSelect(page + 1) }, enabled = page < pages) {
            Icon(
                painterResource(DqdIcons.ChevronRight),
                contentDescription = stringResource(R.string.team_next_page),
            )
        }
    }
}

@Composable
private fun InlineEmpty(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.lg),
    )
}

@Composable
private fun ProfileHeaderSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)))
        Column(verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm)) {
            SkeletonBox(Modifier.width(150.dp).height(22.dp))
            SkeletonBox(Modifier.width(105.dp).height(14.dp))
        }
    }
}
