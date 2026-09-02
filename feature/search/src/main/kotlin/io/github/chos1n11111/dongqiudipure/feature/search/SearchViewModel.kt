package io.github.chos1n11111.dongqiudipure.feature.search

import androidx.lifecycle.ViewModel
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(@StringRes val labelRes: Int) {
    All(R.string.search_filter_all),
    Teams(R.string.search_group_teams),
    Players(R.string.search_group_players),
    Competitions(R.string.search_group_competitions),
    Articles(R.string.search_group_articles),
}

/**
 * 搜索结果。
 *
 * 按实体类型**分组**并各自标注计数，而不是混排一个长列表 ——
 * 用户搜「曼城」时想要的是球队，不是第 40 条资讯。
 */
data class SearchResults(
    val teams: List<TeamRef>,
    val players: List<SampleSearch.PlayerHit>,
    val competitions: List<SampleSearch.CompetitionHit>,
    val articles: List<ArticleSummary>,
    val articleTotal: Int,
) {
    val isEmpty: Boolean
        get() = teams.isEmpty() && players.isEmpty() &&
            competitions.isEmpty() && articles.isEmpty()
}

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.All,
    val recentQueries: List<String> = emptyList(),
    /** null 表示尚未发起搜索，应展示搜索历史而不是空结果。 */
    val results: SectionState<SearchResults>? = null,
)

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入时替换为 `searchRepository.search(query, filter, cursor)`。
 *
 * 只搜索第一阶段已支持的实体类型；未覆盖的类型不出现空分组
 * （FEATURES.md M8：分类搜索结果）。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.7
 */
@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(recentQueries = SampleSearch.recentQueries),
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }

        // 输入为空时回到搜索历史，而不是展示「无结果」。
        if (query.isBlank()) {
            searchJob?.cancel()
            _uiState.update { it.copy(results = null) }
            return
        }
        search(query)
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update { it.copy(query = "", results = null) }
    }

    fun selectFilter(filter: SearchFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun selectRecent(query: String) {
        _uiState.update { it.copy(query = query) }
        search(query)
    }

    fun retry() {
        search(_uiState.value.query)
    }

    private fun search(query: String) {
        // 上一次搜索必须可取消，否则慢响应会覆盖新查询的结果。
        searchJob?.cancel()
        _uiState.update { it.copy(results = SectionState.Loading) }

        searchJob = viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用，并加入输入防抖。
            delay(500)
            val results = SearchResults(
                teams = SampleSearch.teams,
                players = SampleSearch.players,
                competitions = SampleSearch.competitions,
                articles = SampleSearch.articles,
                articleTotal = SampleSearch.ARTICLE_TOTAL,
            )
            _uiState.update {
                it.copy(
                    results = if (results.isEmpty) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(results)
                    },
                )
            }
        }
    }
}
