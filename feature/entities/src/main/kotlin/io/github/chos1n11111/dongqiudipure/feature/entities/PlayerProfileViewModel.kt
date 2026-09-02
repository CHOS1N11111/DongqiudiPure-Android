package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.data.FootballEntityRepository
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHonor
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerInjury
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerTransfer
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerProfileUiState(
    val profile: SectionState<PlayerProfile> = SectionState.Loading,
    val ability: SectionState<PlayerAbility> = SectionState.Loading,
    val career: SectionState<List<CareerEntry>> = SectionState.Loading,
    val honors: SectionState<List<PlayerHonor>> = SectionState.Loading,
    val transfers: SectionState<List<PlayerTransfer>> = SectionState.Loading,
    val injuries: SectionState<List<PlayerInjury>> = SectionState.Loading,
)

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val repository: FootballEntityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    private var playerId: PlayerId? = null
    private val jobs = mutableListOf<Job>()

    fun load(id: PlayerId) {
        if (playerId == id) return
        playerId = id
        loadAll(id)
    }

    fun retryAll() {
        playerId?.let(::loadAll)
    }

    private fun loadAll(id: PlayerId) {
        jobs.forEach { it.cancel() }
        jobs.clear()
        _uiState.value = PlayerProfileUiState()
        jobs += viewModelScope.launch {
            when (val result = repository.loadPlayerOverview(id)) {
                is DataResult.Failure -> updateIfCurrent(id) {
                    it.copy(
                        profile = SectionState.Failed(result.error),
                        honors = SectionState.Failed(result.error),
                        transfers = SectionState.Failed(result.error),
                        injuries = SectionState.Failed(result.error),
                    )
                }
                is DataResult.Success -> {
                    val overview = result.value
                    updateIfCurrent(id) {
                        it.copy(
                            profile = overview?.profile?.let { value -> SectionState.Content(value) }
                                ?: SectionState.Empty,
                            honors = overview?.honors.orEmpty().toSectionState(),
                            transfers = overview?.transfers.orEmpty().toSectionState(),
                            injuries = overview?.injuries.orEmpty().toSectionState(),
                        )
                    }
                }
            }
        }
        jobs += viewModelScope.launch {
            val state = when (val result = repository.loadPlayerAbility(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value?.let { SectionState.Content(it) }
                    ?: SectionState.Empty
            }
            updateIfCurrent(id) { it.copy(ability = state) }
        }
        jobs += viewModelScope.launch {
            val state = when (val result = repository.loadPlayerCareer(id)) {
                is DataResult.Failure -> SectionState.Failed(result.error)
                is DataResult.Success -> result.value.toSectionState()
            }
            updateIfCurrent(id) { it.copy(career = state) }
        }
    }

    private fun updateIfCurrent(
        id: PlayerId,
        transform: (PlayerProfileUiState) -> PlayerProfileUiState,
    ) {
        _uiState.update { if (playerId == id) transform(it) else it }
    }

    private fun <T> List<T>.toSectionState(): SectionState<List<T>> =
        if (isEmpty()) SectionState.Empty else SectionState.Content(this)
}
