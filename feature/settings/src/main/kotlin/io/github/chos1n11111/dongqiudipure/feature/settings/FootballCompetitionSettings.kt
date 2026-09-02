package io.github.chos1n11111.dongqiudipure.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballCatalogRepository
import io.github.chos1n11111.dongqiudipure.core.designsystem.R as DesignR
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.SectionContainer
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CompetitionSettingsMode(@param:StringRes val titleRes: Int) {
    Matches(R.string.settings_matches_title),
    Rankings(R.string.settings_rankings_title),
}

@HiltViewModel
class FootballCompetitionSettingsViewModel @Inject constructor(
    private val repository: FootballCatalogRepository,
) : ViewModel() {
    private val _catalog = MutableStateFlow<SectionState<List<CompetitionCatalogGroup>>>(
        SectionState.Loading,
    )
    val catalog: StateFlow<SectionState<List<CompetitionCatalogGroup>>> = _catalog.asStateFlow()

    init {
        loadCatalog()
    }

    fun retry() {
        _catalog.value = SectionState.Loading
        loadCatalog()
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            _catalog.value = when (val result = repository.loadCompetitionCatalog()) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> if (result.value.isEmpty()) {
                    SectionState.Empty
                } else {
                    SectionState.Content(result.value)
                }
            }
        }
    }
}

@Composable
fun FootballCompetitionSettingsRoute(
    mode: CompetitionSettingsMode,
    preferences: FootballPreferences,
    onDefaultMatchCompetitionChange: (String?) -> Unit,
    onMatchCompetitionToggle: (String, Boolean) -> Unit,
    onRankingCompetitionToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FootballCompetitionSettingsViewModel = hiltViewModel(),
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    FootballCompetitionSettingsScreen(
        mode = mode,
        catalog = catalog,
        preferences = preferences,
        onDefaultMatchCompetitionChange = onDefaultMatchCompetitionChange,
        onMatchCompetitionToggle = onMatchCompetitionToggle,
        onRankingCompetitionToggle = onRankingCompetitionToggle,
        onBack = onBack,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FootballCompetitionSettingsScreen(
    mode: CompetitionSettingsMode,
    catalog: SectionState<List<CompetitionCatalogGroup>>,
    preferences: FootballPreferences,
    onDefaultMatchCompetitionChange: (String?) -> Unit,
    onMatchCompetitionToggle: (String, Boolean) -> Unit,
    onRankingCompetitionToggle: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(mode.titleRes)) },
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
            SettingsSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = stringResource(R.string.settings_competitions_search_hint),
            )
            SectionContainer(
                state = catalog,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
                emptyTitle = stringResource(R.string.settings_competitions_empty),
                emptyDescription = stringResource(R.string.settings_competitions_empty_description),
            ) { groups ->
                val filteredGroups = filterCompetitionGroups(groups, query)
                if (filteredGroups.isEmpty()) {
                    SettingsSearchEmptyState(query)
                } else {
                    CompetitionCatalogList(
                        mode = mode,
                        groups = filteredGroups,
                        preferences = preferences,
                        showMatchDefaults = query.isBlank(),
                        onDefaultMatchCompetitionChange = onDefaultMatchCompetitionChange,
                        onMatchCompetitionToggle = onMatchCompetitionToggle,
                        onRankingCompetitionToggle = onRankingCompetitionToggle,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompetitionCatalogList(
    mode: CompetitionSettingsMode,
    groups: List<CompetitionCatalogGroup>,
    preferences: FootballPreferences,
    showMatchDefaults: Boolean,
    onDefaultMatchCompetitionChange: (String?) -> Unit,
    onMatchCompetitionToggle: (String, Boolean) -> Unit,
    onRankingCompetitionToggle: (String, Boolean) -> Unit,
) {
    val allCompetitions = groups.flatMap { it.competitions }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (mode == CompetitionSettingsMode.Matches && showMatchDefaults) {
            item(key = "default-heading") {
                CatalogHeading(stringResource(R.string.settings_matches_default))
            }
            item(key = "default-important") {
                DefaultCompetitionRow(
                    name = stringResource(R.string.settings_matches_important),
                    selected = preferences.defaultMatchCompetitionId == null,
                    onClick = { onDefaultMatchCompetitionChange(null) },
                )
            }
            items(
                items = allCompetitions.filter {
                    it.id.raw in preferences.matchCompetitionIds
                },
                key = { "default-${it.id.raw}" },
            ) { competition ->
                DefaultCompetitionRow(
                    name = competition.name,
                    selected = preferences.defaultMatchCompetitionId == competition.id.raw,
                    onClick = { onDefaultMatchCompetitionChange(competition.id.raw) },
                )
            }
            item(key = "selection-heading") {
                CatalogHeading(stringResource(R.string.settings_matches_selection))
            }
        }

        groups.forEach { group ->
            item(key = "group-${group.name}") { CatalogGroupHeading(group.name) }
            items(group.competitions, key = { "choice-${it.id.raw}" }) { competition ->
                val selected = when (mode) {
                    CompetitionSettingsMode.Matches ->
                        competition.id.raw in preferences.matchCompetitionIds
                    CompetitionSettingsMode.Rankings ->
                        competition.id.raw in preferences.rankingCompetitionIds
                }
                SelectionSettingsRow(
                    label = competition.name,
                    selected = selected,
                    onClick = {
                        when (mode) {
                            CompetitionSettingsMode.Matches ->
                                onMatchCompetitionToggle(competition.id.raw, !selected)
                            CompetitionSettingsMode.Rankings ->
                                onRankingCompetitionToggle(competition.id.raw, !selected)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CatalogHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = DqdSpacing.listHorizontal,
                end = DqdSpacing.listHorizontal,
                top = DqdSpacing.lg,
                bottom = DqdSpacing.sm,
            )
            .semantics { heading() },
    )
}

@Composable
private fun CatalogGroupHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = DqdSpacing.listHorizontal, vertical = DqdSpacing.sm)
            .semantics { heading() },
    )
}

@Composable
private fun DefaultCompetitionRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .defaultMinSize(minHeight = DqdSize.touchTarget)
            .padding(horizontal = DqdSpacing.listHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

internal fun filterCompetitionGroups(
    groups: List<CompetitionCatalogGroup>,
    query: String,
): List<CompetitionCatalogGroup> {
    val term = query.trim()
    if (term.isEmpty()) return groups
    return groups.mapNotNull { group ->
        val competitions = if (group.name.contains(term, ignoreCase = true)) {
            group.competitions
        } else {
            group.competitions.filter { it.name.contains(term, ignoreCase = true) }
        }
        group.copy(competitions = competitions).takeIf { competitions.isNotEmpty() }
    }
}
