package io.github.chos1n11111.dongqiudipure.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.NewsRepository
import io.github.chos1n11111.dongqiudipure.core.model.ArticleSummary
import io.github.chos1n11111.dongqiudipure.core.model.NewsCategory
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val categories: List<NewsCategory>,
    val selectedCategory: NewsCategory,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: NewsRepository,
) : ViewModel() {

    private val categories = repository.categories
    private val defaultCategory = categories.firstOrNull { it.id == HEADLINE_CATEGORY_ID }
        ?: categories.first()
    private val feedSelection = MutableStateFlow(FeedSelection(defaultCategory))
    private val _uiState = MutableStateFlow(
        HomeUiState(
            categories = categories,
            selectedCategory = feedSelection.value.category,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val feed: Flow<PagingData<ArticleSummary>> = feedSelection
        .flatMapLatest { selection ->
            repository.pagedFeed(
                category = selection.category,
                fresh = selection.refreshGeneration > 0,
            )
        }
        .cachedIn(viewModelScope)

    fun selectCategory(category: NewsCategory) {
        if (category == feedSelection.value.category || category !in _uiState.value.categories) return
        feedSelection.value = FeedSelection(category)
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setEnabledCategoryIds(enabledIds: Set<String>) {
        val enabledCategories = categories.filter { it.id in enabledIds }
            .ifEmpty { listOf(defaultCategory) }
        val current = feedSelection.value.category
        val selected = enabledCategories.firstOrNull { it.id == current.id }
            ?: enabledCategories.first()
        if (_uiState.value.categories == enabledCategories && current == selected) return

        _uiState.value = HomeUiState(enabledCategories, selected)
        if (selected != current) feedSelection.value = FeedSelection(selected)
    }

    fun refresh() {
        feedSelection.update { selection ->
            selection.copy(refreshGeneration = selection.refreshGeneration + 1)
        }
    }
}

private data class FeedSelection(
    val category: NewsCategory,
    val refreshGeneration: Long = 0,
)

private const val HEADLINE_CATEGORY_ID = "1"
