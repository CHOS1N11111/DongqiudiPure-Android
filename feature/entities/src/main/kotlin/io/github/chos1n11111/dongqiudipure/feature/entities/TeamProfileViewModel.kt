package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamScheduleData
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadData
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TeamTab(@param:StringRes val labelRes: Int) {
    Dynamic(R.string.team_tab_dynamic),
    Schedule(R.string.team_tab_fixtures),
    Data(R.string.team_tab_stats),
    Players(R.string.team_tab_players),
    Info(R.string.team_tab_overview),
    Transfers(R.string.team_tab_transfers),
}

enum class TeamScheduleFilter(@param:StringRes val labelRes: Int) {
    Past(R.string.team_schedule_past),
    Future(R.string.team_schedule_future),
}

data class TeamProfileUiState(
    val profile: SectionState<TeamProfile> = SectionState.Loading,
    val schedule: SectionState<TeamScheduleData> = SectionState.Loading,
    val squad: SectionState<TeamSquadData> = SectionState.Loading,
    val statistics: SectionState<TeamStatistics> = SectionState.Loading,
    val transfers: SectionState<TeamTransferData> = SectionState.Loading,
    val selectedTab: TeamTab = TeamTab.Dynamic,
    val scheduleFilter: TeamScheduleFilter = TeamScheduleFilter.Future,
    val selectedCompetitionId: CompetitionId? = null,
    val schedulePage: Int = 1,
) {
    val filteredMatches: List<MatchSummary>
        get() {
            val content = (schedule as? SectionState.Content)?.value ?: return emptyList()
            return content.matches.filter { match ->
                val competitionMatches = selectedCompetitionId == null ||
                    match.competition.id == selectedCompetitionId
                val timeMatches = when (scheduleFilter) {
                    TeamScheduleFilter.Past -> match.status is MatchStatus.Finished ||
                        match.status is MatchStatus.Cancelled || match.status is MatchStatus.Postponed
                    TeamScheduleFilter.Future -> match.status !is MatchStatus.Finished &&
                        match.status !is MatchStatus.Cancelled && match.status !is MatchStatus.Postponed
                }
                competitionMatches && timeMatches
            }.let { matches ->
                if (scheduleFilter == TeamScheduleFilter.Past) matches.reversed() else matches
            }
        }

    val schedulePageCount: Int
        get() = ((filteredMatches.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)

    val visibleMatches: List<MatchSummary>
        get() = filteredMatches.drop((schedulePage - 1) * PAGE_SIZE).take(PAGE_SIZE)

    companion object {
        const val PAGE_SIZE = 15
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamProfileViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()
    private val newsTeamId = MutableStateFlow<TeamId?>(null)
    val news: Flow<PagingData<ArticleSummary>> = newsTeamId
        .filterNotNull()
        .flatMapLatest { repository.pagedTeamNews(it) }
        .cachedIn(viewModelScope)

    private var teamId: TeamId? = null
    private var scheduleSeasonId: String? = null
    private var statisticsSeasonId: String? = null
    private var squadSeasonId: String? = null
    private var transferWindowId: String? = null
    private val jobs = mutableMapOf<String, Job>()

    fun load(id: TeamId) {
        if (teamId == id) return
        teamId = id
        newsTeamId.value = id
        loadAll(id)
    }

    fun selectTab(tab: TeamTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab.hasFailure(_uiState.value)) retryTab(tab)
    }

    fun selectScheduleFilter(filter: TeamScheduleFilter) {
        _uiState.update { it.copy(scheduleFilter = filter, schedulePage = 1) }
    }

    fun selectCompetition(id: CompetitionId?) {
        _uiState.update { it.copy(selectedCompetitionId = id, schedulePage = 1) }
    }

    fun selectSchedulePage(page: Int) {
        _uiState.update { state ->
            state.copy(schedulePage = page.coerceIn(1, state.schedulePageCount))
        }
    }

    fun selectScheduleSeason(seasonId: String) {
        val id = teamId ?: return
        loadSchedule(id, seasonId)
    }

    fun selectStatisticsSeason(seasonId: String) {
        val id = teamId ?: return
        loadStatistics(id, seasonId)
    }

    fun selectSquadSeason(seasonId: String) {
        val id = teamId ?: return
        loadSquad(id, seasonId)
    }

    fun selectTransferWindow(windowId: String) {
        val id = teamId ?: return
        loadTransfers(id, windowId)
    }

    fun retryAll() {
        val id = teamId ?: return
        loadProfile(id)
        retryTab(_uiState.value.selectedTab)
    }

    fun retrySelectedTab() {
        retryTab(_uiState.value.selectedTab)
    }

    private fun loadAll(id: TeamId) {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
        scheduleSeasonId = null
        statisticsSeasonId = null
        squadSeasonId = null
        transferWindowId = null
        _uiState.value = TeamProfileUiState(selectedTab = _uiState.value.selectedTab)
        loadProfile(id)
        loadSchedule(id, null)
        loadSquad(id, null)
        loadStatistics(id, null)
        loadTransfers(id, null)
    }

    private fun loadProfile(id: TeamId) {
        jobs["profile"]?.cancel()
        _uiState.update { it.copy(profile = SectionState.Loading) }
        jobs["profile"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamProfile(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(profile = state) }
        }
    }

    private fun loadSchedule(id: TeamId, seasonId: String?) {
        scheduleSeasonId = seasonId
        jobs["schedule"]?.cancel()
        _uiState.update {
            it.copy(
                schedule = SectionState.Loading,
                selectedCompetitionId = null,
                schedulePage = 1,
            )
        }
        jobs["schedule"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamSchedule(id, seasonId)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState { it.matches.isNotEmpty() }
            }
            updateIfCurrent(id) { it.copy(schedule = state) }
        }
    }

    private fun loadStatistics(id: TeamId, seasonId: String?) {
        statisticsSeasonId = seasonId
        jobs["statistics"]?.cancel()
        _uiState.update { it.copy(statistics = SectionState.Loading) }
        jobs["statistics"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamStatistics(id, seasonId)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(statistics = state) }
        }
    }

    private fun loadSquad(id: TeamId, seasonId: String?) {
        squadSeasonId = seasonId
        jobs["squad"]?.cancel()
        _uiState.update { it.copy(squad = SectionState.Loading) }
        jobs["squad"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamSquad(id, seasonId)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState { it.groups.isNotEmpty() }
            }
            updateIfCurrent(id) { it.copy(squad = state) }
        }
    }

    private fun loadTransfers(id: TeamId, windowId: String?) {
        transferWindowId = windowId
        jobs["transfers"]?.cancel()
        _uiState.update { it.copy(transfers = SectionState.Loading) }
        jobs["transfers"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamTransfers(id, windowId)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState {
                    it.windows.isNotEmpty() || it.groups.isNotEmpty()
                }
            }
            updateIfCurrent(id) { it.copy(transfers = state) }
        }
    }

    private fun updateIfCurrent(id: TeamId, transform: (TeamProfileUiState) -> TeamProfileUiState) {
        _uiState.update { if (teamId == id) transform(it) else it }
    }

    private fun <T> T?.toSectionState(): SectionState<T> =
        this?.let { SectionState.Content(it) } ?: SectionState.Empty

    private fun <T> List<T>.toSectionState(): SectionState<List<T>> =
        if (isEmpty()) SectionState.Empty else SectionState.Content(this)

    private fun <T> T.toSectionState(hasContent: (T) -> Boolean): SectionState<T> =
        if (hasContent(this)) SectionState.Content(this) else SectionState.Empty

    private fun retryTab(tab: TeamTab) {
        val id = teamId ?: return
        when (tab) {
            TeamTab.Dynamic -> Unit
            TeamTab.Schedule -> loadSchedule(id, scheduleSeasonId)
            TeamTab.Data -> loadStatistics(id, statisticsSeasonId)
            TeamTab.Players -> loadSquad(id, squadSeasonId)
            TeamTab.Info -> loadProfile(id)
            TeamTab.Transfers -> loadTransfers(id, transferWindowId)
        }
    }

    private fun TeamTab.hasFailure(state: TeamProfileUiState): Boolean = when (this) {
        TeamTab.Dynamic -> false
        TeamTab.Schedule -> state.schedule is SectionState.Failed
        TeamTab.Data -> state.statistics is SectionState.Failed
        TeamTab.Players -> state.squad is SectionState.Failed
        TeamTab.Info -> state.profile is SectionState.Failed
        TeamTab.Transfers -> state.transfers is SectionState.Failed
    }
}
