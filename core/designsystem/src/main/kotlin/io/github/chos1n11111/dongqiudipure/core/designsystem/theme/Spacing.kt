package io.github.chos1n11111.dongqiudipure.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * 间距与尺寸令牌。
 *
 * 4dp 基准。列表水平内边距用 14dp 是唯一例外 ——
 * 在 360dp 宽度下多让出 4dp 给数据列。
 */
object DqdSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** 列表行与 section 的水平内边距。 */
    val listHorizontal = 14.dp

    /** section 之间的分隔厚度。 */
    val sectionGap = 8.dp
}

object DqdSize {
    /** 所有可点区域的最小尺寸（Material 触摸目标）。 */
    val touchTarget = 48.dp

    val topAppBar = 52.dp
    val bottomNav = 60.dp

    val crestSmall = 19.dp
    val crestMedium = 34.dp
    val crestLarge = 50.dp

    val thumbnailWidth = 112.dp
    val thumbnailHeight = 74.dp
    val coverHeight = 180.dp

    val iconSmall = 15.dp
    val iconMedium = 20.dp
}
