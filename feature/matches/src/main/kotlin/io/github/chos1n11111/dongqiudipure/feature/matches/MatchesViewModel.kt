package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.needsLiveRefresh
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 日期条的一格。 */
data class MatchDay(
    val date: LocalDate,
    val dayLabel: String,
    val weekdayLabel: String,
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
    val groups: SectionState<List<CompetitionGroup>> = SectionState.Loading,
)

/**
 * 比赛列表状态编排。
 *
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时替换为 `matchRepository.observeMatchesByDate(date)`。
 *
 * 实时刷新策略（可取消、感知前后台、终场停止）属于 M4/M5，
 * 需要真实数据源才有意义，此处只保留 [hasLiveMatch] 的判定逻辑。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §比赛
 */
class MatchesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

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

    fun retry() {
        _uiState.update { it.copy(groups = SectionState.Loading) }
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。
            delay(SAMPLE_LOAD_DELAY_MS)
            val grouped = SampleMatches.matches
                .groupBy { it.competition }
                .map { (competition, matches) -> CompetitionGroup(competition, matches) }

            _uiState.update {
                it.copy(
                    groups = if (grouped.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(grouped)
                    },
                )
            }
        }
    }

    private fun buildDays(today: LocalDate): List<MatchDay> =
        (-3..3).map { offset ->
            val date = today.plusDays(offset.toLong())
            MatchDay(
                date = date,
                dayLabel = date.dayOfMonth.toString(),
                weekdayLabel = if (offset == 0) "今天" else weekdayOf(date),
                // TODO(data): 真实实现应由服务端的当日比赛状态决定，而不是示例数据。
                hasLiveMatch = offset == -1 ||
                    (offset == 0 && SampleMatches.matches.any { it.status.needsLiveRefresh }),
            )
        }

    private fun weekdayOf(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }

    private companion object {
        const val SAMPLE_LOAD_DELAY_MS = 600L
    }
}
