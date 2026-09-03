package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHeatMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPage
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShotMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticScope
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticsData
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlayerTab(@param:StringRes val labelRes: Int) {
    Dynamic(R.string.player_tab_dynamic),
    Data(R.string.player_tab_data),
    Matches(R.string.player_tab_matches),
    Ability(R.string.player_tab_ability),
    Info(R.string.player_tab_info),
}

data class PlayerProfileUiState(
    val profile: SectionState<PlayerProfile> = SectionState.Loading,
    val overview: SectionState<PlayerOverview> = SectionState.Loading,
    val news: SectionState<List<ArticleSummary>> = SectionState.Loading,
    val statistics: SectionState<PlayerStatisticsData> = SectionState.Loading,
    val matches: SectionState<PlayerMatchPage> = SectionState.Loading,
    val ability: SectionState<PlayerAbility> = SectionState.Loading,
    val heatMap: SectionState<PlayerHeatMap> = SectionState.Loading,
    val shotMap: SectionState<PlayerShotMap> = SectionState.Loading,
    val selectedTab: PlayerTab = PlayerTab.Dynamic,
    val selectedScope: PlayerStatisticScope = PlayerStatisticScope.League,
    val selectedSeasonId: String? = null,
    val selectedCompetitionId: CompetitionId? = null,
    val selectedTeamId: TeamId? = null,
    val selectedShotMatchId: MatchId? = null,
) {
    val scopeEntries: List<PlayerStatisticEntry>
        get() = (statistics as? SectionState.Content)?.value
            ?.entries
            ?.get(selectedScope)
            .orEmpty()

    val seasonEntries: List<PlayerStatisticEntry>
        get() = scopeEntries.filter { selectedSeasonId == null || it.season.id == selectedSeasonId }

    val competitionEntries: List<PlayerStatisticEntry>
        get() = seasonEntries.filter {
            selectedCompetitionId == null || it.competition?.id == selectedCompetitionId
        }

    val selectedEntry: PlayerStatisticEntry?
        get() = competitionEntries.firstOrNull {
            selectedTeamId == null || it.team.id == selectedTeamId
        }
}

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    private var playerId: PlayerId? = null
    private val jobs = mutableMapOf<String, Job>()

    fun load(id: PlayerId) {
        if (playerId == id) return
        playerId = id
        loadAll(id)
    }

    fun selectTab(tab: PlayerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectScope(scope: PlayerStatisticScope) {
        val data = (_uiState.value.statistics as? SectionState.Content)?.value ?: return
        applySelection(data, scope, null, null, null)
    }

    fun selectSeason(seasonId: String) {
        val data = (_uiState.value.statistics as? SectionState.Content)?.value ?: return
        applySelection(data, _uiState.value.selectedScope, seasonId, null, null)
    }

    fun selectCompetition(competitionId: CompetitionId?) {
        val state = _uiState.value
        val data = (state.statistics as? SectionState.Content)?.value ?: return
        applySelection(data, state.selectedScope, state.selectedSeasonId, competitionId, null)
    }

    fun selectTeam(teamId: TeamId) {
        val state = _uiState.value
        val data = (state.statistics as? SectionState.Content)?.value ?: return
        applySelection(
            data,
            state.selectedScope,
            state.selectedSeasonId,
            state.selectedCompetitionId,
            teamId,
        )
    }

    fun selectMatchesPage(page: Int) {
        val id = playerId ?: return
        val current = (_uiState.value.matches as? SectionState.Content)?.value
        val target = page.coerceIn(1, current?.totalPages ?: 1)
        if (current?.page == target) return
        loadMatches(id, target)
    }

    fun selectShotMatch(matchId: MatchId) {
        val id = playerId ?: return
        loadShotMap(id, matchId)
    }

    fun retryAll() {
        playerId?.let(::loadAll)
    }

    private fun loadAll(id: PlayerId) {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
        _uiState.value = PlayerProfileUiState(selectedTab = _uiState.value.selectedTab)

        jobs["overview"] = viewModelScope.launch {
            when (val result = repository.loadPlayerOverview(id)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(
                        profile = SectionState.Failed(result.error),
                        overview = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> updateIfCurrent(id) { state ->
                    val overview = result.value
                    state.copy(
                        profile = overview?.profile.toSectionState(),
                        overview = overview.toSectionState(),
                    )
                }
            }
        }
        jobs["news"] = viewModelScope.launch {
            val state = when (val result = repository.loadPlayerNews(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(news = state) }
        }
        jobs["statistics"] = viewModelScope.launch {
            when (val result = repository.loadPlayerStatistics(id)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(
                        statistics = SectionState.Failed(result.error),
                        heatMap = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> {
                    updateIfCurrent(id) { it.copy(statistics = SectionState.Content(result.value)) }
                    if (playerId == id) {
                        applySelection(result.value, result.value.defaultScope, null, null, null)
                    }
                }
            }
        }
        jobs["ability"] = viewModelScope.launch {
            val state = when (val result = repository.loadPlayerAbility(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(ability = state) }
        }
        loadMatches(id, 1)
    }

    private fun applySelection(
        data: PlayerStatisticsData,
        scope: PlayerStatisticScope,
        preferredSeasonId: String?,
        preferredCompetitionId: CompetitionId?,
        preferredTeamId: TeamId?,
    ) {
        val entries = data.entries[scope].orEmpty()
        val seasonId = preferredSeasonId?.takeIf { preferred ->
            entries.any { it.season.id == preferred }
        } ?: entries.firstOrNull()?.season?.id
        val seasonEntries = entries.filter { it.season.id == seasonId }
        val competitionId = preferredCompetitionId?.takeIf { preferred ->
            seasonEntries.any { it.competition?.id == preferred }
        } ?: seasonEntries.firstOrNull()?.competition?.id
        val competitionEntries = seasonEntries.filter {
            competitionId == null || it.competition?.id == competitionId
        }
        val teamId = preferredTeamId?.takeIf { preferred ->
            competitionEntries.any { it.team.id == preferred }
        } ?: competitionEntries.firstOrNull()?.team?.id
        _uiState.update {
            it.copy(
                selectedScope = scope,
                selectedSeasonId = seasonId,
                selectedCompetitionId = competitionId,
                selectedTeamId = teamId,
            )
        }
        competitionEntries.firstOrNull { it.team.id == teamId }?.let(::loadHeatMap)
            ?: _uiState.update { it.copy(heatMap = SectionState.Empty) }
    }

    private fun loadHeatMap(entry: PlayerStatisticEntry) {
        val id = playerId ?: return
        jobs["heat"]?.cancel()
        _uiState.update { it.copy(heatMap = SectionState.Loading) }
        jobs["heat"] = viewModelScope.launch {
            val state = when (
                val result = repository.loadPlayerHeatMap(id, entry.season.id, entry.team.id)
            ) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState { it.points.isNotEmpty() }
            }
            updateIfCurrent(id) { current ->
                if (current.selectedEntry?.id == entry.id) current.copy(heatMap = state) else current
            }
        }
    }

    private fun loadMatches(id: PlayerId, page: Int) {
        jobs["matches"]?.cancel()
        _uiState.update { it.copy(matches = SectionState.Loading) }
        jobs["matches"] = viewModelScope.launch {
            when (val result = repository.loadPlayerMatches(id, page)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(matches = SectionState.Failed(result.error))
                }
                is DataResult.Success -> {
                    val state = result.value.toSectionState { it.matches.isNotEmpty() }
                    updateIfCurrent(id) { it.copy(matches = state) }
                    val shotMatch = result.value.matches.firstOrNull { (it.goals ?: 0) > 0 }
                        ?: result.value.matches.firstOrNull()
                    if (shotMatch != null && playerId == id) {
                        loadShotMap(id, shotMatch.match.id)
                    } else {
                        updateIfCurrent(id) { it.copy(shotMap = SectionState.Empty) }
                    }
                }
            }
        }
    }

    private fun loadShotMap(id: PlayerId, matchId: MatchId) {
        jobs["shot"]?.cancel()
        _uiState.update {
            it.copy(selectedShotMatchId = matchId, shotMap = SectionState.Loading)
        }
        jobs["shot"] = viewModelScope.launch {
            val state = when (val result = repository.loadPlayerShotMap(id, matchId)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState {
                    it.summary != null || it.shots.isNotEmpty()
                }
            }
            updateIfCurrent(id) { current ->
                if (current.selectedShotMatchId == matchId) current.copy(shotMap = state) else current
            }
        }
    }

    private fun updateIfCurrent(
        id: PlayerId,
        transform: (PlayerProfileUiState) -> PlayerProfileUiState,
    ) {
        _uiState.update { if (playerId == id) transform(it) else it }
    }

    private fun <T> T?.toSectionState(): SectionState<T> =
        this?.let { SectionState.Content(it) } ?: SectionState.Empty

    private fun <T> List<T>.toSectionState(): SectionState<List<T>> =
        if (isEmpty()) SectionState.Empty else SectionState.Content(this)

    private fun <T> T.toSectionState(hasContent: (T) -> Boolean): SectionState<T> =
        if (hasContent(this)) SectionState.Content(this) else SectionState.Empty
}
