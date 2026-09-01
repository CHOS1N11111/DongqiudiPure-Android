package io.github.chos1n11111.dongqiudipure.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * 中文不打包自定义字体。
 *
 * 思源黑体单个字重约 5–15 MB，会让 APK 体积翻倍，与「轻量客户端」的定位冲突。
 * 中文走系统字面（HarmonyOS Sans / MiSans / Source Han Sans 等，随设备而定）。
 *
 * TODO(设计): 拉丁字形与数字可另行引入 Barlow Semi Condensed（仅 Latin 子集，
 * 每字重约 30–50 KB），以取得记分牌式的紧缩观感。当前先用系统字面 + tnum。
 */
private val DqdFontFamily = FontFamily.Default

/** 等宽数字。比分与统计列在实时刷新时不因 1 与 8 的宽度差而抖动。 */
private const val TABULAR_FIGURES = "tnum"

internal val DqdTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    // 文章标题
    headlineSmall = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 30.sp,
    ),
    // 顶部应用栏标题
    titleLarge = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    // section 标题
    titleMedium = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // 文章正文
    bodyLarge = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 28.sp,
    ),
    // 资讯流标题
    bodyMedium = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // meta 行：来源 · 时间 · 评论数
    labelSmall = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
)

/**
 * 数据字型。
 *
 * 比分、分钟、积分和统计值不属于 Material 3 的文本角色，
 * 但它们必须统一开启等宽数字，所以单独成组下发。
 */
@Immutable
data class DataTextStyles(
    val scoreLarge: TextStyle,
    val scoreMedium: TextStyle,
    val minuteLabel: TextStyle,
    val statValue: TextStyle,
    val tableCell: TextStyle,
    val tableCellStrong: TextStyle,
)

internal val DqdDataTextStyles = DataTextStyles(
    scoreLarge = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    scoreMedium = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    minuteLabel = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    statValue = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    tableCell = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
    tableCellStrong = TextStyle(
        fontFamily = DqdFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = TABULAR_FIGURES,
    ),
)

internal val LocalDataTextStyles = staticCompositionLocalOf { DqdDataTextStyles }
