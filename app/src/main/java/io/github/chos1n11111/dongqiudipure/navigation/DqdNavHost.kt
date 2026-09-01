package io.github.chos1n11111.dongqiudipure.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EntityRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.feature.account.AccountRoute
import io.github.chos1n11111.dongqiudipure.feature.article.ArticleRoute
import io.github.chos1n11111.dongqiudipure.feature.entities.PlayerProfileRoute
import io.github.chos1n11111.dongqiudipure.feature.entities.TeamProfileRoute
import io.github.chos1n11111.dongqiudipure.feature.home.HomeRoute
import io.github.chos1n11111.dongqiudipure.feature.matches.MatchDetailRoute
import io.github.chos1n11111.dongqiudipure.feature.matches.MatchesRoute
import io.github.chos1n11111.dongqiudipure.feature.rankings.DataHubRoute
import io.github.chos1n11111.dongqiudipure.feature.rankings.StandingsRoute
import io.github.chos1n11111.dongqiudipure.feature.search.SearchRoute
import io.github.chos1n11111.dongqiudipure.feature.settings.AboutScreen
import io.github.chos1n11111.dongqiudipure.feature.settings.LicenseScreen
import io.github.chos1n11111.dongqiudipure.feature.settings.SettingsScreen
import io.github.chos1n11111.dongqiudipure.feature.settings.ThemeMode

@Composable
fun DqdNavHost(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    appVersion: String,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DqdDestination.Home.route,
        modifier = modifier,
    ) {
        // ── 根目的地 ────────────────────────────────────────────────────────
        composable(DqdDestination.Home.route) {
            HomeRoute(
                onArticleClick = { navController.navigate(DqdRoutes.article(it.raw)) },
                onSearchClick = { navController.navigate(DqdRoutes.SEARCH) },
            )
        }

        composable(DqdDestination.Matches.route) {
            MatchesRoute(
                onMatchClick = { navController.navigate(DqdRoutes.match(it.raw)) },
                onSearchClick = { navController.navigate(DqdRoutes.SEARCH) },
            )
        }

        composable(DqdDestination.Data.route) {
            DataHubRoute(
                onSearchClick = { navController.navigate(DqdRoutes.SEARCH) },
                onTeamClick = { navController.navigate(DqdRoutes.team(it.raw)) },
                onPlayerClick = { navController.navigate(DqdRoutes.player(it.raw)) },
                onMatchClick = { navController.navigate(DqdRoutes.match(it.raw)) },
            )
        }

        composable(DqdDestination.Account.route) {
            AccountRoute(
                onSettingsClick = { navController.navigate(DqdRoutes.SETTINGS) },
                onAboutClick = { navController.navigate(DqdRoutes.ABOUT) },
                onLicenseClick = { navController.navigate(DqdRoutes.LICENSE) },
                appVersion = appVersion,
            )
        }

        // ── 内容目的地 ──────────────────────────────────────────────────────
        // 导航参数只传稳定 ID，资料由目标页的 Repository 重新加载
        // （ARCHITECTURE.md §5.1）。

        composable(
            route = DqdRoutes.ARTICLE,
            arguments = listOf(navArgument(ARG_ARTICLE_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(ARG_ARTICLE_ID).orEmpty()
            ArticleRoute(
                articleId = ArticleId(id),
                onBack = navController::popBackStack,
                onEntityClick = { entity ->
                    when (entity) {
                        is EntityRef.Team ->
                            navController.navigate(DqdRoutes.team(entity.id.raw))

                        is EntityRef.Competition ->
                            navController.navigate(DqdRoutes.standings(entity.id.raw))

                        is EntityRef.Player ->
                            navController.navigate(DqdRoutes.player(entity.id.raw))
                    }
                },
            )
        }

        composable(
            route = DqdRoutes.MATCH,
            arguments = listOf(navArgument(ARG_MATCH_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(ARG_MATCH_ID).orEmpty()
            MatchDetailRoute(
                matchId = MatchId(id),
                onBack = navController::popBackStack,
                onTeamClick = { navController.navigate(DqdRoutes.team(it.raw)) },
                onPlayerClick = { navController.navigate(DqdRoutes.player(it.raw)) },
            )
        }

        composable(
            route = DqdRoutes.TEAM,
            arguments = listOf(navArgument(ARG_TEAM_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(ARG_TEAM_ID).orEmpty()
            TeamProfileRoute(
                teamId = TeamId(id),
                onBack = navController::popBackStack,
                onMatchClick = { navController.navigate(DqdRoutes.match(it.raw)) },
                onPlayerClick = { navController.navigate(DqdRoutes.player(it.raw)) },
                onArticleClick = { navController.navigate(DqdRoutes.article(it.raw)) },
            )
        }

        composable(
            route = DqdRoutes.PLAYER,
            arguments = listOf(navArgument(ARG_PLAYER_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(ARG_PLAYER_ID).orEmpty()
            PlayerProfileRoute(
                playerId = PlayerId(id),
                onBack = navController::popBackStack,
                onTeamClick = { navController.navigate(DqdRoutes.team(it.raw)) },
            )
        }

        composable(
            route = DqdRoutes.STANDINGS,
            arguments = listOf(navArgument(ARG_COMPETITION_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(ARG_COMPETITION_ID).orEmpty()
            StandingsRoute(
                competitionId = CompetitionId(id),
                onBack = navController::popBackStack,
                onTeamClick = { navController.navigate(DqdRoutes.team(it.raw)) },
                onPlayerClick = { navController.navigate(DqdRoutes.player(it.raw)) },
                onMatchClick = { navController.navigate(DqdRoutes.match(it.raw)) },
            )
        }

        composable(DqdRoutes.SEARCH) {
            SearchRoute(
                onBack = navController::popBackStack,
                onTeamClick = { navController.navigate(DqdRoutes.team(it.raw)) },
                onArticleClick = { navController.navigate(DqdRoutes.article(it.raw)) },
                onCompetitionClick = { navController.navigate(DqdRoutes.standings(it.raw)) },
                onPlayerClick = { navController.navigate(DqdRoutes.player(it.raw)) },
            )
        }

        // ── 本机设置 ────────────────────────────────────────────────────────
        composable(DqdRoutes.SETTINGS) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                onBack = navController::popBackStack,
            )
        }

        composable(DqdRoutes.ABOUT) {
            AboutScreen(
                appVersion = appVersion,
                onBack = navController::popBackStack,
                onLicenseClick = { navController.navigate(DqdRoutes.LICENSE) },
            )
        }

        composable(DqdRoutes.LICENSE) {
            LicenseScreen(onBack = navController::popBackStack)
        }
    }
}
