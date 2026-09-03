package io.github.chos1n11111.dongqiudipure.feature.entities

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchRow
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionHeader
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.labelRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.MarketValuePoint
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHeatMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHonor
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerInjury
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPage
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPerformance
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShotMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticScope
import io.github.chos1n11111.dongqiudipure.core.model.PlayerTransfer
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import kotlin.math.max

@Composable
fun PlayerProfileRoute(
    playerId: PlayerId,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(playerId) { viewModel.load(playerId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onArticleClick = onArticleClick,
        onMatchClick = onMatchClick,
        onTeamClick = onTeamClick,
        onTabSelect = viewModel::selectTab,
        onScopeSelect = viewModel::selectScope,
        onSeasonSelect = viewModel::selectSeason,
        onCompetitionSelect = viewModel::selectCompetition,
        onTeamSelect = viewModel::selectTeam,
        onMatchesPageSelect = viewModel::selectMatchesPage,
        onShotMatchSelect = viewModel::selectShotMatch,
        onRetry = viewModel::retryAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    uiState: PlayerProfileUiState,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onTabSelect: (PlayerTab) -> Unit,
    onScopeSelect: (PlayerStatisticScope) -> Unit,
    onSeasonSelect: (String) -> Unit,
    onCompetitionSelect: (CompetitionId?) -> Unit,
    onTeamSelect: (TeamId) -> Unit,
    onMatchesPageSelect: (Int) -> Unit,
    onShotMatchSelect: (MatchId) -> Unit,
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
                            painterResource(DqdIcons.ArrowBack),
                            stringResource(DesignR.string.ds_action_back),
                            Modifier.size(DqdSize.iconMedium),
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
                loading = { HeaderSkeleton() },
            ) { PlayerHeader(it, onTeamClick) }

            PrimaryScrollableTabRow(
                selectedTabIndex = PlayerTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = DqdSpacing.sm,
            ) {
                PlayerTab.entries.forEach { tab ->
                    val selected = tab == uiState.selectedTab
                    Tab(
                        selected = selected,
                        onClick = { onTabSelect(tab) },
                        text = {
                            Text(
                                stringResource(tab.labelRes),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            when (uiState.selectedTab) {
                PlayerTab.Dynamic -> SectionContainer(
                    state = uiState.news,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.player_news_empty_title),
                    emptyDescription = stringResource(R.string.player_news_empty_description),
                ) { TeamNewsList(it, onArticleClick) }

                PlayerTab.Data -> PlayerDataTab(
                    uiState,
                    onScopeSelect,
                    onSeasonSelect,
                    onCompetitionSelect,
                    onTeamSelect,
                    onRetry,
                )

                PlayerTab.Matches -> PlayerMatchesTab(
                    uiState,
                    onMatchClick,
                    onMatchesPageSelect,
                    onShotMatchSelect,
                    onRetry,
                )

                PlayerTab.Ability -> SectionContainer(
                    state = uiState.ability,
                    onRetry = onRetry,
                    title = stringResource(R.string.player_ability),
                    emptyTitle = stringResource(R.string.player_ability_empty_title),
                    emptyDescription = stringResource(R.string.player_ability_empty_description),
                ) { AbilityContent(it) }

                PlayerTab.Info -> SectionContainer(
                    state = uiState.overview,
                    onRetry = onRetry,
                    emptyTitle = stringResource(R.string.player_profile_empty_title),
                    emptyDescription = stringResource(R.string.player_profile_empty_description),
                ) { PlayerInfoContent(it, onTeamClick) }
            }
            Box(Modifier.height(DqdSpacing.xl))
        }
    }
}

@Composable
private fun PlayerDataTab(
    uiState: PlayerProfileUiState,
    onScopeSelect: (PlayerStatisticScope) -> Unit,
    onSeasonSelect: (String) -> Unit,
    onCompetitionSelect: (CompetitionId?) -> Unit,
    onTeamSelect: (TeamId) -> Unit,
    onRetry: () -> Unit,
) {
    SectionContainer(
        state = uiState.statistics,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.player_season_stats_empty_title),
        emptyDescription = stringResource(R.string.player_season_stats_empty_description),
    ) {
        PlayerDataSelectors(
            uiState,
            onScopeSelect,
            onSeasonSelect,
            onCompetitionSelect,
            onTeamSelect,
        )
        val entry = uiState.selectedEntry
        if (entry == null) {
            InlineEmpty(stringResource(R.string.player_selection_empty))
        } else {
            StatisticEntryContent(entry)
        }
    }
    SectionContainer(
        state = uiState.heatMap,
        onRetry = onRetry,
        title = stringResource(R.string.player_heat_map),
        emptyTitle = stringResource(R.string.player_heat_map_empty_title),
        emptyDescription = stringResource(R.string.player_heat_map_empty_description),
    ) { HeatMap(it) }
    val ratings = (uiState.matches as? SectionState.Content)?.value?.matches.orEmpty()
        .mapNotNull { match -> match.ratingLabel?.toFloatOrNull()?.let { match.match.id.raw to it } }
        .reversed()
    if (ratings.size >= 2) {
        SectionHeader(stringResource(R.string.player_form_trend))
        TrendChart(ratings.map { it.second })
    }
}

@Composable
private fun PlayerDataSelectors(
    state: PlayerProfileUiState,
    onScopeSelect: (PlayerStatisticScope) -> Unit,
    onSeasonSelect: (String) -> Unit,
    onCompetitionSelect: (CompetitionId?) -> Unit,
    onTeamSelect: (TeamId) -> Unit,
) {
    ChoiceRow(
        options = PlayerStatisticScope.entries.map { it.name to stringResource(it.labelRes()) },
        selectedId = state.selectedScope.name,
        onSelect = { raw -> onScopeSelect(PlayerStatisticScope.valueOf(raw)) },
    )
    ChoiceRow(
        options = state.scopeEntries.distinctBy { it.season.id }
            .map { it.season.id to it.season.label },
        selectedId = state.selectedSeasonId,
        onSelect = onSeasonSelect,
    )
    val competitions = state.seasonEntries.mapNotNull { it.competition }.distinctBy { it.id }
    if (competitions.isNotEmpty()) {
        ChoiceRow(
            options = competitions.map { it.id.raw to it.name },
            selectedId = state.selectedCompetitionId?.raw,
            onSelect = { onCompetitionSelect(CompetitionId(it)) },
        )
    }
    ChoiceRow(
        options = state.competitionEntries.map { it.team }.distinctBy { it.id }
            .map { it.id.raw to it.name },
        selectedId = state.selectedTeamId?.raw,
        onSelect = { onTeamSelect(TeamId(it)) },
    )
}

@Composable
private fun StatisticEntryContent(entry: PlayerStatisticEntry) {
    if (entry.summary.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_summary_data))
        TeamStatsGrid(entry.summary)
    }
    entry.sections.forEach { section ->
        SectionHeader(section.name)
        TeamStatsGrid(section.values)
    }
}

@Composable
private fun PlayerMatchesTab(
    uiState: PlayerProfileUiState,
    onMatchClick: (MatchId) -> Unit,
    onPageSelect: (Int) -> Unit,
    onShotMatchSelect: (MatchId) -> Unit,
    onRetry: () -> Unit,
) {
    SectionContainer(
        state = uiState.matches,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.player_matches_empty_title),
        emptyDescription = stringResource(R.string.player_matches_empty_description),
    ) { page ->
        PlayerMatchList(page, onMatchClick)
        if (page.totalPages > 1) {
            EntityPagination(page.page, page.totalPages, onPageSelect)
        }
        if (page.matches.isNotEmpty()) {
            SectionHeader(stringResource(R.string.player_shot_match))
            ChoiceRow(
                options = page.matches.map { performance ->
                    performance.match.id.raw to
                        "${performance.match.home.name}-${performance.match.away.name}"
                },
                selectedId = uiState.selectedShotMatchId?.raw,
                onSelect = { onShotMatchSelect(MatchId(it)) },
            )
        }
    }
    SectionContainer(
        state = uiState.shotMap,
        onRetry = onRetry,
        title = stringResource(R.string.player_shot_map),
        emptyTitle = stringResource(R.string.player_shot_map_empty_title),
        emptyDescription = stringResource(R.string.player_shot_map_empty_description),
    ) { ShotMap(it) }
}

@Composable
private fun PlayerMatchList(page: PlayerMatchPage, onMatchClick: (MatchId) -> Unit) {
    page.matches.forEach { performance ->
        MatchRow(
            match = performance.match,
            onClick = { onMatchClick(performance.match.id) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer),
        )
        PerformanceRow(performance)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PerformanceRow(performance: PlayerMatchPerformance) {
    val values = listOfNotNull(
        performance.minutesLabel?.let { stringResource(R.string.player_minutes_value, it) },
        performance.goals?.let { stringResource(R.string.player_goals_value, it) },
        performance.assists?.let { stringResource(R.string.player_assists_value, it) },
        performance.cardsLabel?.let { stringResource(R.string.player_cards_value, it) },
        performance.ratingLabel?.let { stringResource(R.string.player_rating_value, it) },
        performance.userRatingLabel?.let { stringResource(R.string.player_user_rating_value, it) },
    )
    if (values.isNotEmpty()) {
        Text(
            text = values.joinToString("  ·  "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(start = 64.dp, end = DqdSpacing.listHorizontal, bottom = DqdSpacing.sm),
        )
    }
}

@Composable
private fun HeatMap(data: PlayerHeatMap) {
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    val pointColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .aspectRatio(1.55f),
    ) {
        val stroke = Stroke(1.dp.toPx())
        drawRect(lineColor, style = stroke)
        drawLine(lineColor, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
        drawCircle(lineColor, size.height * 0.14f, center, style = stroke)
        data.points.forEach { point ->
            drawCircle(
                color = pointColor.copy(alpha = 0.10f),
                radius = 9.dp.toPx(),
                center = Offset(size.width * point.x / 100f, size.height * point.y / 100f),
            )
            drawCircle(
                color = pointColor.copy(alpha = 0.26f),
                radius = 3.dp.toPx(),
                center = Offset(size.width * point.x / 100f, size.height * point.y / 100f),
            )
        }
    }
}

@Composable
private fun ShotMap(data: PlayerShotMap) {
    data.summary?.let { summary ->
        val values = listOf(
            stringResource(R.string.player_shots_total) to summary.total?.toString(),
            stringResource(R.string.player_shots_goals) to summary.goals?.toString(),
            stringResource(R.string.player_shots_on_target) to summary.onTarget?.toString(),
            stringResource(R.string.player_shots_xg) to summary.expectedGoalsLabel,
        )
        Row(Modifier.fillMaxWidth()) {
            values.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f).padding(DqdSpacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ValueText(value, style = DqdTheme.dataText.statValue)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    val plotted = data.shots.filter { it.x != null && it.y != null }
    if (plotted.isNotEmpty()) {
        val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        val normalColor = MaterialTheme.colorScheme.primary
        val goalColor = DqdTheme.sports.win
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DqdSpacing.md)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .aspectRatio(1.8f),
        ) {
            drawRect(lineColor, style = Stroke(1.dp.toPx()))
            plotted.forEach { shot ->
                val x = requireNotNull(shot.x).coerceIn(0f, 100f)
                val y = requireNotNull(shot.y).coerceIn(0f, 100f)
                drawCircle(
                    color = if (shot.outcome.orEmpty().contains("进球")) goalColor else normalColor,
                    radius = 5.dp.toPx(),
                    center = Offset(size.width * x / 100f, size.height * y / 100f),
                )
            }
        }
    }
    data.shots.forEach { shot ->
        Text(
            text = listOfNotNull(
                shot.minuteLabel,
                shot.outcome,
                shot.situation,
                shot.shotType,
                shot.expectedGoalsLabel?.let { "xG $it" },
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = DqdSpacing.listHorizontal, vertical = 6.dp),
        )
    }
}

@Composable
private fun TrendChart(values: List<Float>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(DqdSpacing.md),
    ) {
        drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        val min = values.minOrNull() ?: return@Canvas
        val maxValue = values.maxOrNull() ?: return@Canvas
        val range = max(maxValue - min, 0.5f)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2 else size.width * index / (values.size - 1)
            val y = size.height - (value - min) / range * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(lineColor, 3.dp.toPx(), Offset(x, y))
        }
        drawPath(path, lineColor, style = Stroke(2.dp.toPx()))
    }
}

@Composable
private fun PlayerInfoContent(overview: PlayerOverview, onTeamClick: (TeamId) -> Unit) {
    SectionHeader(stringResource(R.string.player_profile_section))
    AttributeGrid(overview.profile)
    if (overview.honors.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_honors))
        HonorList(overview.honors)
    }
    if (overview.transfers.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_transfers))
        TransferList(overview.transfers, onTeamClick)
    }
    SectionHeader(stringResource(R.string.player_injuries))
    if (overview.injuries.isEmpty()) {
        InlineEmpty(stringResource(R.string.player_injuries_empty_description))
    } else {
        InjuryList(overview.injuries)
    }
    SectionHeader(stringResource(R.string.player_market_value_history))
    if (overview.marketValues.isEmpty()) {
        InlineEmpty(stringResource(R.string.player_market_value_empty))
    } else {
        MarketValueChart(overview.marketValues)
    }
}

@Composable
private fun PlayerHeader(profile: PlayerProfile, onTeamClick: (TeamId) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
    ) {
        PlayerAvatar(profile.id, profile.name, profile.avatarUrl, 64.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(profile.name, style = MaterialTheme.typography.headlineSmall)
                profile.shirtNumber?.let {
                    Text(stringResource(R.string.player_shirt_number, it), style = DqdTheme.dataText.statValue)
                }
            }
            profile.englishName?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val team = profile.team
            if (team != null) {
                Row(
                    modifier = Modifier.clickable { onTeamClick(team.id) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TeamCrest(
                        teamId = team.id,
                        teamName = team.name,
                        crestUrl = team.crestUrl,
                        size = 17.dp,
                    )
                    Text(
                        "${team.name} · ${stringResource(profile.position.labelRes())}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                Text(stringResource(profile.position.labelRes()), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun AttributeGrid(profile: PlayerProfile) {
    val values = listOf(
        stringResource(R.string.player_attr_nationality) to profile.nationality,
        stringResource(R.string.player_attr_age) to profile.ageLabel,
        stringResource(R.string.player_attr_birthday) to profile.birthdayLabel,
        stringResource(R.string.player_attr_height) to profile.heightLabel,
        stringResource(R.string.player_attr_weight) to profile.weightLabel,
        stringResource(R.string.player_attr_foot) to profile.footLabel,
        stringResource(R.string.player_attr_value) to profile.marketValueLabel,
        stringResource(R.string.player_attr_contract) to profile.contractUntil,
    )
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer)) {
        values.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth()) {
                pair.forEach { (label, value) ->
                    Column(Modifier.weight(1f).padding(DqdSpacing.md)) {
                        ValueText(value, style = MaterialTheme.typography.bodySmall)
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun AbilityContent(ability: PlayerAbility) {
    Row(
        Modifier.fillMaxWidth().padding(DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        ValueText(ability.overall, style = DqdTheme.dataText.scoreLarge)
        Column {
            Text(stringResource(R.string.player_ability_overall), style = MaterialTheme.typography.labelMedium)
            ability.version?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
    }
    TeamStatsGrid(ability.attributes)
}

@Composable
private fun HonorList(honors: List<PlayerHonor>) {
    honors.forEach { honor ->
        Row(
            Modifier.fillMaxWidth().padding(DqdSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
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
            honor.times?.let { Text(stringResource(R.string.player_honor_times, it)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun TransferList(transfers: List<PlayerTransfer>, onTeamClick: (TeamId) -> Unit) {
    transfers.forEach { transfer ->
        Column(Modifier.fillMaxWidth().padding(DqdSpacing.md)) {
            Text(
                listOfNotNull(transfer.date, transfer.type, transfer.fee).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = DqdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                TransferTeam(transfer.fromTeam, onTeamClick, Modifier.weight(1f))
                Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TransferTeam(transfer.toTeam, onTeamClick, Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun TransferTeam(team: TeamRef?, onClick: (TeamId) -> Unit, modifier: Modifier = Modifier) {
    if (team == null) {
        Box(modifier, contentAlignment = Alignment.Center) { ValueText(null as String?) }
    } else {
        Row(
            modifier.clickable { onClick(team.id) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            TeamCrest(
                teamId = team.id,
                teamName = team.name,
                crestUrl = team.crestUrl,
                size = 22.dp,
            )
            Text(team.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
private fun InjuryList(injuries: List<PlayerInjury>) {
    injuries.forEach { injury ->
        Row(Modifier.fillMaxWidth().padding(DqdSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(injury.type, style = MaterialTheme.typography.bodySmall)
                Text(
                    listOfNotNull(
                        injury.teamName,
                        listOfNotNull(injury.startDate, injury.endDate)
                            .joinToString(" - ")
                            .takeIf { it.isNotEmpty() },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            injury.gamesMissed?.let { Text(stringResource(R.string.player_games_missed, it)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun MarketValueChart(points: List<MarketValuePoint>) {
    val usable = points.filter { it.value != null }
    if (usable.size >= 2) {
        TrendChart(usable.map { requireNotNull(it.value).toFloat() })
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        listOfNotNull(points.firstOrNull(), points.lastOrNull()).distinct().forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(point.valueLabel, style = DqdTheme.dataText.tableCellStrong)
                Text(point.dateLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    if (options.isEmpty()) return
    LazyRow(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(options, key = { it.first }) { (id, label) ->
            val selected = id == selectedId
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = DqdSpacing.md, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun EntityPagination(page: Int, pages: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(DqdSpacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onSelect(page - 1) }, enabled = page > 1) {
            Icon(painterResource(DqdIcons.ArrowBack), stringResource(R.string.team_previous_page))
        }
        Text(stringResource(R.string.team_page_number, page, pages), style = DqdTheme.dataText.tableCell)
        IconButton(onClick = { onSelect(page + 1) }, enabled = page < pages) {
            Icon(painterResource(DqdIcons.ChevronRight), stringResource(R.string.team_next_page))
        }
    }
}

@Composable
private fun InlineEmpty(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(DqdSpacing.lg),
    )
}

@Composable
private fun HeaderSkeleton() {
    Row(
        Modifier.padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(Modifier.size(64.dp), CircleShape)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBox(Modifier.width(110.dp).height(22.dp))
            SkeletonBox(Modifier.width(150.dp).height(13.dp))
        }
    }
}

private fun PlayerStatisticScope.labelRes(): Int = when (this) {
    PlayerStatisticScope.Total -> R.string.player_scope_total
    PlayerStatisticScope.League -> R.string.player_scope_league
    PlayerStatisticScope.Cup -> R.string.player_scope_cup
    PlayerStatisticScope.NationalTeam -> R.string.player_scope_national
}
