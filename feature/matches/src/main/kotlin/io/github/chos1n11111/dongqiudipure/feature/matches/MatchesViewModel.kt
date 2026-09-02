package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.MatchRepository
import io.github.chos1n11111.dongqiudipure.core.data.FootballCatalogRepository
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.needsLiveRefresh
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日期条的一格。
 *
 * 只带原始日期与标记，**不带已格式化的文案** ——
 * 「周一」「今天」这类展示文本由 UI 层按用户语言与时区生成
 * （ARCHITECTURE.md §5.2：只有 UI 层做格式化）。
 */
data class MatchDay(
    val date: LocalDate,
    val isToday: Boolean,
    val hasLiveMatch: Boolean,
)

/** 按赛事分组后的比赛。列表用分组标题分隔，而不是混排。 */
data class CompetitionGroup(
    val competition: CompetitionRef,
    val matches: List<MatchSummary>,
)

data class MatchesUiState(
    val days: List<MatchDay> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val extraCompetitions: List<CompetitionRef> = emptyList(),
    val selectedCompetition: CompetitionRef? = null,
    val groups: SectionState<List<CompetitionGroup>> = SectionState.Loading,
)

/**
 * 比赛列表状态编排。
 *
 * “重要”聚合预设赛事，自选赛事通过赛季赛程接口读取。
 * 接口没有支持赛事或当天没有比赛时返回空状态，不补造比赛。
 *
 * 实时刷新策略（可取消、感知前后台、终场停止）属于 M4/M5，
 * 当前仅根据已加载比赛维护 [hasLiveMatch]；自动轮询仍属于后续实时能力。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.3
 */
@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val repository: MatchRepository,
    private val catalogRepository: FootballCatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var catalogJob: Job? = null
    private var configuredCompetitionIds: Set<String>? = null
    private var configuredDefaultCompetitionId: String? = null
    private var initialDefaultApplied = false

    init {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(days = buildDays(today), selectedDate = today)
        }
        loadMatches()
    }

    fun selectDate(date: LocalDate) {
        if (date == _uiState.value.selectedDate) return
        _uiState.update { it.copy(selectedDate = date, groups = SectionState.Loading) }
        loadMatches()
    }

    fun configureCompetitions(ids: Set<String>, defaultCompetitionId: String?) {
        if (configuredCompetitionIds == ids &&
            configuredDefaultCompetitionId == defaultCompetitionId &&
            initialDefaultApplied
        ) return
        val shouldApplyDefault = !initialDefaultApplied ||
            configuredDefaultCompetitionId != defaultCompetitionId
        configuredCompetitionIds = ids
        configuredDefaultCompetitionId = defaultCompetitionId
        catalogJob?.cancel()
        if (ids.isEmpty()) {
            val changedSelection = _uiState.value.selectedCompetition != null
            _uiState.update {
                it.copy(extraCompetitions = emptyList(), selectedCompetition = null)
            }
            initialDefaultApplied = true
            if (changedSelection) loadMatches()
            return
        }
        catalogJob = viewModelScope.launch {
            val result = catalogRepository.loadCompetitionCatalog()
            if (configuredCompetitionIds != ids ||
                configuredDefaultCompetitionId != defaultCompetitionId ||
                result !is DataResult.Success
            ) return@launch
            val competitions = result.value
                .flatMap { it.competitions }
                .filter { it.id.raw in ids }
            val current = _uiState.value.selectedCompetition
            val selected = when {
                shouldApplyDefault -> competitions.firstOrNull {
                    it.id.raw == defaultCompetitionId
                }
                current != null -> competitions.firstOrNull { it.id == current.id }
                else -> null
            }
            initialDefaultApplied = true
            val selectionChanged = selected?.id != current?.id
            _uiState.update {
                it.copy(extraCompetitions = competitions, selectedCompetition = selected)
            }
            if (selectionChanged) loadMatches()
        }
    }

    fun selectCompetition(competition: CompetitionRef?) {
        if (competition?.id == _uiState.value.selectedCompetition?.id) return
        _uiState.update { it.copy(selectedCompetition = competition, groups = SectionState.Loading) }
        loadMatches()
    }

    fun retry() {
        _uiState.update { it.copy(groups = SectionState.Loading) }
        loadMatches()
    }

    private fun loadMatches() {
        loadJob?.cancel()
        val selectedDate = _uiState.value.selectedDate
        val selectedCompetition = _uiState.value.selectedCompetition
        loadJob = viewModelScope.launch {
            when (val result = repository.loadMatches(selectedDate, selectedCompetition)) {
                is DataResult.Failure -> _uiState.update {
                    if (it.selectedDate == selectedDate &&
                        it.selectedCompetition?.id == selectedCompetition?.id
                    ) {
                        it.copy(groups = SectionState.Failed(result.error))
                    } else {
                        it
                    }
                }
                is DataResult.Success -> {
                    val grouped = result.value
                        .groupBy { it.competition.id }
                        .map { (_, matches) -> CompetitionGroup(matches.first().competition, matches) }
                    val hasLive = result.value.any { it.status.needsLiveRefresh }
                    _uiState.update {
                        if (it.selectedDate != selectedDate ||
                            it.selectedCompetition?.id != selectedCompetition?.id
                        ) return@update it
                        it.copy(
                            days = it.days.map { day ->
                                if (day.date == selectedDate) day.copy(hasLiveMatch = hasLive) else day
                            },
                            groups = if (grouped.isEmpty()) {
                                SectionState.Empty
                            } else {
                                SectionState.Content(grouped)
                            },
                        )
                    }
                }
            }
        }
    }

    private fun buildDays(today: LocalDate): List<MatchDay> =
        (-3..3).map { offset ->
            val date = today.plusDays(offset.toLong())
            MatchDay(
                date = date,
                isToday = offset == 0,
                hasLiveMatch = false,
            )
        }
}
