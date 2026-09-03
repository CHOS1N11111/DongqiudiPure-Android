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
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
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
    val seasons: List<SeasonOption> = emptyList(),
    val selectedSeason: SeasonOption? = null,
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
                    seasons = emptyList(),
                    selectedSeason = null,
                    seasonLabel = "",
                    metrics = emptyList(),
                    selectedMetric = null,
                    table = if (selected == null) SectionState.Empty else SectionState.Loading,
                    statisticTable = if (selected == null) SectionState.Empty else SectionState.Loading,
                )
            }
            if (selected != null) loadCompetition(selected) else contentJob?.cancel()
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
                    seasons = emptyList(),
                    selectedSeason = null,
                    selectedSection = RankingSection.Standings,
                    metrics = emptyList(),
                    selectedMetric = null,
                    seasonLabel = "",
                    table = SectionState.Loading,
                )
            }
            loadCompetition(competition)
        }
    }

    fun selectCompetition(competition: CompetitionRef) {
        if (competition.id == _uiState.value.selectedCompetition?.id) return
        _uiState.update {
            it.copy(
                selectedCompetition = competition,
                competitionName = competition.name,
                seasons = emptyList(),
                selectedSeason = null,
                seasonLabel = "",
                metrics = emptyList(),
                selectedMetric = null,
                table = SectionState.Loading,
                statisticTable = SectionState.Loading,
            )
        }
        loadCompetition(competition)
    }

    fun selectSeason(season: SeasonOption) {
        val state = _uiState.value
        val competition = state.selectedCompetition ?: return
        if (season.id == state.selectedSeason?.id) return
        _uiState.update {
            it.copy(
                selectedSeason = season,
                seasonLabel = season.label,
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
                metrics = emptyList(),
                selectedMetric = null,
            )
        }
        loadCurrentContent(competition)
    }

    fun selectMetric(metric: RankingMetric) {
        val state = _uiState.value
        val competition = state.selectedCompetition ?: return
        val season = state.selectedSeason ?: return
        if (metric == state.selectedMetric) return
        _uiState.update {
            it.copy(selectedMetric = metric, statisticTable = SectionState.Loading)
        }
        contentJob?.cancel()
        contentJob = viewModelScope.launch {
            loadRankingInCurrentJob(competition, season, state.selectedSection, metric)
        }
    }

    fun retry() {
        val competition = _uiState.value.selectedCompetition ?: return
        if (_uiState.value.seasons.isEmpty()) loadCompetition(competition)
        else loadCurrentContent(competition)
    }

    private fun loadCompetition(competition: CompetitionRef) {
        contentJob?.cancel()
        contentJob = viewModelScope.launch {
            when (val result = repository.loadSeasons(competition.id)) {
                is DataResult.Failure -> updateIfCompetition(competition) {
                    it.copy(
                        table = SectionState.Failed(result.error),
                        statisticTable = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> {
                    val seasons = result.value
                    val selected = seasons.firstOrNull { it.isCurrent } ?: seasons.firstOrNull()
                    updateIfCompetition(competition) {
                        it.copy(
                            seasons = seasons,
                            selectedSeason = selected,
                            seasonLabel = selected?.label.orEmpty(),
                            table = if (selected == null) SectionState.Empty else SectionState.Loading,
                            statisticTable = if (selected == null) {
                                SectionState.Empty
                            } else {
                                SectionState.Loading
                            },
                        )
                    }
                    if (selected != null && isCurrent(competition, selected)) {
                        loadSectionInCurrentJob(
                            competition = competition,
                            season = selected,
                            section = _uiState.value.selectedSection,
                        )
                    }
                }
            }
        }
    }

    private fun loadCurrentContent(competition: CompetitionRef) {
        val state = _uiState.value
        val season = state.selectedSeason ?: return
        val section = state.selectedSection
        _uiState.update {
            if (section == RankingSection.Standings) {
                it.copy(table = SectionState.Loading)
            } else {
                it.copy(
                    metrics = emptyList(),
                    selectedMetric = null,
                    statisticTable = SectionState.Loading,
                )
            }
        }
        contentJob?.cancel()
        contentJob = viewModelScope.launch {
            loadSectionInCurrentJob(competition, season, section)
        }
    }

    private suspend fun loadSectionInCurrentJob(
        competition: CompetitionRef,
        season: SeasonOption,
        section: RankingSection,
    ) {
        val seasonId = SeasonId(season.id)
        if (section == RankingSection.Standings) {
            when (val result = repository.loadStandings(competition, seasonId)) {
                is DataResult.Failure -> updateIfCurrent(competition, season, section) {
                    it.copy(table = SectionState.Failed(result.error))
                }
                is DataResult.Success -> updateIfCurrent(competition, season, section) {
                    val table = result.value
                    it.copy(
                        seasonLabel = table?.seasonLabel ?: season.label,
                        table = table?.let { value -> SectionState.Content(value) }
                            ?: SectionState.Empty,
                    )
                }
            }
            return
        }

        when (val result = repository.loadRankingMetrics(competition.id, section, seasonId)) {
            is DataResult.Failure -> updateIfCurrent(competition, season, section) {
                it.copy(statisticTable = SectionState.Failed(result.error))
            }
            is DataResult.Success -> {
                val metric = result.value.firstOrNull()
                updateIfCurrent(competition, season, section) {
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
                if (metric != null && isCurrent(competition, season, section)) {
                    loadRankingInCurrentJob(competition, season, section, metric)
                }
            }
        }
    }

    private suspend fun loadRankingInCurrentJob(
        competition: CompetitionRef,
        season: SeasonOption,
        section: RankingSection,
        metric: RankingMetric,
    ) {
        when (
            val result = repository.loadRanking(
                competition = competition,
                section = section,
                metric = metric,
                seasonId = SeasonId(season.id),
            )
        ) {
            is DataResult.Failure -> updateIfCurrent(competition, season, section, metric) {
                it.copy(statisticTable = SectionState.Failed(result.error))
            }
            is DataResult.Success -> updateIfCurrent(competition, season, section, metric) {
                val table = result.value
                it.copy(
                    seasonLabel = table?.seasonLabel ?: season.label,
                    statisticTable = table?.let { value -> SectionState.Content(value) }
                        ?: SectionState.Empty,
                )
            }
        }
    }

    private fun updateIfCompetition(
        competition: CompetitionRef,
        transform: (RankingsUiState) -> RankingsUiState,
    ) {
        _uiState.update { state ->
            if (state.selectedCompetition?.id == competition.id) transform(state) else state
        }
    }

    private fun updateIfCurrent(
        competition: CompetitionRef,
        season: SeasonOption,
        section: RankingSection,
        metric: RankingMetric? = null,
        transform: (RankingsUiState) -> RankingsUiState,
    ) {
        _uiState.update { state ->
            if (!isCurrent(state, competition, season, section) ||
                (metric != null && state.selectedMetric != metric)
            ) state else transform(state)
        }
    }

    private fun isCurrent(competition: CompetitionRef, season: SeasonOption): Boolean {
        val state = _uiState.value
        return state.selectedCompetition?.id == competition.id &&
            state.selectedSeason?.id == season.id
    }

    private fun isCurrent(
        competition: CompetitionRef,
        season: SeasonOption,
        section: RankingSection,
    ): Boolean = isCurrent(_uiState.value, competition, season, section)

    private fun isCurrent(
        state: RankingsUiState,
        competition: CompetitionRef,
        season: SeasonOption,
        section: RankingSection,
    ): Boolean = state.selectedCompetition?.id == competition.id &&
        state.selectedSeason?.id == season.id && state.selectedSection == section
}
