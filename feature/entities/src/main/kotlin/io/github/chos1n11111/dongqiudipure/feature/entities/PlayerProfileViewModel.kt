package io.github.chos1n11111.dongqiudipure.feature.entities

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.SectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PlayerProfileUiState(
    val profile: SectionState<PlayerProfile> = SectionState.Loading,
    val seasonStats: SectionState<List<PlayerSeasonStat>> = SectionState.Loading,
    val career: SectionState<List<CareerEntry>> = SectionState.Loading,
)

/**
 * 球员资料 contract 尚未接入，当前各 section 明确显示为空，不填充样例数据。
 *
 * 与球队页同样的约束：Repository 不得按热门名单分支，
 * 本页必须能接收任意 [PlayerId]，未覆盖的 section 分别降级。
 *
 * 详见 docs/engineering/BACKEND-CONTRACT-TODO.md §2.6
 */
@HiltViewModel
class PlayerProfileViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    private var playerId: PlayerId? = null

    fun load(id: PlayerId) {
        if (playerId == id) return
        playerId = id
        clearUnavailableData()
    }

    fun retryAll() {
        clearUnavailableData()
    }

    private fun clearUnavailableData() {
        _uiState.update {
            it.copy(
                profile = SectionState.Empty,
                seasonStats = SectionState.Empty,
                career = SectionState.Empty,
            )
        }
    }
}
