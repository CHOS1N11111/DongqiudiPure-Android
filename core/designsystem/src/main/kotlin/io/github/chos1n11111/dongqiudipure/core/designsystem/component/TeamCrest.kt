package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSize
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import coil3.compose.AsyncImage
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * 队徽。
 *
 * [crestUrl] 可用时加载真实队徽；短暂失败会有限重试，占位逻辑作为
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
    val color = rememberCrestColor(teamId)
    val corner = if (size >= DqdSize.crestLarge) 10.dp else 4.dp
    var imageLoaded by remember(crestUrl) { mutableStateOf(false) }
    var retryAttempt by remember(crestUrl) { mutableIntStateOf(0) }
    var retryPending by remember(crestUrl) { mutableStateOf(false) }

    LaunchedEffect(retryPending) {
        if (retryPending) {
            delay(750)
            retryAttempt += 1
            retryPending = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(
                if (crestUrl == null) color else MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (!imageLoaded) {
            Text(
                text = teamName.take(1),
                color = if (crestUrl == null) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = maxOf(8f, size.value * 0.4f).sp,
                ),
            )
        }
        if (crestUrl != null) {
            key(retryAttempt) {
                AsyncImage(
                    model = crestUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    onLoading = { imageLoaded = false },
                    onSuccess = { imageLoaded = true },
                    onError = {
                        imageLoaded = false
                        if (retryAttempt < MAX_RETRY_ATTEMPTS) retryPending = true
                    },
                    modifier = Modifier
                        .size(size)
                        .padding(1.dp),
                )
            }
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

private const val MAX_RETRY_ATTEMPTS = 2
