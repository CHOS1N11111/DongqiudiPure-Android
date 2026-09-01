package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import kotlin.math.abs

/**
 * 队徽。
 *
 * 当前始终渲染占位色块：队徽图片需要网络加载，属于 data 层能力。
 * 接入图片加载库后，[crestUrl] 非空时改为加载远端图片，占位逻辑作为
 * loading / error 的回退保留。
 *
 * 占位色由 [teamId] 稳定派生 —— 同一支球队在任何页面都是同一个颜色，
 * 比统一的灰块更容易在列表中扫读。
 *
 * 队徽不承载文字信息，语义上由相邻的球队名承担，因此对无障碍树隐藏。
 */
@Composable
fun TeamCrest(
    teamId: TeamId,
    teamName: String,
    modifier: Modifier = Modifier,
    crestUrl: String? = null,
    size: Dp = DqdSize.crestSmall,
) {
    // TODO(data): crestUrl 非空时加载远端图片。见 docs/engineering/BACKEND-CONTRACT-TODO.md
    val color = rememberCrestColor(teamId)
    val corner = if (size >= DqdSize.crestLarge) 10.dp else 4.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(color)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (size >= DqdSize.crestMedium) {
            Text(
                text = teamName.take(1),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (size.value * 0.4f).sp,
                ),
            )
        }
    }
}

/**
 * 由球队 ID 派生一个稳定、饱和度受控的颜色。
 *
 * 明度固定在中段，保证白色首字母在其上可读，且深浅主题下都不刺眼。
 */
@Composable
private fun rememberCrestColor(teamId: TeamId): Color {
    val hash = abs(teamId.raw.hashCode())
    val hue = (hash % 360).toFloat()
    return Color.hsl(hue = hue, saturation = 0.42f, lightness = 0.44f)
}
