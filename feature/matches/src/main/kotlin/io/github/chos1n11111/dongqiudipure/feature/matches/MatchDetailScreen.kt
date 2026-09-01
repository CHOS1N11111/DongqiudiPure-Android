package io.github.chos1n11111.dongqiudipure.feature.matches

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.MatchStatusBadge
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchEventKind
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.hasScore

@Composable
fun MatchDetailRoute(
    matchId: MatchId,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchDetailViewModel = viewModel(),
) {
    LaunchedEffect(matchId) { viewModel.load(matchId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MatchDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onTeamClick = onTeamClick,
        onTabSelect = viewModel::selectTab,
        onRetryEvents = viewModel::retryEvents,
        onRetryStats = viewModel::retryStats,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    uiState: MatchDetailUiState,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onTabSelect: (MatchTab) -> Unit,
    onRetryEvents: () -> Unit,
    onRetryStats: () -> Unit,
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
                            contentDescription = "返回",
                            modifier = Modifier.size(DqdSize.iconMedium),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO(share): 接入系统分享 */ }) {
                        Icon(
                            painter = painterResource(DqdIcons.Share),
                            contentDescription = "分享",
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
                onRetry = {},
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
                    val isSelected = tab == uiState.selectedTab
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelect(tab) },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            when (uiState.selectedTab) {
                MatchTab.Events -> SectionContainer(
                    state = uiState.events,
                    onRetry = onRetryEvents,
                    emptyTitle = "暂无事件",
                    emptyDescription = "这场比赛还没有已收录的事件记录。",
                    loading = { EventsSkeleton() },
                ) { events ->
                    EventTimeline(events)
                }

                MatchTab.Stats -> SectionContainer(
                    state = uiState.stats,
                    onRetry = onRetryStats,
                    emptyTitle = "暂无技术统计",
                    emptyDescription = "该赛事未提供这场比赛的技术统计。",
                    loading = { StatsSkeleton() },
                ) { stats ->
                    Column(modifier = Modifier.padding(vertical = DqdSpacing.sm)) {
                        stats.forEach { StatRow(it) }
                    }
                }

                // 阵容模型（首发 / 替补 / 教练 / 阵型 / 号码 / 缺阵）尚未定义。
                MatchTab.Lineup -> PendingSection(
                    title = "阵容尚未实现",
                    description = "属于 M5，需要先定义阵容 Domain model 并完成 contract 验证。",
                )
            }
        }
    }
}

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
            text = listOfNotNull(
                match.competition.name,
                match.competition.roundLabel,
            ).joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamSide(
                match = match,
                isHome = true,
                onClick = { onTeamClick(match.home.id) },
                modifier = Modifier.weight(1f),
            )

            Column(
                modifier = Modifier.width(110.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                if (match.status.hasScore) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ValueText(
                            value = match.homeScore,
                            style = DqdTheme.dataText.scoreLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Text(
                            text = " – ",
                            style = DqdTheme.dataText.scoreLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ValueText(
                            value = match.awayScore,
                            style = DqdTheme.dataText.scoreLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                } else {
                    // 未开始 / 延期：没有比分可展示，用中性占位而不是缺失记号。
                    Text(
                        text = "–",
                        style = DqdTheme.dataText.scoreLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MatchStatusBadge(status = match.status)
            }

            TeamSide(
                match = match,
                isHome = false,
                onClick = { onTeamClick(match.away.id) },
                modifier = Modifier.weight(1f),
            )
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
        TeamCrest(
            teamId = team.id,
            teamName = team.name,
            crestUrl = team.crestUrl,
            size = DqdSize.crestLarge,
        )
        Text(
            text = team.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EventTimeline(events: List<MatchEvent>) {
    Column(modifier = Modifier.padding(vertical = DqdSpacing.sm)) {
        events.forEach { event ->
            EventRow(event)
        }
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
            text = event.minuteLabel,
            style = DqdTheme.dataText.minuteLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(32.dp),
        )

        EventIcon(event.kind)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = event.primaryName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val subtitle = event.secondaryName ?: event.kind.label()
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val scoreAfter = event.scoreAfter
        if (scoreAfter != null) {
            Text(
                text = scoreAfter,
                style = DqdTheme.dataText.statValue,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 事件图标。
 *
 * 黄牌 / 红牌画成矩形牌而非纯色圆点 —— 色觉差异用户靠图形也能分辨，
 * 颜色只是第二重编码。
 */
@Composable
private fun EventIcon(kind: MatchEventKind) {
    val sports = DqdTheme.sports
    val tint = when (kind) {
        MatchEventKind.Goal, MatchEventKind.PenaltyGoal -> sports.win
        MatchEventKind.OwnGoal -> sports.loss
        MatchEventKind.YellowCard -> sports.yellowCard
        MatchEventKind.RedCard, MatchEventKind.SecondYellow -> sports.redCard
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            MatchEventKind.YellowCard, MatchEventKind.RedCard, MatchEventKind.SecondYellow ->
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 11.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(tint),
                )

            MatchEventKind.Substitution -> Icon(
                painter = painterResource(DqdIcons.Substitution),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp),
            )

            else -> Icon(
                painter = painterResource(DqdIcons.Ball),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

private fun MatchEventKind.label(): String = when (this) {
    MatchEventKind.Goal -> "进球"
    MatchEventKind.OwnGoal -> "乌龙球"
    MatchEventKind.PenaltyGoal -> "点球"
    MatchEventKind.YellowCard -> "黄牌"
    MatchEventKind.RedCard -> "红牌"
    MatchEventKind.SecondYellow -> "两黄变红"
    MatchEventKind.Substitution -> "换人"
    MatchEventKind.VarReview -> "VAR 回看"
    // 未知事件类型不丢弃，原样显示服务端返回的值。
    is MatchEventKind.Unknown -> rawValue
}

/**
 * 统计对比条。
 *
 * 指标集合由服务端驱动，客户端不写死列表。
 * 两侧都缺失时显示虚线空槽 + 「—」，与真实的 0 视觉上完全不同。
 */
@Composable
private fun StatRow(stat: StatItem) {
    val hasData = stat.homeFraction != null && stat.awayFraction != null
    val total = ((stat.homeFraction ?: 0f) + (stat.awayFraction ?: 0f)).takeIf { it > 0f }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ValueText(
                value = stat.homeValue,
                style = DqdTheme.dataText.statValue.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
            Text(
                text = stat.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            ValueText(
                value = stat.awayValue,
                style = DqdTheme.dataText.statValue.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }

        if (hasData && total != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val homeWeight = (stat.homeFraction!! / total).coerceIn(0.001f, 1f)
                val awayWeight = (stat.awayFraction!! / total).coerceIn(0.001f, 1f)
                Box(
                    modifier = Modifier
                        .weight(homeWeight)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Box(
                    modifier = Modifier
                        .weight(awayWeight)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        } else {
            // 虚线空槽：这一项没有数据，而不是数值为零。
            DashedTrack()
        }
    }
}

@Composable
private fun DashedTrack() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(24) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            )
        }
    }
}

@Composable
private fun PendingSection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Icon(
            painter = painterResource(DqdIcons.Info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScoreHeaderSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
        ) {
            SkeletonBox(Modifier.size(DqdSize.crestLarge), CircleShape)
            SkeletonBox(Modifier.width(56.dp).height(13.dp))
        }
        SkeletonBox(Modifier.width(96.dp).height(40.dp))
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

@Composable
private fun EventsSkeleton() {
    Column(
        modifier = Modifier.padding(
            horizontal = DqdSpacing.listHorizontal,
            vertical = DqdSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        repeat(5) {
            Row(horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md)) {
                SkeletonBox(Modifier.width(32.dp).height(16.dp))
                SkeletonBox(Modifier.size(22.dp), CircleShape)
                SkeletonBox(Modifier.fillMaxWidth(0.55f).height(16.dp))
            }
        }
    }
}

@Composable
private fun StatsSkeleton() {
    Column(
        modifier = Modifier.padding(
            horizontal = DqdSpacing.listHorizontal,
            vertical = DqdSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
    ) {
        repeat(5) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonBox(Modifier.fillMaxWidth(0.4f).height(14.dp))
                SkeletonBox(Modifier.fillMaxWidth().height(5.dp))
            }
        }
    }
}

@Preview(name = "比赛详情 · 深色", showBackground = true)
@Composable
private fun MatchDetailDarkPreview() {
    DqdTheme(darkTheme = true) {
        MatchDetailScreen(
            uiState = MatchDetailUiState(
                header = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.liveMatch,
                ),
                events = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.events,
                ),
                stats = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches.stats,
                ),
            ),
            onBack = {},
            onTeamClick = {},
            onTabSelect = {},
            onRetryEvents = {},
            onRetryStats = {},
        )
    }
}
