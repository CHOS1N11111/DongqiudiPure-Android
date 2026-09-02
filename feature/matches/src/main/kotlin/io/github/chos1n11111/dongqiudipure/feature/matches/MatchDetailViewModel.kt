package io.github.chos1n11111.dongqiudipure.feature.matches

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleLineup
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MatchTab(@StringRes val labelRes: Int) {
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
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时每个 section 分别对应一次 Repository 调用，互不等待。
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.4
 */
@HiltViewModel
class MatchDetailViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    private var matchId: MatchId? = null

    fun load(id: MatchId) {
        if (matchId == id) return
        matchId = id
        loadHeader()
        loadEvents()
        loadStats()
        loadLineup()
    }

    fun selectLineupSide(side: LineupSide) {
        _uiState.update { it.copy(lineupSide = side) }
    }

    fun retryLineup() {
        _uiState.update { it.copy(lineup = SectionState.Loading) }
        loadLineup()
    }

    fun selectTab(tab: MatchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retryEvents() {
        _uiState.update { it.copy(events = SectionState.Loading) }
        loadEvents()
    }

    fun retryStats() {
        _uiState.update { it.copy(stats = SectionState.Loading) }
        loadStats()
    }

    private fun loadHeader() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。
            delay(300)
            val match = SampleMatches.matches.firstOrNull { it.id == matchId }
                ?: SampleMatches.liveMatch
            _uiState.update { it.copy(header = SectionState.Content(match)) }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            delay(600)
            val events = SampleMatches.events
            _uiState.update {
                it.copy(
                    events = if (events.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(events)
                    },
                )
            }
        }
    }

    private fun loadLineup() {
        viewModelScope.launch {
            delay(700)
            _uiState.update {
                it.copy(lineup = SectionState.Content(SampleLineup.matchLineup))
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            delay(800)
            val stats = SampleMatches.stats.sortedBy { it.displayOrder }
            _uiState.update {
                it.copy(
                    stats = if (stats.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(stats)
                    },
                )
            }
        }
    }
}
