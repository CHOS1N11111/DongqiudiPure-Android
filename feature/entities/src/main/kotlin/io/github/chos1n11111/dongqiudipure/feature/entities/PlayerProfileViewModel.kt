package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import io.github.chos1n11111.dongqiudipure.core.sampledata.SamplePlayers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerProfileUiState(
    val profile: SectionState<PlayerProfile> = SectionState.Loading,
    val seasonStats: SectionState<List<PlayerSeasonStat>> = SectionState.Loading,
    val career: SectionState<List<CareerEntry>> = SectionState.Loading,
)

/**
 * ⚠️ 当前从 :core:sampledata 读取假数据。
 *
 * 与球队页同样的约束：Repository 不得按热门名单分支，
 * 本页必须能接收任意 [PlayerId]，未覆盖的 section 分别降级。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.6
 */
class PlayerProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    private var playerId: PlayerId? = null

    fun load(id: PlayerId) {
        if (playerId == id) return
        playerId = id
        loadProfile()
        loadSeasonStats()
        loadCareer()
    }

    fun retryAll() {
        _uiState.update {
            it.copy(
                profile = SectionState.Loading,
                seasonStats = SectionState.Loading,
                career = SectionState.Loading,
            )
        }
        loadProfile()
        loadSeasonStats()
        loadCareer()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            // TODO(data): 替换为 Repository 调用。
            delay(350)
            _uiState.update { it.copy(profile = SectionState.Content(SamplePlayers.profile)) }
        }
    }

    private fun loadSeasonStats() {
        viewModelScope.launch {
            delay(600)
            _uiState.update {
                it.copy(seasonStats = SectionState.Content(SamplePlayers.profileStats))
            }
        }
    }

    private fun loadCareer() {
        viewModelScope.launch {
            delay(750)
            val career = SamplePlayers.career
            _uiState.update {
                it.copy(
                    career = if (career.isEmpty()) {
                        SectionState.Empty
                    } else {
                        SectionState.Content(career)
                    },
                )
            }
        }
    }
}
