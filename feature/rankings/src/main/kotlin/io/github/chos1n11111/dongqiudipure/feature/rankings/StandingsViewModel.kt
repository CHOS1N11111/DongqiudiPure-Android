package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.StandingsRepository
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingsUiState(
    val competitions: List<CompetitionRef> = emptyList(),
    val selectedCompetition: CompetitionRef? = null,
    val competitionName: String = "",
    val seasonLabel: String = "",
    val table: SectionState<StandingTable> = SectionState.Loading,
)

@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val repository: StandingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    private var competitionId: CompetitionId? = null
    private var loadJob: Job? = null

    fun loadHub() {
        if (_uiState.value.competitions.isNotEmpty()) return
        val competitions = repository.supportedCompetitions
        val default = competitions.firstOrNull()
        _uiState.update {
            it.copy(competitions = competitions, selectedCompetition = default)
        }
        default?.let { load(it.id) }
            ?: _uiState.update { it.copy(table = SectionState.Empty) }
    }

    fun selectCompetition(competition: CompetitionRef) {
        if (competition.id == competitionId) return
        _uiState.update { it.copy(selectedCompetition = competition) }
        load(competition.id)
    }

    fun load(id: CompetitionId) {
        if (competitionId == id && _uiState.value.table !is SectionState.Failed) return
        competitionId = id
        val supported = repository.supportedCompetitions.firstOrNull { it.id == id }
        _uiState.update {
            it.copy(
                competitions = it.competitions.ifEmpty { repository.supportedCompetitions },
                selectedCompetition = supported,
                competitionName = supported?.name.orEmpty(),
                seasonLabel = "",
                table = SectionState.Loading,
            )
        }
        loadTable(id)
    }

    fun retry() {
        val id = competitionId ?: return
        _uiState.update { it.copy(table = SectionState.Loading) }
        loadTable(id)
    }

    private fun loadTable(id: CompetitionId) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            when (val result = repository.loadStandings(id)) {
                is DataResult.Failure -> _uiState.update {
                    if (competitionId == id) it.copy(table = SectionState.Failed(result.error)) else it
                }
                is DataResult.Success -> _uiState.update {
                    if (competitionId != id) return@update it
                    val table = result.value
                    it.copy(
                        competitionName = table?.competition?.name ?: it.competitionName,
                        seasonLabel = table?.seasonLabel.orEmpty(),
                        table = table?.let { SectionState.Content(it) } ?: SectionState.Empty,
                    )
                }
            }
        }
    }
}
