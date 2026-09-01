package io.github.chos1n11111.dongqiudipure.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * 主色刻意避开红、绿、琥珀三个色相。
 *
 * 在足球界面里这三个色相全部承担语义：红牌与降级区、胜场与进球、黄牌与警告。
 * 品牌色若也落在其中，就会与 LIVE 状态和胜负标记争夺同一套视觉编码。
 * 青绿同时满足 DECISIONS.md D-011「不以视觉方式暗示官方授权」。
 *
 * 中性色带轻微冷青偏移，不是纯灰。
 */

// ── 深色 ────────────────────────────────────────────────────────────────────
private val DarkPrimary = Color(0xFF3DD6C0)
private val DarkOnPrimary = Color(0xFF062B27)
private val DarkPrimaryContainer = Color(0xFF12332F)
private val DarkOnPrimaryContainer = Color(0xFF6FE3D3)

private val DarkBackground = Color(0xFF0E1417)
private val DarkOnBackground = Color(0xFFE8F0F1)
private val DarkOnSurfaceVariant = Color(0xFF93A7AE)

// ── 浅色 ────────────────────────────────────────────────────────────────────
// 不是深色反相：主色降至 #00786B 才能在白底达到 4.5:1。
private val LightPrimary = Color(0xFF00786B)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFDBF0EC)
private val LightOnPrimaryContainer = Color(0xFF00201C)

private val LightBackground = Color(0xFFF1F5F5)
private val LightOnBackground = Color(0xFF0D1517)
private val LightOnSurfaceVariant = Color(0xFF53666C)

internal val DqdDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Color(0xFF9FB6BC),
    onSecondary = Color(0xFF0E1417),
    secondaryContainer = Color(0xFF243138),
    onSecondaryContainer = Color(0xFFCFDEE2),
    tertiary = Color(0xFF8FB6E8),
    onTertiary = Color(0xFF0B1A2A),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkBackground,
    onSurface = DarkOnBackground,
    surfaceVariant = Color(0xFF1D282D),
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0A0F11),
    surfaceContainerLow = Color(0xFF121A1E),
    surfaceContainer = Color(0xFF151E22),
    surfaceContainerHigh = Color(0xFF1D282D),
    surfaceContainerHighest = Color(0xFF243138),
    // outline 用于需要 3:1 的边框，outlineVariant 用于分隔线。
    outline = Color(0xFF3C4C53),
    outlineVariant = Color(0xFF27353B),
    error = Color(0xFFE5484D),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3A1416),
    onErrorContainer = Color(0xFFFFB4B4),
    inverseSurface = Color(0xFFE8F0F1),
    inverseOnSurface = Color(0xFF0E1417),
    scrim = Color(0xFF000000),
)

internal val DqdLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = Color(0xFF4A6068),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE7EA),
    onSecondaryContainer = Color(0xFF15272D),
    tertiary = Color(0xFF3A63C0),
    onTertiary = Color(0xFFFFFFFF),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground,
    onSurface = LightOnBackground,
    surfaceVariant = Color(0xFFE3EBEB),
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7FAFA),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFEFF4F4),
    surfaceContainerHighest = Color(0xFFEAF0F0),
    outline = Color(0xFF6F8288),
    outlineVariant = Color(0xFFDEE8E9),
    error = Color(0xFFC3373B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD9),
    onErrorContainer = Color(0xFF410004),
    inverseSurface = Color(0xFF2A3439),
    inverseOnSurface = Color(0xFFF1F5F5),
    scrim = Color(0xFF000000),
)

/**
 * 足球语义色。
 *
 * 这些颜色不属于 Material 3 的标准角色，[androidx.compose.material3.ColorScheme]
 * 里没有它们的位置。如果直接把色值写死在组件里，深色下正常、浅色下就会对比度不足 ——
 * 所以它们和标准角色一样有两套取值，经 [LocalSportsColors] 下发。
 *
 * 每一项都只是「第二重编码」：颜色必须与文字或图形同时使用，
 * 不得作为唯一状态提示（PRODUCT.md §8 可访问性）。
 */
@Immutable
data class SportsColors(
    /** 进行中。必须与脉冲圆点 +「LIVE」文字同时出现。 */
    val live: Color,
    val win: Color,
    val draw: Color,
    val loss: Color,
    val yellowCard: Color,
    val redCard: Color,
    val zoneChampions: Color,
    val zoneEuropa: Color,
    val zoneConference: Color,
    val zonePromotion: Color,
    val zoneRelegation: Color,
    /** 「暂无数据」记号的颜色。必须弱于正文，与数值 0 拉开层级。 */
    val missing: Color,
)

internal val DarkSportsColors = SportsColors(
    live = Color(0xFFFF5C46),
    win = Color(0xFF35C46E),
    draw = Color(0xFF78898F),
    loss = Color(0xFFE5484D),
    yellowCard = Color(0xFFF0C24B),
    redCard = Color(0xFFE5484D),
    zoneChampions = Color(0xFF5B8DEF),
    zoneEuropa = Color(0xFFC77DE0),
    zoneConference = Color(0xFF4FB3A5),
    zonePromotion = Color(0xFF35C46E),
    zoneRelegation = Color(0xFFE5484D),
    missing = Color(0xFF63767D),
)

// 浅色版全部单独降低明度以在白底达标，不从深色推导。
internal val LightSportsColors = SportsColors(
    live = Color(0xFFCF3A24),
    win = Color(0xFF1D8F4E),
    draw = Color(0xFF6B7C82),
    loss = Color(0xFFC3373B),
    yellowCard = Color(0xFFB98410),
    redCard = Color(0xFFC3373B),
    zoneChampions = Color(0xFF3A63C0),
    zoneEuropa = Color(0xFF8E4CAE),
    zoneConference = Color(0xFF14776B),
    zonePromotion = Color(0xFF1D8F4E),
    zoneRelegation = Color(0xFFC3373B),
    missing = Color(0xFF7B8E94),
)

internal val LocalSportsColors = staticCompositionLocalOf { DarkSportsColors }
