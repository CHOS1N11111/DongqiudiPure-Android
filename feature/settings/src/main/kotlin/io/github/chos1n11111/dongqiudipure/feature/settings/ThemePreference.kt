package io.github.chos1n11111.dongqiudipure.feature.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

data class FootballPreferences(
    val matchCompetitionIds: Set<String> = emptySet(),
    val defaultMatchCompetitionId: String? = null,
    val rankingCompetitionIds: Set<String> = DEFAULT_RANKING_COMPETITION_IDS,
)

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

    val footballPreferences: Flow<FootballPreferences> = context.settingsDataStore.data.map { preferences ->
        FootballPreferences(
            matchCompetitionIds = preferences[MATCH_COMPETITION_IDS_KEY].orEmpty(),
            defaultMatchCompetitionId = preferences[DEFAULT_MATCH_COMPETITION_ID_KEY],
            rankingCompetitionIds = preferences[RANKING_COMPETITION_IDS_KEY]
                ?: DEFAULT_RANKING_COMPETITION_IDS,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setMatchCompetitionEnabled(competitionId: String, enabled: Boolean) {
        requireCompetitionId(competitionId)
        context.settingsDataStore.edit { preferences ->
            val selected = preferences[MATCH_COMPETITION_IDS_KEY].orEmpty().toMutableSet()
            if (enabled) selected += competitionId else selected -= competitionId
            preferences[MATCH_COMPETITION_IDS_KEY] = selected
            if (!enabled && preferences[DEFAULT_MATCH_COMPETITION_ID_KEY] == competitionId) {
                preferences.remove(DEFAULT_MATCH_COMPETITION_ID_KEY)
            }
        }
    }

    suspend fun setDefaultMatchCompetition(competitionId: String?) {
        competitionId?.let(::requireCompetitionId)
        context.settingsDataStore.edit { preferences ->
            if (competitionId == null) {
                preferences.remove(DEFAULT_MATCH_COMPETITION_ID_KEY)
            } else if (competitionId in preferences[MATCH_COMPETITION_IDS_KEY].orEmpty()) {
                preferences[DEFAULT_MATCH_COMPETITION_ID_KEY] = competitionId
            }
        }
    }

    suspend fun setRankingCompetitionEnabled(competitionId: String, enabled: Boolean) {
        requireCompetitionId(competitionId)
        context.settingsDataStore.edit { preferences ->
            val selected = (preferences[RANKING_COMPETITION_IDS_KEY]
                ?: DEFAULT_RANKING_COMPETITION_IDS).toMutableSet()
            if (enabled) selected += competitionId else selected -= competitionId
            preferences[RANKING_COMPETITION_IDS_KEY] = selected
        }
    }

    private fun requireCompetitionId(value: String) {
        require(value.isNotEmpty() && value.all(Char::isDigit))
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val MATCH_COMPETITION_IDS_KEY = stringSetPreferencesKey("match_competition_ids")
        val DEFAULT_MATCH_COMPETITION_ID_KEY = stringPreferencesKey("default_match_competition_id")
        val RANKING_COMPETITION_IDS_KEY = stringSetPreferencesKey("ranking_competition_ids")
    }
}

val DEFAULT_RANKING_COMPETITION_IDS: Set<String> = setOf("4", "3", "9", "5", "12", "43")
