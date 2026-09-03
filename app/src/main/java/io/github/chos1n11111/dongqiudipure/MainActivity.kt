package io.github.chos1n11111.dongqiudipure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import io.github.chos1n11111.dongqiudipure.core.designsystem.theme.DqdTheme
import io.github.chos1n11111.dongqiudipure.feature.settings.SettingsStore
import io.github.chos1n11111.dongqiudipure.feature.settings.FootballPreferences
import io.github.chos1n11111.dongqiudipure.feature.settings.NewsPreferences
import io.github.chos1n11111.dongqiudipure.feature.settings.ThemeMode
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsStore.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.System)
            val footballPreferences by settingsStore.footballPreferences
                .collectAsStateWithLifecycle(initialValue = FootballPreferences())
            val newsPreferences by settingsStore.newsPreferences
                .collectAsStateWithLifecycle(initialValue = NewsPreferences())

            val darkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            DqdTheme(darkTheme = darkTheme) {
                DqdApp(
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        lifecycleScope.launch { settingsStore.setThemeMode(mode) }
                    },
                    footballPreferences = footballPreferences,
                    newsPreferences = newsPreferences,
                    onDefaultMatchCompetitionChange = { competitionId ->
                        lifecycleScope.launch {
                            settingsStore.setDefaultMatchCompetition(competitionId)
                        }
                    },
                    onMatchCompetitionToggle = { competitionId, enabled ->
                        lifecycleScope.launch {
                            settingsStore.setMatchCompetitionEnabled(competitionId, enabled)
                        }
                    },
                    onRankingCompetitionToggle = { competitionId, enabled ->
                        lifecycleScope.launch {
                            settingsStore.setRankingCompetitionEnabled(competitionId, enabled)
                        }
                    },
                    onNewsCategoryToggle = { categoryId, enabled ->
                        lifecycleScope.launch {
                            settingsStore.setNewsCategoryEnabled(categoryId, enabled)
                        }
                    },
                    onNewsFootballOnlyChange = { enabled ->
                        lifecycleScope.launch { settingsStore.setNewsFootballOnly(enabled) }
                    },
                    appVersion = BuildConfig.VERSION_NAME,
                )
            }
        }
    }
}
