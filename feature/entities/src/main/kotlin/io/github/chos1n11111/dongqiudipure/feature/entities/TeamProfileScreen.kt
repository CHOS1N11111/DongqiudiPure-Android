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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.FormBadge
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
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.HistoricalCoach
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
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

    TeamProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onArticleClick = onArticleClick,
        onMatchClick = onMatchClick,
        onTeamClick = onTeamClick,
        onPlayerClick = onPlayerClick,
        onTabSelect = viewModel::selectTab,
        onScheduleFilterSelect = viewModel::selectScheduleFilter,
        onCompetitionSelect = viewModel::selectCompetition,
        onScheduleSeasonSelect = viewModel::selectScheduleSeason,
        onStatisticsSeasonSelect = viewModel::selectStatisticsSeason,
        onTransferWindowSelect = viewModel::selectTransferWindow,
        onSchedulePageSelect = viewModel::selectSchedulePage,
        onRetry = viewModel::retryAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProfileScreen(
    uiState: TeamProfileUiState,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onTabSelect: (TeamTab) -> Unit,
    onScheduleFilterSelect: (TeamScheduleFilter) -> Unit,
    onCompetitionSelect: (CompetitionId?) -> Unit,
    onScheduleSeasonSelect: (String) -> Unit,
    onStatisticsSeasonSelect: (String) -> Unit,
    onTransferWindowSelect: (String) -> Unit,
    onSchedulePageSelect: (Int) -> Unit,
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

            when (uiState.selectedTab) {
                TeamTab.Dynamic -> SectionContainer(
                    state = uiState.news,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.team_news_empty_title),
                    emptyDescription = stringResource(R.string.team_news_empty_description),
                ) { TeamNewsList(it, onArticleClick) }

                TeamTab.Schedule -> SectionContainer(
                    state = uiState.schedule,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.team_fixtures_empty_title),
                    emptyDescription = stringResource(R.string.team_fixtures_empty_description),
                ) { schedule ->
                    TeamScheduleContent(
                        uiState = uiState,
                        seasons = schedule.seasons,
                        selectedSeasonId = schedule.selectedSeasonId,
                        onSeasonSelect = onScheduleSeasonSelect,
                        onCompetitionSelect = onCompetitionSelect,
                        onFilterSelect = onScheduleFilterSelect,
                        onPageSelect = onSchedulePageSelect,
                        onMatchClick = onMatchClick,
                    )
                }

                TeamTab.Players -> SectionContainer(
                    state = uiState.squad,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.team_squad_empty_title),
                    emptyDescription = stringResource(R.string.team_squad_empty_description),
                ) { SquadList(it, onPlayerClick) }

                TeamTab.Data -> TeamInfoContent(
                    profileState = uiState.profile,
                    statisticsState = uiState.statistics,
                    transfersState = uiState.transfers,
                    onStatisticsSeasonSelect = onStatisticsSeasonSelect,
                    onTransferWindowSelect = onTransferWindowSelect,
                    onPlayerClick = onPlayerClick,
                    onTeamClick = onTeamClick,
                    onRetry = onRetry,
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
                    onRetry = onRetry,
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
                    onRetry = onRetry,
                    showProfile = false,
                    showStatistics = false,
                    showTransfers = true,
                )
            }
            Box(modifier = Modifier.height(DqdSpacing.xl))
        }
    }
}

@Composable
private fun TeamScheduleContent(
    uiState: TeamProfileUiState,
    seasons: List<SeasonOption>,
    selectedSeasonId: String?,
    onSeasonSelect: (String) -> Unit,
    onCompetitionSelect: (CompetitionId?) -> Unit,
    onFilterSelect: (TeamScheduleFilter) -> Unit,
    onPageSelect: (Int) -> Unit,
    onMatchClick: (MatchId) -> Unit,
) {
    SeasonPicker(seasons, selectedSeasonId, onSeasonSelect)
    val schedule = (uiState.schedule as? SectionState.Content)?.value
    val competitions = schedule?.matches.orEmpty()
        .map { it.competition }
        .distinctBy { it.id }
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            ChoiceChip(
                label = stringResource(R.string.team_schedule_all_competitions),
                selected = uiState.selectedCompetitionId == null,
                onClick = { onCompetitionSelect(null) },
            )
        }
        items(competitions, key = { it.id.raw }) { competition ->
            ChoiceChip(
                label = competition.name,
                selected = competition.id == uiState.selectedCompetitionId,
                onClick = { onCompetitionSelect(competition.id) },
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        TeamScheduleFilter.entries.forEach { filter ->
            ChoiceChip(
                label = stringResource(filter.labelRes),
                selected = filter == uiState.scheduleFilter,
                onClick = { onFilterSelect(filter) },
                modifier = Modifier.weight(1f),
                centered = true,
            )
        }
    }
    if (uiState.visibleMatches.isEmpty()) {
        InlineEmpty(stringResource(R.string.team_schedule_filter_empty))
    } else {
        TeamFixtureList(uiState.visibleMatches, onMatchClick)
    }
    if (uiState.schedulePageCount > 1) {
        Pagination(
            page = uiState.schedulePage,
            pages = uiState.schedulePageCount,
            onPageSelect = onPageSelect,
        )
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
        title = stringResource(R.string.team_details),
    ) { profile ->
        TeamFacts(profile)
        profile.description?.let { description ->
            SectionHeader(stringResource(R.string.team_description))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(
                    start = DqdSpacing.listHorizontal,
                    end = DqdSpacing.listHorizontal,
                    bottom = DqdSpacing.md,
                ),
            )
        }
        if (profile.honors.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_honors))
            profile.honors.forEach { honor ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(honor.name, style = MaterialTheme.typography.bodySmall)
                        if (honor.seasons.isNotEmpty()) {
                            Text(
                                honor.seasons.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    ValueText(honor.timesLabel, style = DqdTheme.dataText.statValue)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        if (profile.historicalCoaches.isNotEmpty()) {
            SectionHeader(stringResource(R.string.team_historical_coaches))
            profile.historicalCoaches.forEach { coach ->
                HistoricalCoachRow(coach, onPlayerClick)
            }
        }
    }

    if (showStatistics) SectionContainer(
        state = statisticsState,
        onRetry = onRetry,
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

    if (showProfile) {
        SectionHeader(stringResource(R.string.team_availability))
        InlineEmpty(stringResource(R.string.team_availability_unavailable))
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
        profile.marketValueLabel?.let { stringResource(R.string.team_fact_value) to it },
    )
    val facts = (profile.facts.map { it.label to it.value } + fallbackFacts).distinctBy { it.first }
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
    statistics.categories.forEach { category ->
        SectionHeader(category.name)
        TeamStatsGrid(category.values)
    }
    if (statistics.keyPlayers.isNotEmpty()) {
        SectionHeader(stringResource(R.string.team_key_players))
        statistics.keyPlayers.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayerClick(item.player.id) }
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                PlayerAvatar(item.player.id, item.player.name, item.player.avatarUrl, 32.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.player.name, style = MaterialTheme.typography.bodySmall)
                    Text(item.metric, style = MaterialTheme.typography.labelSmall)
                }
                ValueText(item.value, style = DqdTheme.dataText.statValue)
            }
        }
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
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(options, key = { it.id }) { option ->
            ChoiceChip(
                label = option.label,
                selected = option.id == selectedId,
                onClick = { onSelect(option.id) },
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
