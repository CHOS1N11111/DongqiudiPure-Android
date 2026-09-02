package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballCatalogRepository
import io.github.chos1n11111.dongqiudipure.core.data.StandingsRepository
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingSection
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
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
    val selectedSection: RankingSection = RankingSection.Standings,
    val metrics: List<RankingMetric> = emptyList(),
    val selectedMetric: RankingMetric? = null,
    val competitionName: String = "",
    val seasonLabel: String = "",
    val table: SectionState<StandingTable> = SectionState.Loading,
    val statisticTable: SectionState<StatisticRankingTable> = SectionState.Loading,
)

@HiltViewModel
class StandingsViewModel @Inject constructor(
    private val repository: StandingsRepository,
    private val catalogRepository: FootballCatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    private var configuredCompetitionIds: Set<String>? = null
    private var catalogJob: Job? = null
    private var contentJob: Job? = null

    fun loadHub(selectedIds: Set<String>) {
        if (configuredCompetitionIds == selectedIds) return
        configuredCompetitionIds = selectedIds
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            val catalog = when (val result = catalogRepository.loadCompetitionCatalog()) {
                is DataResult.Success -> result.value.flatMap { it.competitions }
                is DataResult.Failure -> repository.defaultCompetitions
            }
            if (configuredCompetitionIds != selectedIds) return@launch
            val competitions = catalog
                .distinctBy { it.id }
                .filter { it.id.raw in selectedIds }
            val current = _uiState.value.selectedCompetition
            val selected = competitions.firstOrNull { it.id == current?.id }
                ?: competitions.firstOrNull()
            _uiState.update {
                it.copy(
                    competitions = competitions,
                    selectedCompetition = selected,
                    competitionName = selected?.name.orEmpty(),
                    seasonLabel = "",
                    table = if (selected == null) SectionState.Empty else SectionState.Loading,
                    statisticTable = if (selected == null) SectionState.Empty else SectionState.Loading,
                )
            }
            if (selected != null) loadCurrentContent(selected) else contentJob?.cancel()
        }
    }

    fun load(id: CompetitionId) {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch {
            val catalogCompetition = when (val result = catalogRepository.loadCompetitionCatalog()) {
                is DataResult.Success -> result.value
                    .flatMap { it.competitions }
                    .firstOrNull { it.id == id }
                is DataResult.Failure -> null
            }
            val competition = catalogCompetition
                ?: repository.defaultCompetitions.firstOrNull { it.id == id }
                ?: CompetitionRef(id, id.raw, null)
            _uiState.update {
                it.copy(
                    selectedCompetition = competition,
                    competitionName = competition.name,
                    selectedSection = RankingSection.Standings,
                    table = SectionState.Loading,
                )
            }
            loadCurrentContent(competition)
        }
    }

    fun selectCompetition(competition: CompetitionRef) {
        if (competition.id == _uiState.value.selectedCompetition?.id) return
        _uiState.update {
            it.copy(
                selectedCompetition = competition,
                competitionName = competition.name,
                seasonLabel = "",
                metrics = emptyList(),
                selectedMetric = null,
            )
        }
        loadCurrentContent(competition)
    }

    fun selectSection(section: RankingSection) {
        if (section == _uiState.value.selectedSection) return
        val competition = _uiState.value.selectedCompetition ?: return
        _uiState.update {
            it.copy(
                selectedSection = section,
                seasonLabel = "",
                metrics = emptyList(),
                selectedMetric = null,
            )
        }
        loadCurrentContent(competition)
    }

    fun selectMetric(metric: RankingMetric) {
        val state = _uiState.value
        val competition = state.selectedCompetition ?: return
        if (metric == state.selectedMetric) return
        _uiState.update {
            it.copy(selectedMetric = metric, statisticTable = SectionState.Loading)
        }
        loadStatisticRanking(competition, state.selectedSection, metric)
    }

    fun retry() {
        _uiState.value.selectedCompetition?.let(::loadCurrentContent)
    }

    private fun loadCurrentContent(competition: CompetitionRef) {
        contentJob?.cancel()
        val section = _uiState.value.selectedSection
        if (section == RankingSection.Standings) {
            _uiState.update { it.copy(table = SectionState.Loading) }
            contentJob = viewModelScope.launch {
                when (val result = repository.loadStandings(competition)) {
                    is DataResult.Failure -> updateIfCurrent(competition, section) {
                        it.copy(table = SectionState.Failed(result.error))
                    }
                    is DataResult.Success -> updateIfCurrent(competition, section) {
                        val table = result.value
                        it.copy(
                            seasonLabel = table?.seasonLabel.orEmpty(),
                            table = table?.let { value -> SectionState.Content(value) }
                                ?: SectionState.Empty,
                        )
                    }
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    metrics = emptyList(),
                    selectedMetric = null,
                    statisticTable = SectionState.Loading,
                )
            }
            contentJob = viewModelScope.launch {
                when (val result = repository.loadRankingMetrics(competition.id, section)) {
                    is DataResult.Failure -> updateIfCurrent(competition, section) {
                        it.copy(statisticTable = SectionState.Failed(result.error))
                    }
                    is DataResult.Success -> {
                        val metric = result.value.firstOrNull()
                        updateIfCurrent(competition, section) {
                            it.copy(
                                metrics = result.value,
                                selectedMetric = metric,
                                statisticTable = if (metric == null) {
                                    SectionState.Empty
                                } else {
                                    SectionState.Loading
                                },
                            )
                        }
                        if (metric != null && isCurrent(competition, section)) {
                            loadRankingInCurrentJob(competition, section, metric)
                        }
                    }
                }
            }
        }
    }

    private fun loadStatisticRanking(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric,
    ) {
        contentJob?.cancel()
        contentJob = viewModelScope.launch {
            loadRankingInCurrentJob(competition, section, metric)
        }
    }

    private suspend fun loadRankingInCurrentJob(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric,
    ) {
        when (val result = repository.loadRanking(competition, section, metric)) {
            is DataResult.Failure -> updateIfCurrent(competition, section, metric) {
                it.copy(statisticTable = SectionState.Failed(result.error))
            }
            is DataResult.Success -> updateIfCurrent(competition, section, metric) {
                val table = result.value
                it.copy(
                    seasonLabel = table?.seasonLabel.orEmpty(),
                    statisticTable = table?.let { value -> SectionState.Content(value) }
                        ?: SectionState.Empty,
                )
            }
        }
    }

    private fun updateIfCurrent(
        competition: CompetitionRef,
        section: RankingSection,
        metric: RankingMetric? = null,
        transform: (RankingsUiState) -> RankingsUiState,
    ) {
        _uiState.update { state ->
            if (!isCurrent(state, competition, section) ||
                (metric != null && state.selectedMetric != metric)
            ) state else transform(state)
        }
    }

    private fun isCurrent(competition: CompetitionRef, section: RankingSection): Boolean =
        isCurrent(_uiState.value, competition, section)

    private fun isCurrent(
        state: RankingsUiState,
        competition: CompetitionRef,
        section: RankingSection,
    ): Boolean = state.selectedCompetition?.id == competition.id && state.selectedSection == section
}
