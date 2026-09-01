package io.github.chos1n11111.dongqiudipure.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 应用主题。
 *
 * 深浅两套色板同时设计并各自验证对比度，不从其中一套推导另一套
 * （浅色主色 #00786B 与深色主色 #3DD6C0 是两次独立取值）。
 *
 * @param darkTheme 默认跟随系统。后续如在设置中提供「跟随系统 / 浅色 / 深色」
 *   三档，由 :feature:settings 传入覆盖值，主题本身不读取偏好存储。
 */
@Composable
fun DqdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DqdDarkColorScheme else DqdLightColorScheme
    val sportsColors = if (darkTheme) DarkSportsColors else LightSportsColors

    CompositionLocalProvider(
        LocalSportsColors provides sportsColors,
        LocalDataTextStyles provides DqdDataTextStyles,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DqdTypography,
            content = content,
        )
    }
}

/**
 * 主题扩展的访问入口。
 *
 * 标准角色仍走 [MaterialTheme]；这里只暴露 Material 3 没有位置可放的东西。
 */
object DqdTheme {

    /** 足球语义色。深浅主题各一套。 */
    val sports: SportsColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSportsColors.current

    /** 比分、分钟、统计值的字型。统一开启等宽数字。 */
    val dataText: DataTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalDataTextStyles.current
}
