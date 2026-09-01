package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * 图片位。
 *
 * 图片加载需要网络，属于 data 层能力。当前统一渲染为占位块 ——
 * 但**尺寸与真实图片完全一致**，这样接入图片加载后不会产生位移。
 *
 * [badgeLabel] 用于图集张数、视频时长等角标。
 *
 * TODO(data): 接入图片加载库后，url 非空时加载远端图片，
 * 本组件降级为 placeholder / error 态。见 docs/engineering/BACKEND-CONTRACT-TODO.md
 */
@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    url: String? = null,
    cornerRadius: androidx.compose.ui.unit.Dp = 6.dp,
    badgeLabel: String? = null,
    @DrawableRes badgeIcon: Int? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        scheme.surfaceContainerHighest,
                        scheme.surfaceContainerHigh,
                    ),
                ),
            ),
    ) {
        if (badgeLabel != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (badgeIcon != null) {
                    Icon(
                        painter = painterResource(badgeIcon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(11.dp)
                            .padding(end = 0.dp),
                    )
                }
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(start = if (badgeIcon != null) 3.dp else 0.dp),
                )
            }
        }
    }
}
