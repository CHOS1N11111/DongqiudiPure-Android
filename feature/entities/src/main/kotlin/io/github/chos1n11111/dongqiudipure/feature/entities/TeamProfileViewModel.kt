package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleMatches
import io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers
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
    val squad: SectionState<List<SquadMember>> = SectionState.Loading,
    val fixtures: SectionState<List<MatchSummary>> = SectionState.Loading,
    val detailedStats: SectionState<List<PlayerSeasonStat>> = SectionState.Loading,
    val news: SectionState<List<ArticleSummary>> = SectionState.Loading,
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
        loadSquad()
        loadFixtures()
        loadDetailedStats()
        loadNews()
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
                squad = SectionState.Loading,
                fixtures = SectionState.Loading,
                detailedStats = SectionState.Loading,
                news = SectionState.Loading,
            )
        }
        load(teamId ?: return)
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
                it.status is MatchStatus.NotStarted
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

    private fun loadSquad() {
        viewModelScope.launch {
            delay(700)
            _uiState.update { it.copy(squad = SectionState.Content(SamplePlayers.squad)) }
        }
    }

    private fun loadFixtures() {
        viewModelScope.launch {
            delay(600)
            _uiState.update {
                it.copy(fixtures = SectionState.Content(SampleMatches.matches))
            }
        }
    }

    private fun loadDetailedStats() {
        viewModelScope.launch {
            delay(750)
            _uiState.update {
                it.copy(
                    detailedStats = SectionState.Content(
                        listOf(
                            PlayerSeasonStat("played", "场次", "4", 1),
                            PlayerSeasonStat("won", "胜", "3", 2),
                            PlayerSeasonStat("drawn", "平", "1", 3),
                            PlayerSeasonStat("lost", "负", "0", 4),
                            PlayerSeasonStat("gf", "进球", "11", 5),
                            PlayerSeasonStat("ga", "失球", "4", 6),
                            PlayerSeasonStat("clean", "零封", "2", 7),
                            PlayerSeasonStat("possession", "场均控球", "61%", 8),
                            // 该赛事未提供这两项。
                            PlayerSeasonStat("xg", "预期进球", null, 9),
                            PlayerSeasonStat("xga", "预期失球", null, 10),
                        ),
                    ),
                )
            }
        }
    }

    private fun loadNews() {
        viewModelScope.launch {
            delay(800)
            val news = SampleFeed.articles.take(4)
            _uiState.update {
                it.copy(
                    news = if (news.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(news)
                    },
                )
            }
        }
    }
}
