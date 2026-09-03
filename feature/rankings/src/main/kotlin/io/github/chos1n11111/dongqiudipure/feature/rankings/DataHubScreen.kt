package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingSection
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataHubRoute(
    selectedCompetitionIds: Set<String>,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingsViewModel = hiltViewModel(),
) {
    LaunchedEffect(selectedCompetitionIds) { viewModel.loadHub(selectedCompetitionIds) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.data_title)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                CompetitionSwitcher(
                    competitions = uiState.competitions,
                    selected = uiState.selectedCompetition,
                    onSelect = viewModel::selectCompetition,
                )
                SeasonSwitcher(
                    seasons = uiState.seasons,
                    selected = uiState.selectedSeason,
                    onSelect = viewModel::selectSeason,
                )
                RankingSectionSwitcher(
                    selected = uiState.selectedSection,
                    onSelect = viewModel::selectSection,
                )
                if (uiState.selectedSection != RankingSection.Standings &&
                    uiState.metrics.isNotEmpty()
                ) {
                    MetricSwitcher(
                        metrics = uiState.metrics,
                        selected = uiState.selectedMetric,
                        onSelect = viewModel::selectMetric,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        when (uiState.selectedSection) {
            RankingSection.Standings -> RankingsContent(
                uiState = uiState,
                onTeamClick = onTeamClick,
                onMatchClick = onMatchClick,
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            RankingSection.Players, RankingSection.Teams -> StatisticRankingContent(
                state = uiState.statisticTable,
                onTeamClick = onTeamClick,
                onPlayerClick = onPlayerClick,
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
internal fun SeasonSwitcher(
    seasons: List<SeasonOption>,
    selected: SeasonOption?,
    onSelect: (SeasonOption) -> Unit,
) {
    if (seasons.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(seasons, key = { it.id }) { season ->
            SwitchChip(
                name = season.label,
                selected = season.id == selected?.id,
                onClick = { onSelect(season) },
            )
        }
    }
}

@Composable
private fun CompetitionSwitcher(
    competitions: List<CompetitionRef>,
    selected: CompetitionRef?,
    onSelect: (CompetitionRef) -> Unit,
) {
    if (competitions.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(competitions, key = { it.id.raw }) { competition ->
            SwitchChip(
                name = competition.name,
                selected = competition.id == selected?.id,
                onClick = { onSelect(competition) },
            )
        }
    }
}

@Composable
private fun RankingSectionSwitcher(
    selected: RankingSection,
    onSelect: (RankingSection) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(RankingSection.entries, key = { it.name }) { section ->
            val label = when (section) {
                RankingSection.Standings -> stringResource(R.string.rankings_tab_standings)
                RankingSection.Players -> stringResource(R.string.rankings_tab_players)
                RankingSection.Teams -> stringResource(R.string.rankings_tab_teams)
            }
            SwitchChip(
                name = label,
                selected = section == selected,
                onClick = { onSelect(section) },
            )
        }
    }
}

@Composable
private fun MetricSwitcher(
    metrics: List<RankingMetric>,
    selected: RankingMetric?,
    onSelect: (RankingMetric) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.lg),
    ) {
        items(metrics, key = { it.id }) { metric ->
            val isSelected = metric == selected
            Text(
                text = metric.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clickable { onSelect(metric) }
                    .padding(horizontal = 4.dp, vertical = 5.dp)
                    .semantics { this.selected = isSelected },
            )
        }
    }
}

@Composable
private fun SwitchChip(name: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = DqdSpacing.md, vertical = 7.dp)
            .semantics { this.selected = selected },
    )
}
