package io.github.chos1n11111.dongqiudipure.feature.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntity
import io.github.chos1n11111.dongqiudipure.core.model.FollowedEntityPreferences
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRef
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import java.net.URI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

data class NewsPreferences(
    val categoryIds: Set<String> = DEFAULT_NEWS_CATEGORY_IDS,
    val footballOnly: Boolean = true,
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

    val newsPreferences: Flow<NewsPreferences> = context.settingsDataStore.data.map { preferences ->
        NewsPreferences(
            categoryIds = preferences[NEWS_CATEGORY_IDS_KEY]
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_NEWS_CATEGORY_IDS,
            footballOnly = preferences[NEWS_FOOTBALL_ONLY_KEY] ?: true,
        )
    }

    val followedEntityPreferences: Flow<FollowedEntityPreferences> =
        context.settingsDataStore.data.map { preferences ->
            decodeFollowedEntities(preferences[FOLLOWED_ENTITIES_KEY])
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

    suspend fun setNewsCategoryEnabled(categoryId: String, enabled: Boolean) {
        requireCompetitionId(categoryId)
        context.settingsDataStore.edit { preferences ->
            val selected = (preferences[NEWS_CATEGORY_IDS_KEY]
                ?: DEFAULT_NEWS_CATEGORY_IDS).toMutableSet()
            if (enabled) selected += categoryId else selected -= categoryId
            if (selected.isNotEmpty()) preferences[NEWS_CATEGORY_IDS_KEY] = selected
        }
    }

    suspend fun setNewsFootballOnly(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NEWS_FOOTBALL_ONLY_KEY] = enabled
        }
    }

    suspend fun addFollowedEntity(entity: FollowedEntity, makeMainTeam: Boolean = false) {
        val validated = entity.validated() ?: return
        context.settingsDataStore.edit { preferences ->
            val current = decodeFollowedEntities(preferences[FOLLOWED_ENTITIES_KEY])
            val existingIndex = current.entities.indexOfFirst { it.stableKey == validated.stableKey }
            val entities = current.entities.toMutableList().apply {
                if (existingIndex >= 0) this[existingIndex] = validated else add(validated)
            }.take(MAX_FOLLOWED_ENTITIES)
            val requestedMainId = if (makeMainTeam) {
                (validated as? FollowedEntity.Team)?.team?.id?.takeIf { teamId ->
                    entities.filterIsInstance<FollowedEntity.Team>().any { it.team.id == teamId }
                }
            } else {
                null
            }
            val mainTeamId = requestedMainId
                ?: current.mainTeamId
                ?: entities.filterIsInstance<FollowedEntity.Team>().firstOrNull()?.team?.id
            preferences[FOLLOWED_ENTITIES_KEY] = encodeFollowedEntities(
                FollowedEntityPreferences(mainTeamId, entities),
            )
        }
    }

    suspend fun removeFollowedEntity(stableKey: String) {
        context.settingsDataStore.edit { preferences ->
            val current = decodeFollowedEntities(preferences[FOLLOWED_ENTITIES_KEY])
            val entities = current.entities.filterNot { it.stableKey == stableKey }
            val mainTeamId = current.mainTeamId?.takeIf { id ->
                entities.filterIsInstance<FollowedEntity.Team>().any { it.team.id == id }
            } ?: entities.filterIsInstance<FollowedEntity.Team>().firstOrNull()?.team?.id
            preferences[FOLLOWED_ENTITIES_KEY] = encodeFollowedEntities(
                FollowedEntityPreferences(mainTeamId, entities),
            )
        }
    }

    suspend fun setMainTeam(teamId: TeamId) {
        context.settingsDataStore.edit { preferences ->
            val current = decodeFollowedEntities(preferences[FOLLOWED_ENTITIES_KEY])
            if (current.entities.filterIsInstance<FollowedEntity.Team>().none { it.team.id == teamId }) {
                return@edit
            }
            preferences[FOLLOWED_ENTITIES_KEY] = encodeFollowedEntities(current.copy(mainTeamId = teamId))
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
        val NEWS_CATEGORY_IDS_KEY = stringSetPreferencesKey("news_category_ids")
        val NEWS_FOOTBALL_ONLY_KEY = booleanPreferencesKey("news_football_only")
        val FOLLOWED_ENTITIES_KEY = stringPreferencesKey("followed_entities_v1")
    }
}

@Serializable
private data class StoredFollowedEntities(
    val mainTeamId: String? = null,
    val entities: List<StoredFollowedEntity> = emptyList(),
)

@Serializable
private data class StoredFollowedEntity(
    val type: String,
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val secondaryLabel: String? = null,
)

private val FOLLOWED_ENTITY_JSON = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

internal fun encodeFollowedEntities(preferences: FollowedEntityPreferences): String =
    FOLLOWED_ENTITY_JSON.encodeToString(
        StoredFollowedEntities(
            mainTeamId = preferences.mainTeamId?.raw,
            entities = preferences.entities.map { entity ->
                StoredFollowedEntity(
                    type = when (entity) {
                        is FollowedEntity.Team -> "team"
                        is FollowedEntity.Player -> "player"
                    },
                    id = when (entity) {
                        is FollowedEntity.Team -> entity.team.id.raw
                        is FollowedEntity.Player -> entity.player.id.raw
                    },
                    name = entity.name,
                    imageUrl = entity.imageUrl,
                    secondaryLabel = entity.secondaryLabel,
                )
            },
        ),
    )

internal fun decodeFollowedEntities(raw: String?): FollowedEntityPreferences {
    val stored = raw?.let {
        runCatching { FOLLOWED_ENTITY_JSON.decodeFromString<StoredFollowedEntities>(it) }.getOrNull()
    } ?: return FollowedEntityPreferences()
    val entities = stored.entities.asSequence()
        .mapNotNull(StoredFollowedEntity::toDomain)
        .distinctBy(FollowedEntity::stableKey)
        .take(MAX_FOLLOWED_ENTITIES)
        .toList()
    val mainTeamId = stored.mainTeamId
        ?.takeIf(::isValidEntityId)
        ?.let(::TeamId)
        ?.takeIf { id -> entities.filterIsInstance<FollowedEntity.Team>().any { it.team.id == id } }
    return FollowedEntityPreferences(mainTeamId, entities)
}

private fun StoredFollowedEntity.toDomain(): FollowedEntity? {
    if (!isValidEntityId(id)) return null
    val safeName = name.trim().takeIf { it.isNotEmpty() && it.length <= 100 } ?: return null
    val safeImageUrl = imageUrl.safeStoredUrl()
    val safeSecondary = secondaryLabel?.trim()?.takeIf { it.isNotEmpty() }?.take(160)
    return when (type) {
        "team" -> FollowedEntity.Team(TeamRef(TeamId(id), safeName, safeImageUrl), safeSecondary)
        "player" -> FollowedEntity.Player(PlayerRef(PlayerId(id), safeName, safeImageUrl), safeSecondary)
        else -> null
    }
}

private fun FollowedEntity.validated(): FollowedEntity? = StoredFollowedEntity(
    type = when (this) {
        is FollowedEntity.Team -> "team"
        is FollowedEntity.Player -> "player"
    },
    id = when (this) {
        is FollowedEntity.Team -> team.id.raw
        is FollowedEntity.Player -> player.id.raw
    },
    name = name,
    imageUrl = imageUrl,
    secondaryLabel = secondaryLabel,
).toDomain()

private fun isValidEntityId(value: String): Boolean =
    value.isNotEmpty() && value.length <= 20 && value.all(Char::isDigit)

private fun String?.safeStoredUrl(): String? {
    val value = this?.trim()?.takeIf { it.length <= 2048 } ?: return null
    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    return value.takeIf {
        uri.scheme == "https" &&
            (uri.host == "qunliao.info" || uri.host?.endsWith(".qunliao.info") == true)
    }
}

private const val MAX_FOLLOWED_ENTITIES = 20

val DEFAULT_RANKING_COMPETITION_IDS: Set<String> = setOf("4", "3", "9", "5", "12", "43")
val DEFAULT_NEWS_CATEGORY_IDS: Set<String> = setOf("1")
