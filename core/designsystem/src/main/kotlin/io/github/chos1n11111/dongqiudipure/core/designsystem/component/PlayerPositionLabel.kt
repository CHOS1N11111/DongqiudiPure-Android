package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.annotation.StringRes
import io.github.chos1n11111.dongqiudipure.core.designsystem.R
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition

/**
 * 球员位置的展示文案。
 *
 * 放在 designsystem 而不是各 feature：阵容（比赛详情）与名单（球队资料）
 * 两处都要用，而 feature 之间不得互相依赖（ARCHITECTURE.md §4）。
 */
@StringRes
fun PlayerPosition.labelRes(): Int = when (this) {
    PlayerPosition.Goalkeeper -> R.string.ds_position_goalkeeper
    PlayerPosition.Defender -> R.string.ds_position_defender
    PlayerPosition.Midfielder -> R.string.ds_position_midfielder
    PlayerPosition.Forward -> R.string.ds_position_forward
    PlayerPosition.Unknown -> R.string.ds_position_unknown
}
