package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class TeamTab(@param:StringRes val labelRes: Int) {
    Overview(R.string.team_tab_overview),
    Squad(R.string.team_tab_squad),
    Fixtures(R.string.team_tab_fixtures),
    Stats(R.string.team_tab_stats),
    News(R.string.team_tab_news),
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
    val squad: SectionState<List<SquadMember>> = SectionState.Loading,
    val fixtures: SectionState<List<MatchSummary>> = SectionState.Loading,
    val detailedStats: SectionState<List<PlayerSeasonStat>> = SectionState.Loading,
    val news: SectionState<List<ArticleSummary>> = SectionState.Loading,
    val selectedTab: TeamTab = TeamTab.Overview,
)

/**
 * 球队资料 contract 尚未接入，当前各 section 明确显示为空，不填充样例数据。
 *
 * 接入时的关键约束（PLAN.md M7）：Repository **不得按热门名单分支**。
 * 本页面必须能接收任意 [TeamId] —— 范围外的球队让各 section 分别降级，
 * 而不是整页显示「不支持该球队」。这是 M10 主队入口能复用同一条链路的前提。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.6
 */
@HiltViewModel
class TeamProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TeamProfileUiState())
    val uiState: StateFlow<TeamProfileUiState> = _uiState.asStateFlow()

    private var teamId: TeamId? = null

    fun load(id: TeamId) {
        if (teamId == id) return
        teamId = id
        clearUnavailableData()
    }

    fun selectTab(tab: TeamTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retryAll() {
        clearUnavailableData()
    }

    private fun clearUnavailableData() {
        _uiState.update {
            it.copy(
                profile = SectionState.Empty,
                seasonStats = SectionState.Empty,
                nextMatch = SectionState.Empty,
                squad = SectionState.Empty,
                fixtures = SectionState.Empty,
                detailedStats = SectionState.Empty,
                news = SectionState.Empty,
            )
        }
    }
}
