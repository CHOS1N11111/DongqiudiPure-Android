package io.github.chos1n11111.dongqiudipure.core.designsystem.component

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.R
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.core.model.FormResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus

/**
 * 比赛状态徽标。
 *
 * **状态到版式的唯一映射点。** 页面不得自行判断状态再拼版式，
 * 否则 [MatchStatus.Unknown] 分支迟早会在某个页面被遗漏。
 *
 * 每种状态都是「文字 + 形态」双编码，颜色只是第二重提示
 * （PRODUCT.md §8：颜色不是唯一状态提示）。
 */
@Composable
fun MatchStatusBadge(
    status: MatchStatus,
    modifier: Modifier = Modifier,
) {
    val sports = DqdTheme.sports
    val a11yLabel = matchStatusAccessibilityLabel(status)

    Column(
        modifier = modifier.semantics { contentDescription = a11yLabel },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        when (status) {
            is MatchStatus.Live -> {
                Text(
                    text = status.minuteLabel ?: stringResource(R.string.ds_match_live),
                    style = DqdTheme.dataText.minuteLabel,
                    color = sports.live,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    LiveDot(color = sports.live)
                    Text(
                        text = stringResource(R.string.ds_match_live_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = sports.live,
                    )
                }
            }

            MatchStatus.HalfTime -> StatusStack(
                primary = stringResource(R.string.ds_match_halftime),
                secondary = stringResource(R.string.ds_match_halftime_secondary),
                primaryColor = sports.live,
                secondaryColor = sports.live,
            )

            is MatchStatus.NotStarted -> StatusStack(
                primary = status.kickoffLabel,
                secondary = stringResource(R.string.ds_match_not_started),
                primaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                primaryStyleIsData = true,
            )

            MatchStatus.Finished -> StatusStack(
                primary = stringResource(R.string.ds_match_finished),
                secondary = null,
                primaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MatchStatus.Postponed -> StatusStack(
                primary = stringResource(R.string.ds_match_postponed),
                secondary = stringResource(R.string.ds_match_postponed_secondary),
                primaryColor = sports.yellowCard,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MatchStatus.Cancelled -> StatusStack(
                primary = stringResource(R.string.ds_match_cancelled),
                secondary = null,
                primaryColor = sports.loss,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 服务端新增了当前版本不认识的状态：原样显示，不猜测语义。
            is MatchStatus.Unknown -> StatusStack(
                primary = status.rawValue,
                secondary = stringResource(R.string.ds_match_unknown),
                primaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 状态的无障碍描述。
 *
 * 徽标本身是「分钟 + LIVE」这种紧凑排版，读屏逐字念出来不成句，
 * 所以整块用一句完整描述覆盖。
 */
@Composable
private fun matchStatusAccessibilityLabel(status: MatchStatus): String = when (status) {
    is MatchStatus.Live -> {
        val minute = status.minuteLabel
        if (minute != null) {
            stringResource(R.string.ds_match_a11y_live, minute)
        } else {
            stringResource(R.string.ds_match_a11y_live_unknown_minute)
        }
    }

    MatchStatus.HalfTime -> stringResource(R.string.ds_match_a11y_halftime)
    is MatchStatus.NotStarted ->
        stringResource(R.string.ds_match_a11y_not_started, status.kickoffLabel)

    MatchStatus.Finished -> stringResource(R.string.ds_match_a11y_finished)
    MatchStatus.Postponed -> stringResource(R.string.ds_match_a11y_postponed)
    MatchStatus.Cancelled -> stringResource(R.string.ds_match_a11y_cancelled)
    is MatchStatus.Unknown ->
        stringResource(R.string.ds_match_a11y_unknown, status.rawValue)
}

@Composable
private fun StatusStack(
    primary: String,
    secondary: String?,
    primaryColor: Color,
    secondaryColor: Color,
    primaryStyleIsData: Boolean = false,
) {
    Text(
        text = primary,
        style = if (primaryStyleIsData) {
            DqdTheme.dataText.minuteLabel
        } else {
            MaterialTheme.typography.labelLarge
        },
        color = primaryColor,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
    if (secondary != null) {
        Text(
            text = secondary,
            style = MaterialTheme.typography.labelSmall,
            color = secondaryColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * LIVE 脉冲圆点。
 *
 * 这是全应用唯一的持续动画 —— 它编码状态而非装饰。
 * 用户关闭系统动画时停止脉冲，圆点仍在，状态信息不丢失。
 */
@Composable
fun LiveDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = rememberReducedMotion()
    val alpha = if (reducedMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "live")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "liveAlpha",
        ).value
    }

    Box(
        modifier = modifier
            .size(6.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
            .clearAndSetSemantics { },
    )
}

/**
 * 近期战绩标记。
 *
 * 用「胜 / 平 / 负」汉字而不是纯色圆点：色觉差异用户无法只靠红绿分辨结果。
 */
@Composable
fun FormBadge(
    result: FormResult,
    modifier: Modifier = Modifier,
) {
    val sports = DqdTheme.sports
    val color = when (result) {
        FormResult.Win -> sports.win
        FormResult.Draw -> sports.draw
        FormResult.Loss -> sports.loss
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(result.labelRes()),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@StringRes
private fun FormResult.labelRes(): Int = when (this) {
    FormResult.Win -> R.string.ds_form_win
    FormResult.Draw -> R.string.ds_form_draw
    FormResult.Loss -> R.string.ds_form_loss
}

@Preview(name = "比赛状态", showBackground = true, backgroundColor = 0xFF0E1417)
@Composable
private fun MatchStatusBadgePreview() {
    DqdTheme(darkTheme = true) {
        Row(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MatchStatusBadge(MatchStatus.Live("67'"))
            MatchStatusBadge(MatchStatus.NotStarted("22:30"))
            MatchStatusBadge(MatchStatus.Finished)
            MatchStatusBadge(MatchStatus.Postponed)
            MatchStatusBadge(MatchStatus.Unknown("AWARDED"))
        }
    }
}
