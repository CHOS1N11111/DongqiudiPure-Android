package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdEmptyState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.DqdErrorState
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.PlayerAvatar
import io.github.chos1n11111.dongqiudipure.core.designsystem.component.TeamCrest
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.EntitySearchResults
import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntity
import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntityPreferences
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId

@Composable
fun MainTeamRoute(
    preferences: FollowedEntityPreferences,
    onAddEntity: (FollowedEntity, Boolean) -> Unit,
    onRemoveEntity: (String) -> Unit,
    onSetMainTeam: (TeamId) -> Unit,
    onArticleClick: (ArticleId) -> Unit,
    onMatchClick: (MatchId) -> Unit,
    onTeamClick: (TeamId) -> Unit,
    onPlayerClick: (PlayerId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainTeamViewModel = hiltViewModel(),
    teamViewModel: TeamProfileViewModel = hiltViewModel(),
    playerViewModel: PlayerProfileViewModel = hiltViewModel(),
) {
    val searchState by viewModel.uiState.collectAsStateWithLifecycle()
    val teamState by teamViewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val teamNews = teamViewModel.news.collectAsLazyPagingItems()
    val teamCircle = teamViewModel.circle.collectAsLazyPagingItems()
    val playerNews = playerViewModel.news.collectAsLazyPagingItems()

    val orderedEntities = remember(preferences) {
        listOfNotNull(preferences.mainTeam) +
            preferences.entities.filterNot { it.stableKey == preferences.mainTeam?.stableKey }
    }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(orderedEntities, preferences.mainTeamId) {
        if (orderedEntities.none { it.stableKey == selectedKey }) {
            selectedKey = preferences.mainTeam?.stableKey ?: orderedEntities.firstOrNull()?.stableKey
        }
    }
    val selectedEntity = orderedEntities.firstOrNull { it.stableKey == selectedKey }
    LaunchedEffect(selectedEntity?.stableKey) {
        when (selectedEntity) {
            is FollowedEntity.Team -> teamViewModel.load(selectedEntity.team.id)
            is FollowedEntity.Player -> playerViewModel.load(selectedEntity.player.id)
            null -> Unit
        }
    }

    MainTeamScaffold(
        preferences = preferences,
        entities = orderedEntities,
        selectedKey = selectedKey,
        searchState = searchState,
        onEntitySelect = { selectedKey = it.stableKey },
        onQueryChange = viewModel::setQuery,
        onSearchRetry = viewModel::retrySearch,
        onSearchClear = viewModel::clearSearch,
        onAddEntity = { entity, makeMain ->
            selectedKey = entity.stableKey
            onAddEntity(entity, makeMain)
        },
        onRemoveEntity = onRemoveEntity,
        onSetMainTeam = onSetMainTeam,
        modifier = modifier,
    ) {
        when (selectedEntity) {
            is FollowedEntity.Team -> TeamProfileScreen(
                uiState = teamState,
                news = teamNews,
                circle = teamCircle,
                onBack = {},
                onArticleClick = onArticleClick,
                onMatchClick = onMatchClick,
                onTeamClick = onTeamClick,
                onPlayerClick = onPlayerClick,
                onTabSelect = teamViewModel::selectTab,
                onScheduleSeasonSelect = teamViewModel::selectScheduleSeason,
                onSquadSeasonSelect = teamViewModel::selectSquadSeason,
                onStatisticsSeasonSelect = teamViewModel::selectStatisticsSeason,
                onTransferWindowSelect = teamViewModel::selectTransferWindow,
                onRetry = teamViewModel::retryAll,
                onRetryTab = teamViewModel::retrySelectedTab,
                modifier = Modifier.fillMaxSize(),
                showTopBar = false,
                showProfileHeader = false,
            )

            is FollowedEntity.Player -> PlayerProfileScreen(
                uiState = playerState,
                news = playerNews,
                onBack = {},
                onArticleClick = onArticleClick,
                onMatchClick = onMatchClick,
                onTeamClick = onTeamClick,
                onTabSelect = playerViewModel::selectTab,
                onScopeSelect = playerViewModel::selectScope,
                onStatisticToggle = playerViewModel::toggleStatistic,
                onMatchesPageSelect = playerViewModel::selectMatchesPage,
                onShotMatchSelect = playerViewModel::selectShotMatch,
                onShotMapRetry = playerViewModel::retryShotMap,
                onRetry = playerViewModel::retryAll,
                modifier = Modifier.fillMaxSize(),
                showTopBar = false,
                showProfileHeader = false,
            )

            null -> MainTeamEmptyState(onSelect = {})
        }
    }
}

private enum class SheetMode { Search, Manage }

private enum class EntityFilter { Teams, Players }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTeamScaffold(
    preferences: FollowedEntityPreferences,
    entities: List<FollowedEntity>,
    selectedKey: String?,
    searchState: MainTeamUiState,
    onEntitySelect: (FollowedEntity) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchRetry: () -> Unit,
    onSearchClear: () -> Unit,
    onAddEntity: (FollowedEntity, Boolean) -> Unit,
    onRemoveEntity: (String) -> Unit,
    onSetMainTeam: (TeamId) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var sheetMode by rememberSaveable { mutableStateOf<SheetMode?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val needsMainTeam = preferences.mainTeam == null

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.main_team_title)) },
                actions = {
                    if (entities.isNotEmpty()) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    painterResource(DqdIcons.More),
                                    contentDescription = stringResource(R.string.main_team_manage),
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.main_team_manage)) },
                                    onClick = {
                                        menuExpanded = false
                                        sheetMode = SheetMode.Manage
                                    },
                                )
                            }
                        }
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
            if (entities.isNotEmpty()) {
                FollowedEntityStrip(
                    entities = entities,
                    selectedKey = selectedKey,
                    onSelect = onEntitySelect,
                    onAdd = {
                        onSearchClear()
                        sheetMode = SheetMode.Search
                    },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(modifier = Modifier.weight(1f)) { content() }
            } else {
                MainTeamEmptyState(
                    onSelect = {
                        onSearchClear()
                        sheetMode = SheetMode.Search
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    sheetMode?.let { mode ->
        ModalBottomSheet(
            onDismissRequest = {
                sheetMode = null
                onSearchClear()
            },
        ) {
            when (mode) {
                SheetMode.Search -> EntitySearchSheet(
                    uiState = searchState,
                    preferences = preferences,
                    teamOnly = needsMainTeam,
                    onQueryChange = onQueryChange,
                    onRetry = onSearchRetry,
                    onAdd = { entity ->
                        onAddEntity(entity, needsMainTeam && entity is FollowedEntity.Team)
                        if (needsMainTeam) {
                            sheetMode = null
                            onSearchClear()
                        }
                    },
                    onRemove = onRemoveEntity,
                )

                SheetMode.Manage -> ManageEntitiesSheet(
                    preferences = preferences,
                    entities = entities,
                    onSetMainTeam = onSetMainTeam,
                    onRemove = onRemoveEntity,
                    onAdd = {
                        onSearchClear()
                        sheetMode = SheetMode.Search
                    },
                )
            }
        }
    }
}

@Composable
private fun FollowedEntityStrip(
    entities: List<FollowedEntity>,
    selectedKey: String?,
    onSelect: (FollowedEntity) -> Unit,
    onAdd: () -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = DqdSpacing.md, vertical = DqdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(entities, key = FollowedEntity::stableKey) { entity ->
            val selected = entity.stableKey == selectedKey
            Box(
                modifier = Modifier
                    .size(if (selected) 72.dp else 56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.surface,
                    )
                    .clickable { onSelect(entity) }
                    .semantics { contentDescription = entity.name },
                contentAlignment = Alignment.Center,
            ) {
                FollowedEntityImage(entity, if (selected) 56.dp else 44.dp)
            }
        }
        item(key = "add-followed-entity") {
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Icon(
                    painterResource(DqdIcons.Add),
                    contentDescription = stringResource(R.string.main_team_add),
                )
            }
        }
    }
}

@Composable
private fun FollowedEntityImage(entity: FollowedEntity, size: androidx.compose.ui.unit.Dp) {
    when (entity) {
        is FollowedEntity.Team -> TeamCrest(
            teamId = entity.team.id,
            teamName = entity.team.name,
            crestUrl = entity.team.crestUrl,
            size = size,
        )

        is FollowedEntity.Player -> PlayerAvatar(
            playerId = entity.player.id,
            playerName = entity.player.name,
            avatarUrl = entity.player.avatarUrl,
            size = size,
        )
    }
}

@Composable
private fun MainTeamEmptyState(
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DqdEmptyState(
            title = stringResource(R.string.main_team_empty_title),
            description = stringResource(R.string.main_team_empty_description),
        )
        Button(onClick = onSelect, modifier = Modifier.padding(top = DqdSpacing.md)) {
            Icon(
                painterResource(DqdIcons.Add),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.main_team_choose),
                modifier = Modifier.padding(start = DqdSpacing.sm),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntitySearchSheet(
    uiState: MainTeamUiState,
    preferences: FollowedEntityPreferences,
    teamOnly: Boolean,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onAdd: (FollowedEntity) -> Unit,
    onRemove: (String) -> Unit,
) {
    var filter by rememberSaveable(teamOnly) { mutableStateOf(EntityFilter.Teams) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 420.dp, max = 680.dp)
            .imePadding()
            .padding(horizontal = DqdSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Text(
            text = stringResource(if (teamOnly) R.string.main_team_choose else R.string.main_team_add),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (!teamOnly) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                EntityFilter.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = filter == item,
                        onClick = { filter = item },
                        shape = SegmentedButtonDefaults.itemShape(index, EntityFilter.entries.size),
                        label = {
                            Text(
                                stringResource(
                                    if (item == EntityFilter.Teams) R.string.main_team_teams
                                    else R.string.main_team_players,
                                ),
                            )
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            singleLine = true,
            label = {
                Text(
                    stringResource(
                        if (teamOnly || filter == EntityFilter.Teams) R.string.main_team_search_teams
                        else R.string.main_team_search_players,
                    ),
                )
            },
            leadingIcon = {
                Icon(painterResource(DqdIcons.Search), contentDescription = null)
            },
            trailingIcon = if (uiState.query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            painterResource(DqdIcons.Close),
                            contentDescription = stringResource(R.string.main_team_clear_search),
                        )
                    }
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val state = uiState.searchResults) {
                null -> SearchPrompt(teamOnly || filter == EntityFilter.Teams)
                SectionState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(28.dp),
                )
                SectionState.Empty -> DqdEmptyState(
                    title = stringResource(R.string.main_team_search_empty),
                    description = stringResource(R.string.main_team_search_empty_description),
                    modifier = Modifier.align(Alignment.Center),
                )
                is SectionState.Failed -> DqdErrorState(
                    error = state.error,
                    onRetry = onRetry,
                    forceRetry = true,
                    modifier = Modifier.align(Alignment.Center),
                )
                is SectionState.Content -> SearchResultsList(
                    results = state.value,
                    filter = if (teamOnly) EntityFilter.Teams else filter,
                    followedKeys = preferences.entities.mapTo(mutableSetOf(), FollowedEntity::stableKey),
                    onAdd = onAdd,
                    onRemove = onRemove,
                )
            }
        }
    }
}

@Composable
private fun SearchPrompt(teams: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(
                if (teams) R.string.main_team_search_teams_prompt
                else R.string.main_team_search_players_prompt,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchResultsList(
    results: EntitySearchResults,
    filter: EntityFilter,
    followedKeys: Set<String>,
    onAdd: (FollowedEntity) -> Unit,
    onRemove: (String) -> Unit,
) {
    val entities: List<FollowedEntity> = when (filter) {
        EntityFilter.Teams -> results.teams
        EntityFilter.Players -> results.players
    }
    if (entities.isEmpty()) {
        DqdEmptyState(
            title = stringResource(R.string.main_team_search_empty),
            description = stringResource(R.string.main_team_search_empty_description),
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(entities, key = FollowedEntity::stableKey) { entity ->
            val followed = entity.stableKey in followedKeys
            SearchEntityRow(
                entity = entity,
                followed = followed,
                onClick = {
                    if (followed) onRemove(entity.stableKey) else onAdd(entity)
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SearchEntityRow(
    entity: FollowedEntity,
    followed: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = DqdSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FollowedEntityImage(entity, 44.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            entity.secondaryLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            painter = painterResource(if (followed) DqdIcons.Check else DqdIcons.Add),
            contentDescription = stringResource(
                if (followed) R.string.main_team_remove else R.string.main_team_add_one,
                entity.name,
            ),
            tint = if (followed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ManageEntitiesSheet(
    preferences: FollowedEntityPreferences,
    entities: List<FollowedEntity>,
    onSetMainTeam: (TeamId) -> Unit,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp, max = 680.dp)
            .padding(horizontal = DqdSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.main_team_manage),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(entities, key = FollowedEntity::stableKey) { entity ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = DqdSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DqdSpacing.md),
                ) {
                    FollowedEntityImage(entity, 42.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entity.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (entity is FollowedEntity.Team && entity.team.id == preferences.mainTeamId) {
                            Text(
                                text = stringResource(R.string.main_team_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (entity is FollowedEntity.Team) {
                        RadioButton(
                            selected = entity.team.id == preferences.mainTeamId,
                            onClick = { onSetMainTeam(entity.team.id) },
                        )
                    }
                    IconButton(onClick = { onRemove(entity.stableKey) }) {
                        Icon(
                            painterResource(DqdIcons.Close),
                            contentDescription = stringResource(R.string.main_team_remove, entity.name),
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth().padding(bottom = DqdSpacing.lg),
        ) {
            Icon(painterResource(DqdIcons.Add), contentDescription = null)
            Text(
                text = stringResource(R.string.main_team_add),
                modifier = Modifier.padding(start = DqdSpacing.sm),
            )
        }
    }
}
