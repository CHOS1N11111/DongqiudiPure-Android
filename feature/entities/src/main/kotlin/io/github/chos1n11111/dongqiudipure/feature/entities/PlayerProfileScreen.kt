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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@Composable
fun PlayerProfileRoute(
    playerId: PlayerId,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerProfileViewModel = viewModel(),
) {
    LaunchedEffect(playerId) { viewModel.load(playerId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlayerProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onTeamClick = onTeamClick,
        onRetry = viewModel::retryAll,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    uiState: PlayerProfileUiState,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
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
                state = uiState.profile,
                onRetry = onRetry,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                loading = { HeaderSkeleton() },
            ) { profile ->
                PlayerHeader(profile = profile, onTeamClick = onTeamClick)
            }

            SectionContainer(
                state = uiState.profile,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = "资料",
            ) { profile ->
                AttributeGrid(profile)
            }

            SectionContainer(
                state = uiState.seasonStats,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = "本赛季数据",
                emptyTitle = "暂无赛季数据",
                emptyDescription = "该赛事未提供这名球员的本赛季统计。",
            ) { stats ->
                TeamStatsGrid(stats = stats)
            }

            SectionContainer(
                state = uiState.career,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = "履历",
                emptyTitle = "暂无履历",
                emptyDescription = "没有已收录的历史赛季记录。",
            ) { career ->
                CareerTable(career)
            }

            Box(modifier = Modifier.height(DqdSpacing.xl))
        }
    }
}

@Composable
private fun PlayerHeader(profile: PlayerProfile, onTeamClick: (TeamId) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // 号码缺失时显示「—」而不是省略这一位。
                Box(contentAlignment = Alignment.Center) {
                    ValueText(
                        value = profile.shirtNumber?.let { "#$it" },
                        style = DqdTheme.dataText.statValue.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
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
                        size = 16.dp,
                    )
                    Text(
                        text = "${team.name} · ${profile.position.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = profile.position.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AttributeGrid(profile: PlayerProfile) {
    val attributes = listOf(
        "国籍" to profile.nationality,
        "年龄" to profile.ageLabel,
        "身高" to profile.heightLabel,
        "惯用脚" to profile.footLabel,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        attributes.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth()) {
                pair.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                horizontal = DqdSpacing.listHorizontal,
                                vertical = DqdSpacing.md,
                            ),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        ValueText(
                            value = value,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (pair.size == 1) Box(modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/**
 * 履历表。
 *
 * 历史赛季常有数据缺失（尤其是低级别联赛与早期赛季）。
 * 缺失行显示「—」，不补 0 —— 补 0 会让人以为那个赛季一场没打。
 */
@Composable
private fun CareerTable(career: List<CareerEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DqdSpacing.listHorizontal,
                    vertical = DqdSpacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "赛季",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(64.dp),
            )
            Text(
                text = "球队",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "出场",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp),
            )
            Text(
                text = "进球",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        career.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = DqdSpacing.listHorizontal,
                        vertical = DqdSpacing.md,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.seasonLabel,
                    style = DqdTheme.dataText.tableCell,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(64.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.teamName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val competition = entry.competitionName
                    if (competition != null) {
                        Text(
                            text = competition,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                    ValueText(
                        value = entry.appearances,
                        style = DqdTheme.dataText.tableCell.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
                Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                    ValueText(
                        value = entry.goals,
                        style = DqdTheme.dataText.tableCellStrong.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun HeaderSkeleton() {
    Row(
        modifier = Modifier.padding(horizontal = DqdSpacing.lg, vertical = DqdSpacing.md),
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

@Preview(name = "球员资料 · 深色", showBackground = true)
@Composable
private fun PlayerProfileDarkPreview() {
    DqdTheme(darkTheme = true) {
        PlayerProfileScreen(
            uiState = PlayerProfileUiState(
                profile = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers.profile,
                ),
                seasonStats = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers.profileStats,
                ),
                career = SectionState.Content(
                    io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers.career,
                ),
            ),
            onBack = {},
            onTeamClick = {},
            onRetry = {},
        )
    }
}
