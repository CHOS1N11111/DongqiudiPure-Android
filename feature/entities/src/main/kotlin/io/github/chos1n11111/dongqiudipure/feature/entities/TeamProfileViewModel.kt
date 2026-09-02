package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TeamTab(@param:StringRes val labelRes: Int) {
    Overview(R.string.team_tab_overview),
    Fixtures(R.string.team_tab_fixtures),
    Stats(R.string.team_tab_stats),
    Squad(R.string.team_tab_squad),
}

data class SeasonStat(
    val label: String,
    val value: String?,
)

data class TeamProfileUiState(
    val profile: SectionState<TeamProfile> = SectionState.Loading,
    val seasonStats: SectionState<List<SeasonStat>> = SectionState.Loading,
    val nextMatch: SectionState<MatchSummary> = SectionState.Loading,
    val squad: SectionState<List<SquadMember>> = SectionState.Loading,
    val fixtures: SectionState<List<MatchSummary>> = SectionState.Loading,
    val detailedStats: SectionState<List<PlayerSeasonStat>> = SectionState.Loading,
    val selectedTab: TeamTab = TeamTab.Overview,
)

@HiltViewModel
class TeamProfileViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()

    private var teamId: TeamId? = null
    private val jobs = mutableListOf<Job>()

    fun load(id: TeamId) {
        if (teamId == id) return
        teamId = id
        loadAll(id)
    }

    fun selectTab(tab: TeamTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retryAll() {
        teamId?.let(::loadAll)
    }

    private fun loadAll(id: TeamId) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        _uiState.update {
            it.copy(
                profile = SectionState.Loading,
                seasonStats = SectionState.Loading,
                nextMatch = SectionState.Loading,
                squad = SectionState.Loading,
                fixtures = SectionState.Loading,
                detailedStats = SectionState.Loading,
            )
        }
        jobs += viewModelScope.launch {
            val state = when (val result = repository.loadTeamProfile(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value?.let { SectionState.Content(it) }
                    ?: SectionState.Empty
            }
            updateIfCurrent(id) { it.copy(profile = state) }
        }
        jobs += viewModelScope.launch {
            when (val result = repository.loadTeamStatistics(id)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(
                        seasonStats = SectionState.Failed(result.error),
                        detailedStats = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> {
                    val statistics = result.value
                    val summary = statistics?.let {
                        listOf(
                            SeasonStat("赛季", it.seasonLabel),
                            SeasonStat("排名", it.rankLabel),
                            SeasonStat("战绩", it.recordLabel),
                        )
                    }.orEmpty()
                    val detail = statistics?.categories.orEmpty().flatMap { it.values }
                    updateIfCurrent(id) {
                        it.copy(
                            seasonStats = summary.toSectionState(),
                            detailedStats = detail.toSectionState(),
                        )
                    }
                }
            }
        }
        jobs += viewModelScope.launch {
            when (val result = repository.loadTeamSchedule(id)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(
                        nextMatch = SectionState.Failed(result.error),
                        fixtures = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> {
                    val fixtures = result.value
                    val next = fixtures.firstOrNull { it.status is MatchStatus.NotStarted }
                    updateIfCurrent(id) {
                        it.copy(
                            fixtures = fixtures.toSectionState(),
                            nextMatch = next?.let { value -> SectionState.Content(value) }
                                ?: SectionState.Empty,
                        )
                    }
                }
            }
        }
        jobs += viewModelScope.launch {
            val state = when (val result = repository.loadTeamSquad(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(squad = state) }
        }
    }

    private fun updateIfCurrent(id: TeamId, transform: (TeamProfileUiState) -> TeamProfileUiState) {
        _uiState.update { if (teamId == id) transform(it) else it }
    }

    private fun <T> List<T>.toSectionState(): SectionState<List<T>> =
        if (isEmpty()) SectionState.Empty else SectionState.Content(this)
}
