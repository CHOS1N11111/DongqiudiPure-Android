package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * 图片位。
 *
 * 加载、失败与无 URL 都保持相同尺寸，列表不会因网络结果发生位移。
 *
 * [badgeLabel] 用于图集张数、视频时长等角标。
 *
 */
@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    url: String? = null,
    cornerRadius: androidx.compose.ui.unit.Dp = 6.dp,
    badgeLabel: String? = null,
    @DrawableRes badgeIcon: Int? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(scheme.surfaceContainerHigh),
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }

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

/** A small server-provided icon without the framed image placeholder treatment. */
@Composable
fun RemoteIcon(
    url: String?,
    modifier: Modifier = Modifier,
) {
    if (url == null) return
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Displays article media without cropping it; portrait statistics images remain complete. */
@Composable
fun OriginalAspectImage(
    url: String?,
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio?.takeIf { it.isFinite() && it > 0f } ?: 16f / 9f)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
