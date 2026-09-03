package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchStatusBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MissingValue
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.Absentee
import io.github.chos1n11111.dongqiudipure.core.model.AnalysisMatch
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayer
import io.github.chos1n11111.dongqiudipure.core.model.MatchAnalysis
import io.github.chos1n11111.dongqiudipure.core.model.MatchArticle
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchEventKind
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchInfo
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineupBundle
import io.github.chos1n11111.dongqiudipure.core.model.MatchMomentumPoint
import io.github.chos1n11111.dongqiudipure.core.model.MatchOverview
import io.github.chos1n11111.dongqiudipure.core.model.MatchRating
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.contentOrNull
import io.github.chos1n11111.dongqiudipure.core.model.hasScore
import kotlin.math.abs

@Composable
fun MatchDetailRoute(
    matchId: MatchId,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(matchId) { viewModel.load(matchId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onTeamClick = onTeamClick,
        onPlayerClick = onPlayerClick,
        onArticleClick = onArticleClick,
        onMatchClick = onMatchClick,
        onRetryHeader = viewModel::retryHeader,
        onRetryOverview = viewModel::retryOverview,
        onRetryLineup = viewModel::retryLineup,
        onRetryAnalysis = viewModel::retryAnalysis,
        onTabSelect = viewModel::selectTab,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    uiState: MatchDetailUiState,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onRetryHeader: () -> Unit,
    onRetryOverview: () -> Unit,
    onRetryLineup: () -> Unit,
    onRetryAnalysis: () -> Unit,
    onTabSelect: (MatchTab) -> Unit,
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
                state = uiState.header,
                onRetry = onRetryHeader,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                loading = { ScoreHeaderSkeleton() },
            ) { match ->
                ScoreHeader(match = match, onTeamClick = onTeamClick)
            }

            PrimaryTabRow(
                selectedTabIndex = MatchTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                MatchTab.entries.forEach { tab ->
                    val selected = tab == uiState.selectedTab
                    Tab(
                        selected = selected,
                        onClick = { onTabSelect(tab) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }

            when (uiState.selectedTab) {
                MatchTab.Ratings -> RatingsTab(
                    lineupState = uiState.lineup,
                    overviewState = uiState.overview,
                    userRatings = uiState.userRatings,
                    onRetry = onRetryLineup,
                    onPlayerClick = onPlayerClick,
                )
                MatchTab.Situation -> SituationTab(
                    overviewState = uiState.overview,
                    lineup = uiState.lineup.contentOrNull(),
                    onRetry = onRetryOverview,
                    onArticleClick = onArticleClick,
                )
                MatchTab.Lineup -> LineupTab(
                    state = uiState.lineup,
                    onRetry = onRetryLineup,
                    onPlayerClick = onPlayerClick,
                )
                MatchTab.Intelligence -> IntelligenceTab(
                    lineupState = uiState.lineup,
                    analysis = uiState.analysis.contentOrNull(),
                    onRetry = onRetryLineup,
                    onPlayerClick = onPlayerClick,
                )
                MatchTab.Analysis -> AnalysisTab(
                    state = uiState.analysis,
                    onRetry = onRetryAnalysis,
                    onMatchClick = onMatchClick,
                )
            }
        }
    }
}

@Composable
private fun RatingsTab(
    lineupState: SectionState<MatchLineupBundle>,
    overviewState: SectionState<MatchOverview>,
    userRatings: Map<PlayerId, String>,
    onRetry: () -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val actual = lineupState.contentOrNull()?.actual
    val ratedPlayers = actual?.let { lineup ->
        listOf(lineup.home, lineup.away).flatMap { team ->
            (team.starters + team.substitutes)
                .filter { it.ratingLabel != null }
                .map { player -> team.team.name to player }
        }.sortedByDescending { it.second.ratingLabel?.toFloatOrNull() }
    }.orEmpty()

    if (ratedPlayers.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_official_ratings_title))
        Text(
            text = stringResource(R.string.match_ratings_read_only),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = DqdSpacing.listHorizontal,
                end = DqdSpacing.listHorizontal,
                bottom = DqdSpacing.sm,
            ),
        )
        ratedPlayers.forEach { (teamName, player) ->
            PlayerRatingRow(
                player = player,
                teamName = teamName,
                onClick = { onPlayerClick(player.id) },
            )
        }
        SectionHeader(title = stringResource(R.string.match_user_ratings_title))
        val userRatedPlayers = ratedPlayers.filter { (_, player) -> userRatings[player.id] != null }
        if (userRatedPlayers.isEmpty()) {
            Text(
                text = stringResource(R.string.match_user_ratings_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = DqdSpacing.listHorizontal,
                    vertical = DqdSpacing.md,
                ),
            )
        } else {
            userRatedPlayers.forEach { (teamName, player) ->
                UserRatingRow(
                    player = player,
                    teamName = teamName,
                    ratingLabel = requireNotNull(userRatings[player.id]),
                    onClick = { onPlayerClick(player.id) },
                )
            }
        }
    } else {
        SectionContainer(
            state = overviewState,
            onRetry = onRetry,
            emptyTitle = stringResource(R.string.match_ratings_empty_title),
            emptyDescription = stringResource(R.string.match_ratings_empty_description),
            loading = { RatingsSkeleton() },
        ) { overview ->
            if (overview.topRatings.isEmpty()) {
                InlineEmpty(
                    title = stringResource(R.string.match_ratings_empty_title),
                    description = stringResource(R.string.match_ratings_empty_description),
                )
            } else {
                SectionHeader(title = stringResource(R.string.match_ratings_title))
                overview.topRatings.forEach { rating ->
                    MatchRatingRow(rating, onPlayerClick)
                }
            }
        }
    }
}

@Composable
private fun PlayerRatingRow(
    player: LineupPlayer,
    teamName: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        PlayerAvatar(player.id, player.name, player.avatarUrl, 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(player.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                teamName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (player.isMvp) {
            Text(
                stringResource(R.string.match_rating_mvp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            player.ratingLabel.orEmpty(),
            style = DqdTheme.dataText.statValue,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun UserRatingRow(
    player: LineupPlayer,
    teamName: String,
    ratingLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        PlayerAvatar(player.id, player.name, player.avatarUrl, 38.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(player.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                teamName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            ratingLabel,
            style = DqdTheme.dataText.statValue,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun MatchRatingRow(rating: MatchRating, onPlayerClick: (PlayerId) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick(rating.player.id) }
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        PlayerAvatar(
            rating.player.id,
            rating.player.name,
            rating.player.avatarUrl,
            38.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(rating.player.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                rating.team.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (rating.isMvp) {
            Text(
                stringResource(R.string.match_rating_mvp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            rating.ratingLabel,
            style = DqdTheme.dataText.statValue,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SituationTab(
    overviewState: SectionState<MatchOverview>,
    lineup: MatchLineupBundle?,
    onRetry: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
) {
    SectionContainer(
        state = overviewState,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.match_situation_empty_title),
        emptyDescription = stringResource(R.string.match_situation_empty_description),
        loading = { EventsSkeleton() },
    ) { overview ->
        SituationContent(overview, onArticleClick)
    }
    lineup?.info?.takeIf { it.hasDisplayValue() }?.let { info ->
        SectionHeader(title = stringResource(R.string.match_info_title))
        MatchInfoContent(info)
    }
}

@Composable
private fun SituationContent(overview: MatchOverview, onArticleClick: (ArticleId) -> Unit) {
    val hasContent = overview.events.isNotEmpty() || overview.statistics.isNotEmpty() ||
        overview.momentum.isNotEmpty() || overview.report != null || overview.highlights.isNotEmpty() ||
        overview.relatedNews.isNotEmpty()
    if (!hasContent) {
        InlineEmpty(
            title = stringResource(R.string.match_situation_empty_title),
            description = stringResource(R.string.match_situation_empty_description),
        )
        return
    }

    if (overview.events.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_events_title))
        EventTimeline(overview.events)
    }
    if (overview.statistics.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_stats_title))
        overview.statistics.forEach { StatRow(it) }
    }
    if (overview.momentum.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_momentum_title))
        MomentumChart(overview.momentum)
    }
    overview.report?.let { report ->
        SectionHeader(title = stringResource(R.string.match_report_title))
        MatchArticleRow(report) { onArticleClick(report.articleId) }
    }
    if (overview.highlights.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_highlights_title))
        overview.highlights.forEach { item ->
            MatchArticleRow(item) { onArticleClick(item.articleId) }
        }
    }
    if (overview.relatedNews.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_related_news_title))
        overview.relatedNews.forEach { item ->
            MatchArticleRow(item) { onArticleClick(item.articleId) }
        }
    }
}

@Composable
private fun LineupTab(
    state: SectionState<MatchLineupBundle>,
    onRetry: () -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val actualState: SectionState<MatchLineup> = when (state) {
        SectionState.Loading -> SectionState.Loading
        SectionState.Empty -> SectionState.Empty
        is SectionState.Failed -> SectionState.Failed(state.error)
        is SectionState.Content -> state.value.actual?.let { SectionState.Content(it) }
            ?: SectionState.Empty
    }
    SectionContainer(
        state = actualState,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.match_lineup_empty_title),
        emptyDescription = stringResource(R.string.match_lineup_empty_description),
        loading = { LineupSkeleton() },
    ) { lineup ->
        LineupContent(lineup, onPlayerClick)
    }
}

@Composable
private fun IntelligenceTab(
    lineupState: SectionState<MatchLineupBundle>,
    analysis: MatchAnalysis?,
    onRetry: () -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
) {
    val forecastState: SectionState<MatchLineup> = when (lineupState) {
        SectionState.Loading -> SectionState.Loading
        SectionState.Empty -> SectionState.Empty
        is SectionState.Failed -> SectionState.Failed(lineupState.error)
        is SectionState.Content -> lineupState.value.forecast?.let { SectionState.Content(it) }
            ?: SectionState.Empty
    }
    SectionContainer(
        state = forecastState,
        onRetry = onRetry,
        title = stringResource(R.string.match_forecast_title),
        emptyTitle = stringResource(R.string.match_forecast_empty_title),
        emptyDescription = stringResource(R.string.match_forecast_empty_description),
        loading = { LineupSkeleton() },
    ) { lineup ->
        LineupContent(lineup, onPlayerClick)
    }

    val bundle = lineupState.contentOrNull()
    val homeAbsentees = (bundle?.actual?.home?.absentees.orEmpty() +
        bundle?.forecast?.home?.absentees.orEmpty() + analysis?.homeAbsentees.orEmpty())
        .distinctBy { it.name to it.reason }
    val awayAbsentees = (bundle?.actual?.away?.absentees.orEmpty() +
        bundle?.forecast?.away?.absentees.orEmpty() + analysis?.awayAbsentees.orEmpty())
        .distinctBy { it.name to it.reason }
    if (homeAbsentees.isNotEmpty() || awayAbsentees.isNotEmpty()) {
        SectionHeader(title = stringResource(R.string.match_absences_title))
        if (homeAbsentees.isNotEmpty()) {
            AbsenteeGroup(
                bundle?.actual?.home?.team?.name ?: bundle?.forecast?.home?.team?.name,
                homeAbsentees,
            )
        }
        if (awayAbsentees.isNotEmpty()) {
            AbsenteeGroup(
                bundle?.actual?.away?.team?.name ?: bundle?.forecast?.away?.team?.name,
                awayAbsentees,
            )
        }
    }
}

@Composable
private fun AbsenteeGroup(teamName: String?, absentees: List<Absentee>) {
    teamName?.let {
        Text(
            it,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        )
    }
    absentees.forEach { absentee ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        ) {
            Text(absentee.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            absentee.reason?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } ?: MissingValue(style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AnalysisTab(
    state: SectionState<MatchAnalysis>,
    onRetry: () -> Unit,
    onMatchClick: (MatchId) -> Unit,
) {
    SectionContainer(
        state = state,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.match_analysis_empty_title),
        emptyDescription = stringResource(R.string.match_analysis_empty_description),
        loading = { AnalysisSkeleton() },
    ) { analysis ->
        AnalysisContent(analysis, onMatchClick)
    }
}

@Composable
private fun AnalysisContent(analysis: MatchAnalysis, onMatchClick: (MatchId) -> Unit) {
    if (analysis.headToHead.isNotEmpty()) {
        SectionHeader(analysis.headToHeadTitle ?: stringResource(R.string.match_head_to_head_title))
        analysis.headToHead.take(10).forEach { AnalysisMatchRow(it, onMatchClick) }
    }
    if (analysis.homeRecent.isNotEmpty() || analysis.awayRecent.isNotEmpty()) {
        SectionHeader(analysis.recentTitle ?: stringResource(R.string.match_recent_title))
        (analysis.homeRecent.take(5) + analysis.awayRecent.take(5)).forEach {
            AnalysisMatchRow(it, onMatchClick)
        }
    }
    if (analysis.homeFuture.isNotEmpty() || analysis.awayFuture.isNotEmpty()) {
        SectionHeader(analysis.futureTitle ?: stringResource(R.string.match_future_title))
        (analysis.homeFuture + analysis.awayFuture).forEach { AnalysisMatchRow(it, onMatchClick) }
    }
}

@Composable
private fun AnalysisMatchRow(item: AnalysisMatch, onMatchClick: (MatchId) -> Unit) {
    val clickable = item.matchId != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (clickable) Modifier.clickable { onMatchClick(requireNotNull(item.matchId)) } else Modifier)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Column(modifier = Modifier.width(66.dp)) {
            Text(
                item.dateLabel.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.competitionName.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            item.homeName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        item.homeTeamId?.let {
            TeamCrest(it, item.homeName, crestUrl = item.homeLogoUrl, size = DqdSize.crestSmall)
        }
        Text(
            item.scoreLabel ?: "–",
            style = DqdTheme.dataText.tableCell,
            modifier = Modifier.width(38.dp),
            textAlign = TextAlign.Center,
        )
        item.awayTeamId?.let {
            TeamCrest(it, item.awayName, crestUrl = item.awayLogoUrl, size = DqdSize.crestSmall)
        }
        Text(
            item.awayName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun MatchArticleRow(item: MatchArticle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val metadata = listOfNotNull(
                item.minuteLabel,
                item.scoreLabel,
                item.commentCount?.let { stringResource(R.string.match_comments_count, it) },
            ).joinToString(" · ")
            if (metadata.isNotEmpty()) {
                Text(
                    metadata,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            painter = painterResource(DqdIcons.ChevronRight),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun MomentumChart(points: List<MatchMomentumPoint>) {
    val sorted = points.sortedBy { it.minute }
    val maxMinute = sorted.maxOfOrNull { it.minute }?.coerceAtLeast(1) ?: 1
    val maxValue = sorted.maxOfOrNull { abs(it.value) }?.coerceAtLeast(1f) ?: 1f
    val lineColor = MaterialTheme.colorScheme.primary
    val centerColor = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(132.dp)) {
            val centerY = size.height / 2f
            drawLine(centerColor, Offset(0f, centerY), Offset(size.width, centerY), 1.dp.toPx())
            if (sorted.size > 1) {
                val path = Path()
                sorted.forEachIndexed { index, point ->
                    val x = point.minute.toFloat() / maxMinute * size.width
                    val y = centerY - point.value / maxValue * centerY * 0.88f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("0'", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "$maxMinute'",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MatchInfoContent(info: MatchInfo) {
    val rows = listOf(
        stringResource(R.string.match_info_venue) to info.venue,
        stringResource(R.string.match_info_referee) to info.referee,
        stringResource(R.string.match_info_weather) to listOfNotNull(info.weather, info.temperature)
            .joinToString(" ").ifEmpty { null },
        stringResource(R.string.match_info_attendance) to info.attendance,
    )
    rows.forEach { (label, value) ->
        if (value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(72.dp),
                )
                Text(value, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun MatchInfo.hasDisplayValue(): Boolean =
    venue != null || referee != null || weather != null || temperature != null || attendance != null

@Composable
private fun ScoreHeader(match: MatchSummary, onTeamClick: (TeamId) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = DqdSpacing.lg, end = DqdSpacing.lg, bottom = DqdSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Text(
            text = listOfNotNull(match.competition.name, match.competition.roundLabel).joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TeamSide(match, true, { onTeamClick(match.home.id) }, Modifier.weight(1f))
            Column(
                modifier = Modifier.width(110.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                if (match.status.hasScore) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ValueText(match.homeScore, style = DqdTheme.dataText.scoreLarge)
                        Text(" – ", style = DqdTheme.dataText.scoreLarge)
                        ValueText(match.awayScore, style = DqdTheme.dataText.scoreLarge)
                    }
                } else {
                    Text("–", style = DqdTheme.dataText.scoreLarge)
                }
                MatchStatusBadge(match.status)
            }
            TeamSide(match, false, { onTeamClick(match.away.id) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TeamSide(
    match: MatchSummary,
    isHome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val team = if (isHome) match.home else match.away
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = DqdSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        TeamCrest(team.id, team.name, crestUrl = team.crestUrl, size = DqdSize.crestLarge)
        Text(
            team.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EventTimeline(events: List<MatchEvent>) {
    Column(modifier = Modifier.padding(vertical = DqdSpacing.sm)) {
        events.forEach { EventRow(it) }
    }
}

@Composable
private fun EventRow(event: MatchEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            event.minuteLabel,
            style = DqdTheme.dataText.minuteLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(38.dp),
        )
        EventIcon(event.kind)
        Column(modifier = Modifier.weight(1f)) {
            Text(event.primaryName, style = MaterialTheme.typography.bodySmall)
            Text(
                event.secondaryName ?: eventKindLabel(event.kind),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        event.scoreAfter?.let {
            Text(it, style = DqdTheme.dataText.statValue)
        }
    }
}

@Composable
private fun EventIcon(kind: MatchEventKind) {
    val tint = when (kind) {
        MatchEventKind.Goal, MatchEventKind.PenaltyGoal -> DqdTheme.sports.win
        MatchEventKind.OwnGoal -> DqdTheme.sports.loss
        MatchEventKind.YellowCard -> DqdTheme.sports.yellowCard
        MatchEventKind.RedCard, MatchEventKind.SecondYellow -> DqdTheme.sports.redCard
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier.size(22.dp).clip(CircleShape).background(tint.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            MatchEventKind.YellowCard, MatchEventKind.RedCard, MatchEventKind.SecondYellow -> Box(
                Modifier.size(width = 8.dp, height = 11.dp).clip(RoundedCornerShape(1.dp)).background(tint),
            )
            MatchEventKind.Substitution -> Icon(
                painterResource(DqdIcons.Substitution), null, tint = tint, modifier = Modifier.size(13.dp),
            )
            else -> Icon(
                painterResource(DqdIcons.Ball), null, tint = tint, modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun eventKindLabel(kind: MatchEventKind): String = when (kind) {
    MatchEventKind.Goal -> stringResource(R.string.match_event_goal)
    MatchEventKind.OwnGoal -> stringResource(R.string.match_event_own_goal)
    MatchEventKind.PenaltyGoal -> stringResource(R.string.match_event_penalty)
    MatchEventKind.YellowCard -> stringResource(R.string.match_event_yellow_card)
    MatchEventKind.RedCard -> stringResource(R.string.match_event_red_card)
    MatchEventKind.SecondYellow -> stringResource(R.string.match_event_second_yellow)
    MatchEventKind.Substitution -> stringResource(R.string.match_event_substitution)
    MatchEventKind.VarReview -> stringResource(R.string.match_event_var)
    is MatchEventKind.Unknown -> kind.rawValue
}

@Composable
private fun StatRow(stat: StatItem) {
    val homeFraction = stat.homeFraction
    val awayFraction = stat.awayFraction
    val total = ((homeFraction ?: 0f) + (awayFraction ?: 0f)).takeIf { it > 0f }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ValueText(stat.homeValue, style = DqdTheme.dataText.statValue)
            Text(
                stat.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            ValueText(stat.awayValue, style = DqdTheme.dataText.statValue)
        }
        if (homeFraction != null && awayFraction != null && total != null) {
            Row(
                modifier = Modifier.fillMaxWidth().height(5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Box(
                    Modifier.weight((homeFraction / total).coerceAtLeast(0.001f))
                        .fillMaxSize().clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Box(
                    Modifier.weight((awayFraction / total).coerceAtLeast(0.001f))
                        .fillMaxSize().clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        } else {
            DashedTrack()
        }
    }
}

@Composable
private fun DashedTrack() {
    Row(
        modifier = Modifier.fillMaxWidth().height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(24) {
            Box(
                Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
    }
}

@Composable
private fun InlineEmpty(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Icon(
            painterResource(DqdIcons.Inbox),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScoreHeaderSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(2) { index ->
            if (index == 1) SkeletonBox(Modifier.width(96.dp).height(40.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                SkeletonBox(Modifier.size(DqdSize.crestLarge), CircleShape)
                SkeletonBox(Modifier.width(56.dp).height(13.dp))
            }
        }
    }
}

@Composable
private fun EventsSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(5) {
            Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md)) {
                SkeletonBox(Modifier.width(38.dp).height(16.dp))
                SkeletonBox(Modifier.size(22.dp), CircleShape)
                SkeletonBox(Modifier.fillMaxWidth(0.55f).height(16.dp))
            }
        }
    }
}

@Composable
private fun RatingsSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(5) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(Modifier.size(38.dp), CircleShape)
                SkeletonBox(Modifier.weight(1f).height(16.dp))
                SkeletonBox(Modifier.width(30.dp).height(18.dp))
            }
        }
    }
}

@Composable
private fun LineupSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(36.dp))
        SkeletonBox(Modifier.fillMaxWidth().aspectRatio(0.74f))
    }
}

@Composable
private fun AnalysisSkeleton() {
    Column(
        modifier = Modifier.padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(6) { SkeletonBox(Modifier.fillMaxWidth().height(42.dp)) }
    }
}

@Preview(name = "比赛详情 · 深色", showBackground = true)
@Composable
private fun MatchDetailDarkPreview() {
    DqdTheme(darkTheme = true) {
        MatchDetailScreen(
            uiState = MatchDetailUiState(
                header = SectionState.Empty,
                overview = SectionState.Empty,
                lineup = SectionState.Empty,
                analysis = SectionState.Empty,
            ),
            onBack = {},
            onTeamClick = {},
            onPlayerClick = {},
            onArticleClick = {},
            onMatchClick = {},
            onRetryHeader = {},
            onRetryOverview = {},
            onRetryLineup = {},
            onRetryAnalysis = {},
            onTabSelect = {},
        )
    }
}
