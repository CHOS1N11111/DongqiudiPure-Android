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
    private val selectedCategory = MutableStateFlow(categories.first())
    private val _uiState = MutableStateFlow(
        HomeUiState(
            categories = categories,
            selectedCategory = selectedCategory.value,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val feed: Flow<PagingData<ArticleSummary>> = selectedCategory
        .flatMapLatest(repository::pagedFeed)
        .cachedIn(viewModelScope)

    fun selectCategory(category: NewsCategory) {
        if (category == selectedCategory.value) return
        selectedCategory.value = category
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
}
