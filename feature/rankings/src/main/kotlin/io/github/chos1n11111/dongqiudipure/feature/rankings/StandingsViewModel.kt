package io.github.chos1n11111.dongqiudipure.feature.rankings

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleCompetitions
import io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleStandings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 赛事页的分栏。 */
enum class RankingTab(@StringRes val labelRes: Int) {
    Standings(R.string.rankings_tab_standings),
    Scorers(R.string.rankings_tab_scorers),
    Assists(R.string.rankings_tab_assists),
    Fixtures(R.string.rankings_tab_fixtures),
}

data class RankingsUiState(
    /**
     * 「数据」根 tab 的赛事切换器数据源。
     *
     * ⚠️ 真实实现由服务端提供（M2 归档的官方匿名默认入口），
     * **不得在客户端写死名单**（FEATURES.md：M6/M7 不在代码中写死永久名单）。
     */
    val competitions: List<CompetitionRef> = emptyList(),
    val selectedCompetition: CompetitionRef? = null,
    val competitionName: String = "",
    val seasonLabel: String = "",
    val selectedTab: RankingTab = RankingTab.Standings,
    val table: SectionState<StandingTable> = SectionState.Loading,
    val scorers: SectionState<PlayerRankingTable> = SectionState.Loading,
    val assists: SectionState<PlayerRankingTable> = SectionState.Loading,
    val fixtures: SectionState<List<MatchSummary>> = SectionState.Loading,
)

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时替换为 `standingsRepository.loadStandings(...)`、
 * `loadScorers(...)`、`loadAssists(...)`、`loadFixtures(...)`。
 *
 * 四个分栏各自一个 [SectionState]：某个榜单接口失效不影响其他分栏。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.5
 */
class StandingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    private var competitionId: CompetitionId? = null

    /** 「数据」根 tab 用：加载赛事列表并选中默认项。 */
    fun loadHub() {
        if (_uiState.value.competitions.isNotEmpty()) return
        // TODO(data): 替换为 Repository 调用；默认项取服务端返回的默认赛事。
        val competitions = SampleCompetitions.all
        val default = SampleCompetitions.default
        _uiState.update {
            it.copy(competitions = competitions, selectedCompetition = default)
        }
        load(default.id)
    }

    fun selectCompetition(competition: CompetitionRef) {
        if (competition.id == competitionId) return
        _uiState.update { it.copy(selectedCompetition = competition) }
        load(competition.id)
    }

    fun load(id: CompetitionId) {
        if (competitionId == id) return
        competitionId = id
        _uiState.update {
            it.copy(
                table = SectionState.Loading,
                scorers = SectionState.Loading,
                assists = SectionState.Loading,
                fixtures = SectionState.Loading,
            )
        }
        loadTable()
        loadScorers()
        loadAssists()
        loadFixtures()
    }

    fun selectTab(tab: RankingTab) {
        if (tab == _uiState.value.selectedTab) return
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retry() {
        when (_uiState.value.selectedTab) {
            RankingTab.Standings -> {
                _uiState.update { it.copy(table = SectionState.Loading) }
                loadTable()
            }

            RankingTab.Scorers -> {
                _uiState.update { it.copy(scorers = SectionState.Loading) }
                loadScorers()
            }

            RankingTab.Assists -> {
                _uiState.update { it.copy(assists = SectionState.Loading) }
                loadAssists()
            }

            RankingTab.Fixtures -> {
                _uiState.update { it.copy(fixtures = SectionState.Loading) }
                loadFixtures()
            }
        }
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

    private fun loadScorers() {
        viewModelScope.launch {
            delay(650)
            _uiState.update { it.copy(scorers = SectionState.Content(SamplePlayers.scorers)) }
        }
    }

    private fun loadAssists() {
        viewModelScope.launch {
            delay(700)
            _uiState.update { it.copy(assists = SectionState.Content(SamplePlayers.assists)) }
        }
    }

    private fun loadFixtures() {
        viewModelScope.launch {
            delay(600)
            val fixtures = SampleMatches.matches
            _uiState.update {
                it.copy(
                    fixtures = if (fixtures.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(fixtures)
                    },
                )
            }
        }
    }
}
