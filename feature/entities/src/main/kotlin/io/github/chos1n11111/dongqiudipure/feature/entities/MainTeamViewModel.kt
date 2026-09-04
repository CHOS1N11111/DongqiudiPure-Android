package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.EntitySearchResults
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainTeamUiState(
    val query: String = "",
    val searchResults: SectionState<EntitySearchResults>? = null,
)

@HiltViewModel
class MainTeamViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainTeamUiState())
    val uiState: StateFlow<MainTeamUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun setQuery(value: String) {
        val query = value.filterNot { it.isISOControl() }.take(MAX_QUERY_LENGTH)
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = null) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            search(query)
        }
    }

    fun retrySearch() {
        val query = _uiState.value.query
        if (query.isNotBlank()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch { search(query) }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = MainTeamUiState()
    }

    private suspend fun search(query: String) {
        _uiState.update { state ->
            if (state.query == query) state.copy(searchResults = SectionState.Loading) else state
        }
        val result = repository.searchEntities(query)
        _uiState.update { state ->
            if (state.query != query) return@update state
            val section = when (result) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> if (
                    result.value.teams.isEmpty() && result.value.players.isEmpty()
                ) {
                    SectionState.Empty
                } else {
                    SectionState.Content(result.value)
                }
            }
            state.copy(searchResults = section)
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val MAX_QUERY_LENGTH = 100
    }
}
