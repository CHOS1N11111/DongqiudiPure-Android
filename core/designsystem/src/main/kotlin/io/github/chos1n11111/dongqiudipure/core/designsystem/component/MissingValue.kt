package io.github.chos1n11111.dongqiudipure.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme

/**
 * 渲染「暂无数据」记号的唯一组件。
 *
 * 这是 PRODUCT.md §2.4「完整但不伪造」在 UI 层的落点：
 * 服务端缺失的数据必须如实降级，不能以零值或占位内容冒充完整数据。
 *
 * 缺失与零值在三个维度上都不同：
 *  1. 字形 —— 破折号 vs 数字
 *  2. 颜色 —— [SportsColors.missing] vs 正文色
 *  3. 装饰 —— 虚线下划线 vs 无
 *
 * 只靠颜色区分不够：色觉差异用户看不出灰与白的层级差
 * （PRODUCT.md §8「颜色不是唯一状态提示」），所以必须叠加虚线。
 */
@Composable
fun MissingValue(
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    val missingColor = DqdTheme.sports.missing
    Box(
        modifier = modifier.semantics { contentDescription = "暂无数据" },
    ) {
        Text(
            text = "—",
            style = style,
            color = missingColor,
            modifier = Modifier
                .padding(bottom = 2.dp)
                .drawBehind {
                    drawLine(
                        color = missingColor,
                        start = Offset(0f, size.height + 2.dp.toPx()),
                        end = Offset(size.width, size.height + 2.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(2.dp.toPx(), 2.dp.toPx()),
                        ),
                    )
                },
        )
    }
}

/**
 * 数值文本。[value] 为 null 时渲染 [MissingValue]。
 *
 * 页面一律使用本组件展示可能缺失的数值，
 * **不得**自行写 `value ?: 0` 或 `value.orEmpty()` ——
 * 那会把「服务端没提供」伪造成「确实是零」。
 */
@Composable
fun ValueText(
    value: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    if (value == null) {
        MissingValue(modifier = modifier, style = style)
    } else {
        Text(text = value, style = style, modifier = modifier)
    }
}

@Composable
fun ValueText(
    value: Int?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    ValueText(value = value?.toString(), modifier = modifier, style = style)
}

/** 供绘制虚线空槽（如缺失统计项的对比条）使用。 */
internal fun Modifier.dashedTrack(color: androidx.compose.ui.graphics.Color): Modifier =
    drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
        )
    }

@Preview(name = "缺失 vs 零值", showBackground = true, backgroundColor = 0xFF0E1417)
@Composable
private fun MissingValuePreview() {
    DqdTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp),
        ) {
            ValueText(value = 0, style = DqdTheme.dataText.statValue)
            ValueText(value = null as Int?, style = DqdTheme.dataText.statValue)
        }
    }
}
