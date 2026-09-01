package io.github.chos1n11111.dongqiudipure

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.chos1n11111.dongqiudipure.feature.settings.ThemeMode
import io.github.chos1n11111.dongqiudipure.navigation.DqdDestination
import io.github.chos1n11111.dongqiudipure.navigation.DqdNavHost

/**
 * 应用外壳。
 *
 * 使用 [NavigationSuiteScaffold] 实现 DECISIONS.md D-016：
 * compact 宽度显示底栏，medium / expanded 自动切换为 NavigationRail，
 * 四个目的地的语义保持一致。折叠屏与平板不需要另写一套导航。
 */
@Composable
fun DqdApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // 只有停留在根目的地时才显示导航栏。进入文章、比赛详情等内容页后隐藏，
    // 让内容占满屏幕 —— 这些页面靠返回而不是切 tab 退出。
    val isRootDestination = DqdDestination.entries.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            if (!isRootDestination) return@NavigationSuiteScaffold
            DqdDestination.entries.forEach { destination ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == destination.route
                } == true

                item(
                    selected = selected,
                    onClick = { navController.navigateToRoot(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            // 图标旁已有可见文字标签，图标本身对无障碍树是装饰性的。
                            contentDescription = null,
                        )
                    },
                    // 底栏项必须同时有图标与文字标签：纯图标导航损害可发现性。
                    label = { Text(stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        DqdNavHost(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            appVersion = appVersion,
            navController = navController,
        )
    }
}

/**
 * 切换根目的地。
 *
 * 保存并恢复各 tab 自己的返回栈与滚动位置，返回行为可预测
 * （PRODUCT.md §8、Material 导航规范）。
 */
private fun NavHostController.navigateToRoot(destination: DqdDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
