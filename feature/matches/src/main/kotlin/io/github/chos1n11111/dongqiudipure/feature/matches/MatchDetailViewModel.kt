package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.MatchRepository
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MatchTab(@param:StringRes val labelRes: Int) {
    Events(R.string.match_tab_events),
    Lineup(R.string.match_tab_lineup),
    Stats(R.string.match_tab_stats),
}

/**
 * 比赛详情状态。
 *
 * **每个 section 一个独立的 [SectionState]** —— 这是失败隔离的结构前提。
 * 事件接口失效时统计照常显示，反之亦然
 * （PLAN.md M5：「每个详情 section 独立加载、刷新、缓存和失败」）。
 */
data class MatchDetailUiState(
    val header: SectionState<MatchSummary> = SectionState.Loading,
    val events: SectionState<List<MatchEvent>> = SectionState.Loading,
    val stats: SectionState<List<StatItem>> = SectionState.Loading,
    val lineup: SectionState<MatchLineup> = SectionState.Loading,
    val lineupSide: LineupSide = LineupSide.Home,
    val selectedTab: MatchTab = MatchTab.Events,
)

/**
 * 比分头复用比赛列表的真实数据；尚未验证 contract 的事件、阵容和统计
 * 明确显示为空，不以样例内容填充。
 */
@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val repository: MatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private var matchId: MatchId? = null

    fun load(id: MatchId) {
        if (matchId == id) return
        matchId = id
        _uiState.value = MatchDetailUiState(
            events = SectionState.Empty,
            stats = SectionState.Empty,
            lineup = SectionState.Empty,
        )
        loadHeader()
    }

    fun selectLineupSide(side: LineupSide) {
        _uiState.update { it.copy(lineupSide = side) }
    }

    fun retryLineup() {
        _uiState.update { it.copy(lineup = SectionState.Empty) }
    }

    fun selectTab(tab: MatchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retryEvents() {
        _uiState.update { it.copy(events = SectionState.Empty) }
    }

    fun retryStats() {
        _uiState.update { it.copy(stats = SectionState.Empty) }
    }

    fun retryHeader() {
        _uiState.update { it.copy(header = SectionState.Loading) }
        loadHeader()
    }

    private fun loadHeader() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = repository.loadMatch(id)) {
                is DataResult.Failure -> _uiState.update {
                    if (matchId == id) it.copy(header = SectionState.Failed(result.error)) else it
                }
                is DataResult.Success -> _uiState.update {
                    if (matchId != id) return@update it
                    it.copy(
                        header = result.value?.let { SectionState.Content(it) } ?: SectionState.Empty,
                    )
                }
            }
        }
    }
}
