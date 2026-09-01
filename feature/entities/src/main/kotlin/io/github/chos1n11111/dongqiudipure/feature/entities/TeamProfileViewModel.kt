package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TeamTab(val label: String) {
    Overview("资料"),
    Squad("阵容"),
    Fixtures("赛程"),
    Stats("数据"),
    News("资讯"),
}

/** 一项赛季统计。[value] 为 null 表示该赛事未提供，不等于 0。 */
data class SeasonStat(
    val label: String,
    val value: String?,
)

data class TeamProfileUiState(
    val profile: SectionState<TeamProfile> = SectionState.Loading,
    val seasonStats: SectionState<List<SeasonStat>> = SectionState.Loading,
    val nextMatch: SectionState<MatchSummary> = SectionState.Loading,
    val selectedTab: TeamTab = TeamTab.Overview,
)

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 *
 * 接入时的关键约束（PLAN.md M7）：Repository **不得按热门名单分支**。
 * 本页面必须能接收任意 [TeamId] —— 范围外的球队让各 section 分别降级，
 * 而不是整页显示「不支持该球队」。这是 M10 主队入口能复用同一条链路的前提。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.6
 */
class TeamProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()

    private var teamId: TeamId? = null

    fun load(id: TeamId) {
        if (teamId == id) return
        teamId = id
        loadProfile()
        loadSeasonStats()
        loadNextMatch()
    }

    fun selectTab(tab: TeamTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retryAll() {
        _uiState.update {
            it.copy(
                profile = SectionState.Loading,
                seasonStats = SectionState.Loading,
                nextMatch = SectionState.Loading,
            )
        }
        loadProfile()
        loadSeasonStats()
        loadNextMatch()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            delay(350)
            _uiState.update {
                it.copy(profile = SectionState.Content(SampleMatches.teamProfile))
            }
        }
    }

    private fun loadSeasonStats() {
        viewModelScope.launch {
            delay(650)
            _uiState.update {
                it.copy(
                    seasonStats = SectionState.Content(
                        listOf(
                            SeasonStat("积分", "10"),
                            SeasonStat("进球", "11"),
                            // 该赛事未提供预期进球。必须保持 null。
                            SeasonStat("预期进球", null),
                        ),
                    ),
                )
            }
        }
    }

    private fun loadNextMatch() {
        viewModelScope.launch {
            delay(500)
            val next = SampleMatches.matches.firstOrNull {
                it.status is io.github.chos1n11111.dongqiudipure.core.model.MatchStatus.NotStarted
            }
            _uiState.update {
                it.copy(
                    nextMatch = if (next == null) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(next)
                    },
                )
            }
        }
    }
}
