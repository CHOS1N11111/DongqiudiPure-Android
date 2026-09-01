package io.github.chos1n11111.dongqiudipure.feature.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 主题偏好。
 *
 * 三档而不是开关：「跟随系统」必须是可选项且是默认值 ——
 * 用户在系统层面已经表达过一次偏好，应用不该单方面覆盖它。
 */
enum class ThemeMode(@StringRes val labelRes: Int) {
    System(R.string.settings_theme_system),
    Light(R.string.settings_theme_light),
    Dark(R.string.settings_theme_dark),
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dqd_settings",
)

/**
 * 本机 UI 设置。
 *
 * 用 DataStore 而不是 SharedPreferences（ARCHITECTURE.md §10）。
 * 这里只存 UI 偏好；**凭据绝不能进入这里**，那需要 Keystore 支持的加密存储。
 */
class SettingsStore(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { preferences ->
        val raw = preferences[THEME_MODE_KEY]
        // 读到无法识别的值时回退到默认，而不是崩溃 ——
        // 降级或回滚安装后可能残留旧格式。
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.System
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
