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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.labelRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SkeletonBox
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.ValueText
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHonor
import io.github.chos1n11111.dongqiudipure.core.model.PlayerInjury
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.PlayerTransfer
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@Composable
fun PlayerProfileRoute(
    playerId: PlayerId,
    onBack: () -> Unit,
    onTeamClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerProfileViewModel = hiltViewModel(),
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
                loading = { HeaderSkeleton() },
            ) { profile ->
                PlayerHeader(profile = profile, onTeamClick = onTeamClick)
            }

            SectionContainer(
                state = uiState.profile,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_profile_section),
            ) { profile ->
                AttributeGrid(profile)
            }

            SectionContainer(
                state = uiState.ability,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_ability),
                emptyTitle = stringResource(R.string.player_season_stats_empty_title),
                emptyDescription = stringResource(R.string.player_season_stats_empty_description),
            ) { ability ->
                AbilityContent(ability)
            }

            SectionContainer(
                state = uiState.career,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_career),
                emptyTitle = stringResource(R.string.player_career_empty_title),
                emptyDescription = stringResource(R.string.player_career_empty_description),
            ) { career ->
                CareerTable(career)
            }

            SectionContainer(
                state = uiState.honors,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_honors),
                emptyTitle = stringResource(R.string.player_honors_empty),
                emptyDescription = stringResource(R.string.player_honors_empty_description),
            ) { honors -> HonorList(honors) }

            SectionContainer(
                state = uiState.transfers,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_transfers),
                emptyTitle = stringResource(R.string.player_transfers_empty),
                emptyDescription = stringResource(R.string.player_transfers_empty_description),
            ) { transfers -> TransferList(transfers, onTeamClick) }

            SectionContainer(
                state = uiState.injuries,
                onRetry = onRetry,
                modifier = Modifier.padding(top = DqdSpacing.sm),
                title = stringResource(R.string.player_injuries),
                emptyTitle = stringResource(R.string.player_injuries_empty),
                emptyDescription = stringResource(R.string.player_injuries_empty_description),
            ) { injuries -> InjuryList(injuries) }

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
        PlayerAvatar(
            playerId = profile.id,
            playerName = profile.name,
            avatarUrl = profile.avatarUrl,
            size = 64.dp,
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
                        value = profile.shirtNumber?.let { stringResource(R.string.player_shirt_number, it) },
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
                        text = "${team.name} · ${stringResource(profile.position.labelRes())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(profile.position.labelRes()),
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
        stringResource(R.string.player_attr_nationality) to profile.nationality,
        stringResource(R.string.player_attr_age) to profile.ageLabel,
        stringResource(R.string.player_attr_birthday) to profile.birthdayLabel,
        stringResource(R.string.player_attr_height) to profile.heightLabel,
        stringResource(R.string.player_attr_weight) to profile.weightLabel,
        stringResource(R.string.player_attr_foot) to profile.footLabel,
        stringResource(R.string.player_attr_value) to profile.marketValueLabel,
        stringResource(R.string.player_attr_contract) to profile.contractUntil,
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

@Composable
private fun AbilityContent(ability: PlayerAbility) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        ) {
            ValueText(
                value = ability.overall,
                style = DqdTheme.dataText.scoreLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
            Column {
                Text(
                    text = stringResource(R.string.player_ability_overall),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ability.version?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        TeamStatsGrid(stats = ability.attributes)
    }
}

@Composable
private fun HonorList(honors: List<PlayerHonor>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        honors.forEach { honor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = honor.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (honor.seasons.isNotEmpty()) {
                        Text(
                            text = honor.seasons.joinToString(" / "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                honor.times?.let {
                    Text(
                        text = stringResource(R.string.player_honor_times, it),
                        style = DqdTheme.dataText.tableCellStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun TransferList(transfers: List<PlayerTransfer>, onTeamClick: (TeamId) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        transfers.forEach { transfer ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
                verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                ) {
                    Text(
                        text = transfer.date.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = listOfNotNull(transfer.type, transfer.fee)
                            .joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
                ) {
                    TransferTeam(
                        team = transfer.fromTeam,
                        onClick = onTeamClick,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TransferTeam(
                        team = transfer.toTeam,
                        onClick = onTeamClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun TransferTeam(
    team: io.github.chos1n11111.dongqiudipure.core.model.TeamRef?,
    onClick: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (team == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            ValueText(value = null as String?, style = MaterialTheme.typography.bodySmall)
        }
        return
    }
    Row(
        modifier = modifier.clickable { onClick(team.id) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TeamCrest(
            teamId = team.id,
            teamName = team.name,
            crestUrl = team.crestUrl,
            size = 22.dp,
        )
        Text(
            text = team.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun InjuryList(injuries: List<PlayerInjury>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        injuries.forEach { injury ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = injury.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = listOfNotNull(
                            injury.teamName,
                            listOfNotNull(injury.startDate, injury.endDate)
                                .joinToString(" - ").takeIf(String::isNotEmpty),
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                injury.gamesMissed?.let {
                    Text(
                        text = stringResource(R.string.player_games_missed, it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                text = stringResource(R.string.player_career_column_season),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(64.dp),
            )
            Text(
                text = stringResource(R.string.player_career_column_team),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.player_career_column_appearances),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp),
            )
            Text(
                text = stringResource(R.string.player_career_column_starts),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp),
            )
            Text(
                text = stringResource(R.string.player_career_column_goals),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp),
            )
            Text(
                text = stringResource(R.string.player_career_column_assists),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp),
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
                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                    ValueText(
                        value = entry.starts,
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
                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                    ValueText(
                        value = entry.assists,
                        style = DqdTheme.dataText.tableCell.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                profile = SectionState.Empty,
                ability = SectionState.Empty,
                career = SectionState.Empty,
                honors = SectionState.Empty,
                transfers = SectionState.Empty,
                injuries = SectionState.Empty,
            ),
            onBack = {},
            onTeamClick = {},
            onRetry = {},
        )
    }
}
