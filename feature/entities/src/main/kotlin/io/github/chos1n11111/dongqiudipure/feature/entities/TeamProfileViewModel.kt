package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadGroup
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val news: SectionState<List<ArticleSummary>> = SectionState.Loading,
    val schedule: SectionState<TeamScheduleData> = SectionState.Loading,
    val squad: SectionState<List<TeamSquadGroup>> = SectionState.Loading,
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

@HiltViewModel
class TeamProfileViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()

    private var teamId: TeamId? = null
    private val jobs = mutableMapOf<String, Job>()

    fun load(id: TeamId) {
        if (teamId == id) return
        teamId = id
        loadAll(id)
    }

    fun selectTab(tab: TeamTab) {
        _uiState.update { it.copy(selectedTab = tab) }
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

    fun selectTransferWindow(windowId: String) {
        val id = teamId ?: return
        loadTransfers(id, windowId)
    }

    fun retryAll() {
        teamId?.let(::loadAll)
    }

    private fun loadAll(id: TeamId) {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
        _uiState.value = TeamProfileUiState(selectedTab = _uiState.value.selectedTab)
        jobs["profile"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamProfile(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(profile = state) }
        }
        jobs["news"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamNews(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(news = state) }
        }
        loadSchedule(id, null)
        jobs["squad"] = viewModelScope.launch {
            val state = when (val result = repository.loadTeamSquad(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(squad = state) }
        }
        loadStatistics(id, null)
        loadTransfers(id, null)
    }

    private fun loadSchedule(id: TeamId, seasonId: String?) {
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

    private fun loadTransfers(id: TeamId, windowId: String?) {
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
}
