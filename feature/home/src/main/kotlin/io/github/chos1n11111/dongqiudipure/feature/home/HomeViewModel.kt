package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.sampledata.SampleFeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val feed: SectionState<List<ArticleSummary>> = SectionState.Loading,
    val isRefreshing: Boolean = false,
)

/**
 * 资讯流状态编排。
 *
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 * 接入真实数据时，把 [loadFeed] 换成 `articleRepository.observeHomeFeed(category)`，
 * 其余状态编排、分类切换与错误处理逻辑不需要改动 ——
 * 这正是把 UI 状态建模成 [SectionState] 的目的。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.1
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            categories = SampleFeed.categories,
            selectedCategory = SampleFeed.categories.first(),
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun selectCategory(category: String) {
        if (category == _uiState.value.selectedCategory) return
        _uiState.update { it.copy(selectedCategory = category, feed = SectionState.Loading) }
        loadFeed()
    }

    fun retry() {
        _uiState.update { it.copy(feed = SectionState.Loading) }
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。此处的延迟只是为了让
            //  Loading 骨架在开发期真实可见，不是产品行为。
            delay(SAMPLE_LOAD_DELAY_MS)
            _uiState.update { it.copy(feed = SectionState.Content(SampleFeed.articles)) }
        }
    }

    private companion object {
        const val SAMPLE_LOAD_DELAY_MS = 600L
    }
}
