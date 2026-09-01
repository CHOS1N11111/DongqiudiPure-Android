package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleStandings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 榜单页的分栏。射手榜与助攻榜尚未实现，先标注所属 milestone。 */
enum class RankingTab(val label: String) {
    Standings("积分榜"),
    Scorers("射手榜"),
    Assists("助攻榜"),
    Fixtures("赛程"),
}

data class RankingsUiState(
    val competitionName: String = "",
    val seasonLabel: String = "",
    val selectedTab: RankingTab = RankingTab.Standings,
    val table: SectionState<StandingTable> = SectionState.Loading,
)

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时替换为 `standingsRepository.loadStandings(competitionId, seasonId, stageId)`。
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.5
 */
class StandingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    private var competitionId: CompetitionId? = null

    fun load(id: CompetitionId) {
        if (competitionId == id) return
        competitionId = id
        loadTable()
    }

    fun selectTab(tab: RankingTab) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == RankingTab.Standings) loadTable()
    }

    fun retry() {
        _uiState.update { it.copy(table = SectionState.Loading) }
        loadTable()
    }

    private fun loadTable() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。
            delay(500)
            val table = SampleStandings.premierLeague
            _uiState.update {
                it.copy(
                    competitionName = table.competition.name,
                    seasonLabel = table.seasonLabel,
                    table = SectionState.Content(table),
                )
            }
        }
    }
}
