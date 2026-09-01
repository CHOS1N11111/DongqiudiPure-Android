package io.github.chos1n11111.dongqiudipure

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.chos1n11111.dongqiudipure.ui.theme.DongqiudiPureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DongqiudiPureTheme {
                DongqiudiPureApp()
            }
        }
    }
}

@Composable
private fun DongqiudiPureApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: RootDestination.News.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                RootDestination.entries.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) {
                                    destination.selectedIcon
                                } else {
                                    destination.unselectedIcon
                                },
                                contentDescription = null,
                            )
                        },
                        label = {
                            Text(text = stringResource(destination.labelRes))
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RootDestination.News.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            RootDestination.entries.forEach { destination ->
                composable(destination.route) {
                    EmptyDestinationScreen(
                        titleRes = destination.labelRes,
                        emptyMessageRes = destination.emptyMessageRes,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDestinationScreen(
    @StringRes titleRes: Int,
    @StringRes emptyMessageRes: Int,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(emptyMessageRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewsScreenPreview() {
    DongqiudiPureTheme {
        EmptyDestinationScreen(
            titleRes = R.string.news_title,
            emptyMessageRes = R.string.news_empty,
        )
    }
}

private enum class RootDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val emptyMessageRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    News(
        route = "news",
        labelRes = R.string.news_title,
        emptyMessageRes = R.string.news_empty,
        selectedIcon = Icons.Filled.Newspaper,
        unselectedIcon = Icons.Outlined.Newspaper,
    ),
    Matches(
        route = "matches",
        labelRes = R.string.matches_title,
        emptyMessageRes = R.string.matches_empty,
        selectedIcon = Icons.Filled.SportsSoccer,
        unselectedIcon = Icons.Outlined.SportsSoccer,
    ),
    Data(
        route = "data",
        labelRes = R.string.data_title,
        emptyMessageRes = R.string.data_empty,
        selectedIcon = Icons.Filled.Leaderboard,
        unselectedIcon = Icons.Outlined.Leaderboard,
    ),
}
