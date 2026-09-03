package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.model.diagnostic
import io.github.chos1n11111.dongqiudipure.core.model.isRetryable

/**
 * 系统是否要求减少动画。
 *
 * Android 没有直接对应 `prefers-reduced-motion` 的 Compose API，
 * 读「开发者选项 / 无障碍」里的动画时长缩放：为 0 表示用户已关闭动画。
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/**
 * 骨架块。
 *
 * 按真实版式的尺寸摆放，内容到达时零位移 —— 这是 CLS 的移动端等价物。
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp),
) {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val reducedMotion = rememberReducedMotion()

    val alpha = if (reducedMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "skeleton")
        transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "skeletonAlpha",
        ).value
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(base.copy(alpha = alpha)),
    )
}

/**
 * 空状态。
 *
 * 「暂无数据」四个字是设计缺席的标志：空态必须说明**为什么**空，
 * 并给出一条可走的路。这里不提供「重试」—— 重试不会改变结果，
 * 那是 [DqdErrorState] 的职责。
 */
@Composable
fun DqdEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = DqdIcons.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StateScaffold(
        modifier = modifier,
        icon = icon,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        iconBackground = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = title,
        description = description,
        diagnostic = null,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

/**
 * 错误状态。
 *
 * 把 [AppError] 映射为「用户能采取的动作」，不展示 exception message
 * （ARCHITECTURE.md §8）。默认只有重试可能改变结果时才显示重试按钮；调用方确认
 * 服务端响应可能恢复时，可以通过 [forceRetry] 为 contract 错误提供直接重载入口。
 */
@Composable
fun DqdErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    forceRetry: Boolean = false,
) {
    val sports = DqdTheme.sports
    val copy = errorCopy(error)

    StateScaffold(
        modifier = modifier,
        icon = copy.icon,
        iconTint = if (copy.isWarning) sports.yellowCard else MaterialTheme.colorScheme.error,
        iconBackground = (if (copy.isWarning) sports.yellowCard else MaterialTheme.colorScheme.error)
            .copy(alpha = 0.14f),
        title = stringResource(copy.titleRes),
        description = stringResource(copy.descriptionRes),
        diagnostic = error.diagnostic,
        actionLabel = if (error.isRetryable || forceRetry) stringResource(R.string.ds_action_retry) else null,
        onAction = onRetry.takeIf { error.isRetryable || forceRetry },
    )
}

/**
 * 错误文案。持有资源 id 而不是已解析的字符串 —— [errorCopy] 是普通函数，
 * 不在 composable 上下文里，无法（也不该）自己解析字符串。
 */
private data class ErrorCopy(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val icon: Int,
    val isWarning: Boolean,
)

private fun errorCopy(error: AppError): ErrorCopy = when (error) {
    is AppError.Network -> when (error.kind) {
        NetworkKind.NoConnection -> ErrorCopy(
            titleRes = R.string.ds_error_no_connection_title,
            descriptionRes = R.string.ds_error_no_connection_description,
            icon = DqdIcons.WifiOff,
            isWarning = false,
        )

        NetworkKind.Timeout -> ErrorCopy(
            titleRes = R.string.ds_error_timeout_title,
            descriptionRes = R.string.ds_error_timeout_description,
            icon = DqdIcons.WifiOff,
            isWarning = false,
        )

        NetworkKind.TlsFailure -> ErrorCopy(
            titleRes = R.string.ds_error_tls_title,
            descriptionRes = R.string.ds_error_tls_description,
            icon = DqdIcons.WifiOff,
            isWarning = false,
        )

        NetworkKind.Unknown -> ErrorCopy(
            titleRes = R.string.ds_error_network_title,
            descriptionRes = R.string.ds_error_network_description,
            icon = DqdIcons.WifiOff,
            isWarning = false,
        )
    }

    is AppError.RateLimited -> ErrorCopy(
        titleRes = R.string.ds_error_rate_limited_title,
        descriptionRes = R.string.ds_error_rate_limited_description,
        icon = DqdIcons.Alert,
        isWarning = true,
    )

    // 非官方客户端特有：服务端改了结构。用户需要知道这不是自己的网络问题。
    is AppError.UnsupportedContract, is AppError.Parse -> ErrorCopy(
        titleRes = R.string.ds_error_contract_title,
        descriptionRes = R.string.ds_error_contract_description,
        icon = DqdIcons.Alert,
        isWarning = true,
    )

    is AppError.Http, is AppError.Server -> ErrorCopy(
        titleRes = R.string.ds_error_server_title,
        descriptionRes = R.string.ds_error_server_description,
        icon = DqdIcons.Alert,
        isWarning = false,
    )

    AppError.AuthenticationRequired -> ErrorCopy(
        titleRes = R.string.ds_error_auth_required_title,
        descriptionRes = R.string.ds_error_auth_required_description,
        icon = DqdIcons.Login,
        isWarning = true,
    )

    AppError.SessionExpired -> ErrorCopy(
        titleRes = R.string.ds_error_session_expired_title,
        descriptionRes = R.string.ds_error_session_expired_description,
        icon = DqdIcons.Login,
        isWarning = true,
    )
}

@Composable
private fun StateScaffold(
    modifier: Modifier,
    @DrawableRes icon: Int,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    description: String,
    diagnostic: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DqdSpacing.xl, vertical = DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // 脱敏诊断串：只含 endpoint 标识，不含 host、query、Header 或凭据。
        if (diagnostic != null) {
            Text(
                text = diagnostic,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = DqdSpacing.xs)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(horizontal = DqdSpacing.sm, vertical = DqdSpacing.xs),
            )
        }

        if (actionLabel != null && onAction != null) {
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.padding(top = DqdSpacing.xs),
            ) {
                Icon(
                    painter = painterResource(DqdIcons.Refresh),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(start = DqdSpacing.sm),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(name = "错误态", showBackground = true, backgroundColor = 0xFF0E1417)
@Composable
private fun ErrorStatePreview() {
    DqdTheme(darkTheme = true) {
        Column {
            DqdErrorState(
                error = AppError.Network(NetworkKind.NoConnection),
                onRetry = {},
            )
            DqdErrorState(
                error = AppError.UnsupportedContract(
                    io.github.chos1n11111.dongqiudipure.core.model.EndpointId("match/stats"),
                ),
                onRetry = {},
            )
        }
    }
}
