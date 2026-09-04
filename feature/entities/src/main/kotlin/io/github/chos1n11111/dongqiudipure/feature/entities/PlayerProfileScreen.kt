package io.github.chos1n11111.dongqiudipure.feature.entities

import android.graphics.Bitmap
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ImagePlaceholder
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
import io.github.chos1n11111.dongqiudipure.core.model.FootballCharacteristics
import io.github.chos1n11111.dongqiudipure.core.model.MarketValuePoint
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerCareerSummary
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
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

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
    val news = viewModel.news.collectAsLazyPagingItems()
    PlayerProfileScreen(
        uiState = uiState,
        news = news,
        onBack = onBack,
        onArticleClick = onArticleClick,
        onMatchClick = onMatchClick,
        onTeamClick = onTeamClick,
        onTabSelect = viewModel::selectTab,
        onScopeSelect = viewModel::selectScope,
        onStatisticToggle = viewModel::toggleStatistic,
        onMatchesPageSelect = viewModel::selectMatchesPage,
        onShotMatchSelect = viewModel::selectShotMatch,
        onShotMapRetry = viewModel::retryShotMap,
        onRetry = viewModel::retryAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    uiState: PlayerProfileUiState,
    news: LazyPagingItems<io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary>,
    onBack: () -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onTabSelect: (PlayerTab) -> Unit,
    onScopeSelect: (PlayerStatisticScope) -> Unit,
    onStatisticToggle: (PlayerStatisticEntry) -> Unit,
    onMatchesPageSelect: (Int) -> Unit,
    onShotMatchSelect: (MatchId) -> Unit,
    onShotMapRetry: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showProfileHeader: Boolean = true,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (showTopBar) {
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
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (showProfileHeader) {
                SectionContainer(
                    state = uiState.profile,
                    onRetry = onRetry,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    loading = { HeaderSkeleton() },
                ) { PlayerHeader(it, onTeamClick) }
            }

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

            if (uiState.selectedTab == PlayerTab.Dynamic) {
                EntityNewsFeed(
                    articles = news,
                    onArticleClick = onArticleClick,
                    emptyTitle = stringResource(R.string.player_news_empty_title),
                    emptyDescription = stringResource(R.string.player_news_empty_description),
                    modifier = Modifier.weight(1f),
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (uiState.selectedTab) {
                        PlayerTab.Dynamic -> Unit
                        PlayerTab.Data -> PlayerDataTab(
                            uiState,
                            onScopeSelect,
                            onStatisticToggle,
                            onRetry,
                        )

                        PlayerTab.Matches -> PlayerMatchesTab(
                            uiState,
                            onMatchClick,
                            onMatchesPageSelect,
                            onShotMatchSelect,
                            onShotMapRetry,
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
    }
}

@Composable
private fun PlayerDataTab(
    uiState: PlayerProfileUiState,
    onScopeSelect: (PlayerStatisticScope) -> Unit,
    onStatisticToggle: (PlayerStatisticEntry) -> Unit,
    onRetry: () -> Unit,
) {
    SectionContainer(
        state = uiState.statistics,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.player_season_stats_empty_title),
        emptyDescription = stringResource(R.string.player_season_stats_empty_description),
    ) {
        ChoiceRow(
            options = PlayerStatisticScope.entries.map { it.name to stringResource(it.labelRes()) },
            selectedId = uiState.selectedScope.name,
            onSelect = { raw -> onScopeSelect(PlayerStatisticScope.valueOf(raw)) },
        )
        if (uiState.scopeEntries.isEmpty()) {
            InlineEmpty(stringResource(R.string.player_selection_empty))
        } else {
            uiState.scopeEntries.forEach { entry ->
                PlayerStatisticCard(
                    entry = entry,
                    expanded = entry.id == uiState.expandedStatisticId,
                    heatMap = uiState.heatMap,
                    onToggle = { onStatisticToggle(entry) },
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun PlayerStatisticCard(
    entry: PlayerStatisticEntry,
    expanded: Boolean,
    heatMap: SectionState<PlayerHeatMap>,
    onToggle: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            TeamCrest(
                teamId = entry.team.id,
                teamName = entry.team.name,
                crestUrl = entry.team.crestUrl,
                size = 36.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(entry.season.label, style = MaterialTheme.typography.labelLarge)
                Text(
                    listOfNotNull(entry.team.name, entry.competition?.name).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(if (expanded) R.string.player_collapse_details else R.string.player_expand_details),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (entry.summary.isNotEmpty()) TeamStatsGrid(entry.summary, columns = 4)
        if (expanded) {
            SectionContainer(
                state = heatMap,
                onRetry = onRetry,
                title = stringResource(R.string.player_heat_map),
                emptyTitle = stringResource(R.string.player_heat_map_empty_title),
                emptyDescription = stringResource(R.string.player_heat_map_empty_description),
            ) { HeatMap(it) }
            StatisticEntryContent(entry)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun StatisticEntryContent(entry: PlayerStatisticEntry) {
    entry.sections.forEach { section ->
        SectionHeader(section.name)
        TeamStatsGrid(section.values, columns = 4)
    }
}

@Composable
private fun PlayerMatchesTab(
    uiState: PlayerProfileUiState,
    onMatchClick: (MatchId) -> Unit,
    onPageSelect: (Int) -> Unit,
    onShotMatchSelect: (MatchId) -> Unit,
    onShotMapRetry: () -> Unit,
    onRetry: () -> Unit,
) {
    SectionContainer(
        state = uiState.matches,
        onRetry = onRetry,
        emptyTitle = stringResource(R.string.player_matches_empty_title),
        emptyDescription = stringResource(R.string.player_matches_empty_description),
    ) { page ->
        PlayerMatchList(
            page = page,
            selectedShotMatchId = uiState.selectedShotMatchId,
            shotMap = uiState.shotMap,
            onMatchClick = onMatchClick,
            onShotMatchSelect = onShotMatchSelect,
            onShotMapRetry = onShotMapRetry,
        )
        if (page.totalPages > 1) {
            EntityPagination(page.page, page.totalPages, onPageSelect)
        }
    }
}

@Composable
private fun PlayerMatchList(
    page: PlayerMatchPage,
    selectedShotMatchId: MatchId?,
    shotMap: SectionState<PlayerShotMap>,
    onMatchClick: (MatchId) -> Unit,
    onShotMatchSelect: (MatchId) -> Unit,
    onShotMapRetry: () -> Unit,
) {
    page.matches.forEach { performance ->
        val matchId = performance.match.id
        val selected = matchId == selectedShotMatchId
        EntityFixtureRow(
            match = performance.match,
            onClick = { onMatchClick(matchId) },
        )
        PerformanceRow(
            performance = performance,
            shotMapSelected = selected,
            onShotMapClick = { onShotMatchSelect(matchId) },
        )
        if (selected) {
            SectionContainer(
                state = shotMap,
                onRetry = onShotMapRetry,
                title = stringResource(R.string.player_shot_map),
                emptyTitle = stringResource(R.string.player_shot_map_empty_title),
                emptyDescription = stringResource(R.string.player_shot_map_empty_description),
            ) { ShotMap(it) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PerformanceRow(
    performance: PlayerMatchPerformance,
    shotMapSelected: Boolean,
    onShotMapClick: () -> Unit,
) {
    val values = listOfNotNull(
        performance.minutesLabel?.let { stringResource(R.string.player_minutes_value, it) },
        performance.goals?.let { stringResource(R.string.player_goals_value, it) },
        performance.assists?.let { stringResource(R.string.player_assists_value, it) },
        performance.cardsLabel?.let { stringResource(R.string.player_cards_value, it) },
        performance.ratingLabel?.let { stringResource(R.string.player_rating_value, it) },
        performance.userRatingLabel?.let { stringResource(R.string.player_user_rating_value, it) },
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(start = 64.dp, end = DqdSpacing.listHorizontal, bottom = DqdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Text(
            text = values.joinToString("  ·  "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (shotMapSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                )
                .clickable(onClick = onShotMapClick)
                .padding(horizontal = DqdSpacing.sm, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(
                    if (shotMapSelected) R.string.player_hide_shot_map
                    else R.string.player_shot_map,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (shotMapSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Icon(
                painter = painterResource(DqdIcons.ChevronRight),
                contentDescription = null,
                tint = if (shotMapSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .size(14.dp)
                    .rotate(if (shotMapSelected) 90f else 0f),
            )
        }
    }
}

@Composable
private fun HeatMap(data: PlayerHeatMap) {
    val heatImage = remember(data.points, data.direction) { buildHeatMapImage(data) }
    val lineColor = androidx.compose.ui.graphics.Color(0xFFDDE2E6)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.md)
            .background(androidx.compose.ui.graphics.Color(0xFFF6F8F9))
            .aspectRatio(1.6f),
    ) {
        drawImage(
            image = heatImage,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        )

        val strokeWidth = 1.2.dp.toPx()
        val stroke = Stroke(strokeWidth)
        val fieldWidth = size.width
        val fieldHeight = size.height
        drawRect(lineColor, style = stroke)
        drawLine(
            lineColor,
            Offset(fieldWidth / 2f, 0f),
            Offset(fieldWidth / 2f, fieldHeight),
            strokeWidth,
        )
        drawCircle(lineColor, fieldHeight * 0.125f, center, style = stroke)
        drawCircle(lineColor, 1.5.dp.toPx(), center)

        val penaltyHeight = fieldHeight * 0.50f
        val penaltyTop = (fieldHeight - penaltyHeight) / 2f
        val penaltyDepth = fieldWidth * 0.19f
        val goalAreaHeight = fieldHeight * 0.24f
        val goalAreaTop = (fieldHeight - goalAreaHeight) / 2f
        val goalAreaDepth = fieldWidth * 0.08f
        drawRect(lineColor, Offset(0f, penaltyTop), androidx.compose.ui.geometry.Size(penaltyDepth, penaltyHeight), style = stroke)
        drawRect(
            lineColor,
            Offset(fieldWidth - penaltyDepth, penaltyTop),
            androidx.compose.ui.geometry.Size(penaltyDepth, penaltyHeight),
            style = stroke,
        )
        drawRect(lineColor, Offset(0f, goalAreaTop), androidx.compose.ui.geometry.Size(goalAreaDepth, goalAreaHeight), style = stroke)
        drawRect(
            lineColor,
            Offset(fieldWidth - goalAreaDepth, goalAreaTop),
            androidx.compose.ui.geometry.Size(goalAreaDepth, goalAreaHeight),
            style = stroke,
        )
        drawCircle(lineColor, 1.5.dp.toPx(), Offset(fieldWidth * 0.12f, fieldHeight / 2f))
        drawCircle(lineColor, 1.5.dp.toPx(), Offset(fieldWidth * 0.88f, fieldHeight / 2f))
    }
}

private fun buildHeatMapImage(data: PlayerHeatMap): androidx.compose.ui.graphics.ImageBitmap {
    val density = FloatArray(HEAT_MAP_WIDTH * HEAT_MAP_HEIGHT)
    val mirrorX = data.direction.equals("left", ignoreCase = true)
    val sigma = 11f
    val radius = (sigma * 3f).roundToInt()
    val denominator = 2f * sigma * sigma

    data.points.forEach { point ->
        val normalizedX = if (mirrorX) 1f - point.x / 100f else point.x / 100f
        val normalizedY = 1f - point.y / 100f
        val centerX = (normalizedX * (HEAT_MAP_WIDTH - 1)).roundToInt()
        val centerY = (normalizedY * (HEAT_MAP_HEIGHT - 1)).roundToInt()
        val minX = (centerX - radius).coerceAtLeast(0)
        val maxX = (centerX + radius).coerceAtMost(HEAT_MAP_WIDTH - 1)
        val minY = (centerY - radius).coerceAtLeast(0)
        val maxY = (centerY + radius).coerceAtMost(HEAT_MAP_HEIGHT - 1)
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val dx = (x - centerX).toFloat()
                val dy = (y - centerY).toFloat()
                density[y * HEAT_MAP_WIDTH + x] += exp(-(dx * dx + dy * dy) / denominator)
            }
        }
    }

    val maximum = density.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val pixels = IntArray(density.size) { index ->
        val intensity = (density[index] / maximum).coerceIn(0f, 1f).pow(0.58f)
        heatColor(intensity)
    }
    return Bitmap.createBitmap(
        pixels,
        HEAT_MAP_WIDTH,
        HEAT_MAP_HEIGHT,
        Bitmap.Config.ARGB_8888,
    ).asImageBitmap()
}

private fun heatColor(value: Float): Int {
    if (value < 0.045f) return 0
    val alpha = (70f + value * 175f).roundToInt().coerceIn(0, 225)
    val (red, green, blue) = when {
        value < 0.42f -> interpolateHeatColor(value / 0.42f, 35, 218, 108, 96, 232, 88)
        value < 0.68f -> interpolateHeatColor((value - 0.42f) / 0.26f, 96, 232, 88, 255, 224, 54)
        value < 0.84f -> interpolateHeatColor((value - 0.68f) / 0.16f, 255, 224, 54, 255, 137, 31)
        else -> interpolateHeatColor((value - 0.84f) / 0.16f, 255, 137, 31, 239, 53, 32)
    }
    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}

private fun interpolateHeatColor(
    fraction: Float,
    startRed: Int,
    startGreen: Int,
    startBlue: Int,
    endRed: Int,
    endGreen: Int,
    endBlue: Int,
): Triple<Int, Int, Int> {
    val amount = fraction.coerceIn(0f, 1f)
    fun channel(start: Int, end: Int): Int = (start + (end - start) * amount).roundToInt()
    return Triple(
        channel(startRed, endRed),
        channel(startGreen, endGreen),
        channel(startBlue, endBlue),
    )
}

private const val HEAT_MAP_WIDTH = 320
private const val HEAT_MAP_HEIGHT = 200
private const val SHOT_MAP_DEPTH = 50f

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
        val pitchColor = Color(0xFF2FAE5F)
        val lineColor = Color.White.copy(alpha = 0.68f)
        val normalColor = Color.White
        val onTargetColor = Color(0xFFFF8A65)
        val goalColor = Color(0xFFFFD54F)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DqdSpacing.md)
                .clip(RoundedCornerShape(6.dp))
                .background(pitchColor)
                .aspectRatio(1.45f),
        ) {
            val stroke = Stroke(1.2.dp.toPx())
            val penaltyWidth = size.width * 0.68f
            val penaltyDepth = size.height * 0.44f
            val goalAreaWidth = size.width * 0.32f
            val goalAreaDepth = size.height * 0.17f
            val goalWidth = size.width * 0.18f
            val goalDepth = size.height * 0.035f

            drawRect(lineColor, style = stroke)
            drawRect(
                color = lineColor,
                topLeft = Offset((size.width - penaltyWidth) / 2f, 0f),
                size = androidx.compose.ui.geometry.Size(penaltyWidth, penaltyDepth),
                style = stroke,
            )
            drawRect(
                color = lineColor,
                topLeft = Offset((size.width - goalAreaWidth) / 2f, 0f),
                size = androidx.compose.ui.geometry.Size(goalAreaWidth, goalAreaDepth),
                style = stroke,
            )
            drawRect(
                color = lineColor,
                topLeft = Offset((size.width - goalWidth) / 2f, 0f),
                size = androidx.compose.ui.geometry.Size(goalWidth, goalDepth),
                style = stroke,
            )
            drawCircle(
                color = lineColor,
                radius = 1.8.dp.toPx(),
                center = Offset(size.width / 2f, size.height * 0.29f),
            )

            val markerOuterRadius = 7.dp.toPx()
            val markerInnerRadius = 5.dp.toPx()
            plotted.forEach { shot ->
                val x = requireNotNull(shot.x).coerceIn(0f, 100f)
                val y = requireNotNull(shot.y).coerceIn(0f, SHOT_MAP_DEPTH)
                val center = Offset(
                    x = markerOuterRadius +
                        (size.width - markerOuterRadius * 2f) * x / 100f,
                    y = markerOuterRadius +
                        (size.height - markerOuterRadius * 2f) * y / SHOT_MAP_DEPTH,
                )
                val markerColor = when {
                    shot.outcome.orEmpty().contains("进球") -> goalColor
                    shot.outcome.orEmpty().contains("射正") -> onTargetColor
                    else -> normalColor
                }
                drawCircle(
                    color = Color.Black.copy(alpha = 0.32f),
                    radius = markerOuterRadius,
                    center = center,
                )
                drawCircle(
                    color = markerColor,
                    radius = markerInnerRadius,
                    center = center,
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
    AttributeGrid(overview)
    if (overview.marketValues.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_market_value_history))
        MarketValueChart(overview.marketValues)
    }
    overview.characteristics?.let {
        SectionHeader(stringResource(R.string.player_characteristics))
        CharacteristicsContent(it)
    }
    if (overview.transfers.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_transfers))
        TransferList(overview.transfers, onTeamClick)
    }
    if (overview.clubCareer.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_club_career))
        CareerSummaryList(overview.clubCareer, onTeamClick)
    }
    if (overview.nationalCareer.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_national_career))
        CareerSummaryList(overview.nationalCareer, onTeamClick)
    }
    if (overview.injuries.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_injuries))
        InjuryList(overview.injuries)
    }
    if (overview.honors.isNotEmpty()) {
        SectionHeader(stringResource(R.string.player_honors))
        HonorList(overview.honors)
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
            profile.nickname?.let {
                Text(
                    text = stringResource(R.string.player_nickname_value, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val physical = listOfNotNull(
                profile.heightLabel?.let { stringResource(R.string.player_header_height, it) },
                profile.weightLabel?.let { stringResource(R.string.player_header_weight, it) },
                profile.marketValueLabel?.let { stringResource(R.string.player_header_value, it) },
            ).joinToString(" / ")
            if (physical.isNotEmpty()) {
                Text(
                    physical,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val roleDetails = listOfNotNull(
                profile.ageLabel,
                profile.shirtNumber?.let { stringResource(R.string.player_shirt_number, it) },
                listOfNotNull(stringResource(profile.position.labelRes()), profile.footLabel)
                    .joinToString("/")
                    .takeIf(String::isNotEmpty),
            ).joinToString("  |  ")
            if (roleDetails.isNotEmpty()) {
                Text(roleDetails, style = MaterialTheme.typography.labelSmall)
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
private fun AttributeGrid(overview: PlayerOverview) {
    val profile = overview.profile
    val values = overview.facts.map { it.label to it.value } .ifEmpty {
        listOfNotNull(
            profile.name.takeIf(String::isNotEmpty)?.let { "姓名" to it },
            profile.englishName?.let { "全名" to it },
            profile.nationality?.let { "国籍/会籍" to it },
            profile.birthdayLabel?.let { "生日" to it },
            profile.weeklySalaryLabel?.let { "周薪" to it },
            profile.marketValueLabel?.let { "身价" to it },
            profile.contractUntil?.let { "合同到期" to it },
        )
    }
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer)) {
        values.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth()) {
                pair.forEach { (label, value) ->
                    Column(Modifier.weight(1f).padding(DqdSpacing.md)) {
                        Text(value, style = MaterialTheme.typography.bodySmall)
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
private fun CharacteristicsContent(characteristics: FootballCharacteristics) {
    val rows = listOf(
        stringResource(R.string.player_styles) to characteristics.styles,
        stringResource(R.string.player_very_strong) to characteristics.veryStrong,
        stringResource(R.string.player_strong) to characteristics.strong,
        stringResource(R.string.player_weak) to characteristics.weak,
        stringResource(R.string.player_very_weak) to characteristics.veryWeak,
    ).filter { it.second.isNotEmpty() }
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer)) {
        rows.forEach { (label, values) ->
            Row(
                Modifier.fillMaxWidth().padding(DqdSpacing.md),
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
}

@Composable
private fun CareerSummaryList(
    entries: List<PlayerCareerSummary>,
    onTeamClick: (TeamId) -> Unit,
) {
    entries.forEach { entry ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTeamClick(entry.team.id) }
                .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            TeamCrest(
                teamId = entry.team.id,
                teamName = entry.team.name,
                crestUrl = entry.team.crestUrl,
                size = 32.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(entry.team.name, style = MaterialTheme.typography.bodySmall)
                Text(
                    listOfNotNull(entry.startDate, entry.endDate).joinToString(" - "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            listOf(
                stringResource(R.string.player_career_column_appearances) to entry.appearances,
                stringResource(R.string.player_career_column_goals) to entry.goals,
                stringResource(R.string.player_career_column_assists) to entry.assists,
            ).forEach { (label, value) ->
                Column(Modifier.width(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ValueText(value, style = DqdTheme.dataText.tableCellStrong)
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
        Column(Modifier.fillMaxWidth().padding(DqdSpacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(honor.name, style = MaterialTheme.typography.bodySmall)
                honor.times?.let { Text(stringResource(R.string.player_honor_times, it)) }
            }
            if (honor.seasons.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = DqdSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
                ) {
                    honor.seasons.forEach { season ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ImagePlaceholder(
                                url = honor.logoUrl,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(42.dp),
                            )
                            Text(season, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else if (honor.logoUrl != null) {
                ImagePlaceholder(
                    url = honor.logoUrl,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(top = DqdSpacing.sm).size(42.dp),
                )
            }
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
                        injury.durationDays?.let { stringResource(R.string.player_injury_days, it) },
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
