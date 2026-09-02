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
                    appVersion = BuildConfig.VERSION_NAME,
                )
            }
        }
    }
}
