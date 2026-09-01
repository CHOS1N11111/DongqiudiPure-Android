package io.github.chos1n11111.dongqiudipure.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.chos1n11111.dongqiudipure.core.designsystem.icon.DqdIcons
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdSpacing
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.feature.account.AccountRoute
import io.github.chos1n11111.dongqiudipure.feature.home.HomeRoute
import io.github.chos1n11111.dongqiudipure.feature.matches.MatchesRoute

@Composable
fun DqdNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DqdDestination.Home.route,
        modifier = modifier,
    ) {
        composable(DqdDestination.Home.route) {
            HomeRoute(
                onArticleClick = { id: ArticleId ->
                    navController.navigate(DqdRoutes.article(id.raw))
                },
                onSearchClick = { navController.navigate(DqdRoutes.SEARCH) },
            )
        }

        composable(DqdDestination.Matches.route) {
            MatchesRoute(
                onMatchClick = { id: MatchId ->
                    navController.navigate(DqdRoutes.match(id.raw))
                },
                onSearchClick = { navController.navigate(DqdRoutes.SEARCH) },
            )
        }

        composable(DqdDestination.Account.route) {
            AccountRoute(
                onSettingsClick = { navController.navigate(DqdRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(DqdRoutes.ABOUT) },
                onLicenseClick = { navController.navigate(DqdRoutes.LICENSE) },
            )
        }

        // ── 以下目的地的页面尚未实现 ──────────────────────────────────────
        // 它们已经接入导航图，是为了让「点击 -> 返回」的返回栈行为现在就可验证；
        // 页面本身要等各自的 contract gate。
        // 见 docs/engineering/BACKEND-CONTRACT-TODO.md

        composable(
            route = DqdRoutes.ARTICLE,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType }),
        ) { entry ->
            PendingScreen(
                title = "文章详情",
                milestone = "M3",
                detail = "articleId = ${entry.arguments?.getString("articleId")}",
            )
        }

        composable(
            route = DqdRoutes.MATCH,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
        ) { entry ->
            PendingScreen(
                title = "比赛详情",
                milestone = "M4 / M5",
                detail = "matchId = ${entry.arguments?.getString("matchId")}",
            )
        }

        composable(DqdRoutes.SEARCH) { PendingScreen("搜索", "M8") }
        composable(DqdRoutes.SETTINGS) { PendingScreen("设置", "M16") }
        composable(DqdRoutes.ABOUT) { PendingScreen("关于", "M1") }
        composable(DqdRoutes.LICENSE) { PendingScreen("开源许可", "M1") }
    }
}

/**
 * 尚未实现的目的地。
 *
 * 明确写出所属 milestone，而不是显示一个空白页或假装加载中 ——
 * 应用的当前状态应当自解释。
 */
@Composable
private fun PendingScreen(
    title: String,
    milestone: String,
    detail: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DqdSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DqdSpacing.sm, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(DqdIcons.Info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "该页面属于 $milestone，需要先完成对应的 contract 验证。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
