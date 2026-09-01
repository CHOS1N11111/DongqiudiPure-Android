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

class MainActivity : ComponentActivity() {

    // 手动 composition root（DECISIONS.md D-007）：对象图还很小，
    // 暂不引入 DI 框架。
    private val settingsStore by lazy { SettingsStore(applicationContext) }

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
