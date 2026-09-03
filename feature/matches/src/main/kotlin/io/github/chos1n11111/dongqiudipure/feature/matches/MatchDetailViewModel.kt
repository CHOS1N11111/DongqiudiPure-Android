package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.MatchRepository
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchAnalysis
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineupBundle
import io.github.chos1n11111.dongqiudipure.core.model.MatchOverview
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.contentOrNull
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MatchTab(@param:StringRes val labelRes: Int) {
    Ratings(R.string.match_tab_ratings),
    Situation(R.string.match_tab_situation),
    Lineup(R.string.match_tab_lineup),
    Intelligence(R.string.match_tab_intelligence),
    Analysis(R.string.match_tab_analysis),
}

data class MatchDetailUiState(
    val header: SectionState<MatchSummary> = SectionState.Loading,
    val overview: SectionState<MatchOverview> = SectionState.Loading,
    val lineup: SectionState<MatchLineupBundle> = SectionState.Loading,
    val analysis: SectionState<MatchAnalysis> = SectionState.Loading,
    val userRatings: Map<PlayerId, String> = emptyMap(),
    val lineupSide: LineupSide = LineupSide.Home,
    val selectedTab: MatchTab = MatchTab.Situation,
)

/** Each upstream section is isolated so one unsupported contract cannot blank the page. */
@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val repository: MatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private var matchId: MatchId? = null
    private var userRatingsJob: Job? = null
    private var userRatingsRequestedFor: MatchId? = null

    fun load(id: MatchId) {
        if (matchId == id) return
        userRatingsJob?.cancel()
        matchId = id
        userRatingsRequestedFor = null
        _uiState.value = MatchDetailUiState()
        loadHeader()
        loadOverview()
        loadLineup()
        loadAnalysis()
    }

    fun selectLineupSide(side: LineupSide) {
        _uiState.update { it.copy(lineupSide = side) }
    }

    fun selectTab(tab: MatchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == MatchTab.Ratings) loadUserRatingsIfReady()
    }

    fun retryHeader() {
        _uiState.update { it.copy(header = SectionState.Loading) }
        loadHeader()
    }

    fun retryOverview() {
        _uiState.update { it.copy(overview = SectionState.Loading) }
        loadOverview()
    }

    fun retryLineup() {
        _uiState.update { it.copy(lineup = SectionState.Loading) }
        loadLineup()
    }

    fun retryAnalysis() {
        _uiState.update { it.copy(analysis = SectionState.Loading) }
        loadAnalysis()
    }

    private fun loadHeader() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = repository.loadMatch(id)) {
                is DataResult.Failure -> updateFor(id) {
                    it.copy(header = SectionState.Failed(result.error))
                }
                is DataResult.Success -> updateFor(id) {
                    it.copy(
                        header = result.value?.let { value -> SectionState.Content(value) }
                            ?: SectionState.Empty,
                    )
                }
            }
        }
    }

    private fun loadOverview() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = repository.loadMatchOverview(id)) {
                is DataResult.Failure -> updateFor(id) {
                    it.copy(overview = SectionState.Failed(result.error))
                }
                is DataResult.Success -> updateFor(id) {
                    it.copy(overview = SectionState.Content(result.value))
                }
            }
        }
    }

    private fun loadLineup() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = repository.loadMatchLineup(id)) {
                is DataResult.Failure -> updateFor(id) {
                    it.copy(lineup = SectionState.Failed(result.error))
                }
                is DataResult.Success -> updateFor(id) {
                    it.copy(
                        lineup = result.value?.let { value -> SectionState.Content(value) }
                            ?: SectionState.Empty,
                    )
                }
            }
            if (_uiState.value.selectedTab == MatchTab.Ratings) loadUserRatingsIfReady()
        }
    }

    private fun loadUserRatingsIfReady() {
        val id = matchId ?: return
        if (userRatingsRequestedFor == id || userRatingsJob?.isActive == true) return
        val lineup = _uiState.value.lineup.contentOrNull()?.actual ?: return
        val playerIds = listOf(lineup.home, lineup.away).flatMap { team ->
            (team.starters + team.substitutes)
                .filter { it.ratingLabel != null }
                .map { it.id }
        }.distinct()
        if (playerIds.isEmpty()) return
        userRatingsRequestedFor = id
        userRatingsJob = viewModelScope.launch {
            when (val result = repository.loadMatchUserRatings(id, playerIds)) {
                is DataResult.Failure -> if (matchId == id) {
                    userRatingsRequestedFor = null
                }
                is DataResult.Success -> updateFor(id) {
                    it.copy(userRatings = result.value)
                }
            }
        }
    }

    private fun loadAnalysis() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = repository.loadMatchAnalysis(id)) {
                is DataResult.Failure -> updateFor(id) {
                    it.copy(analysis = SectionState.Failed(result.error))
                }
                is DataResult.Success -> updateFor(id) {
                    val value = result.value
                    val hasContent = value.headToHead.isNotEmpty() || value.homeRecent.isNotEmpty() ||
                        value.awayRecent.isNotEmpty() || value.homeFuture.isNotEmpty() ||
                        value.awayFuture.isNotEmpty() || value.homeAbsentees.isNotEmpty() ||
                        value.awayAbsentees.isNotEmpty()
                    it.copy(
                        analysis = if (hasContent) SectionState.Content(value) else SectionState.Empty,
                    )
                }
            }
        }
    }

    private inline fun updateFor(
        id: MatchId,
        transform: (MatchDetailUiState) -> MatchDetailUiState,
    ) {
        _uiState.update { if (matchId == id) transform(it) else it }
    }
}
