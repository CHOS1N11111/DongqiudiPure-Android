package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.network.di.ApiBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.di.SportDataBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchAnalysisDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchNewsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchOverviewDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.DataMenuEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.EntityFeedEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CompetitionScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerAbilityEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerHeatMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerMatchesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerShotMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingTypesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamMembersEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamSampleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamStatisticDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamTransferEnvelopeDto
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpFootballRemoteDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    @param:ApiBaseUrl private val apiBaseUrl: HttpUrl,
    @param:SportDataBaseUrl private val sportDataBaseUrl: HttpUrl,
) : FootballRemoteDataSource {

    override suspend fun loadImportantMatches(startDate: LocalDate): ApiResult<MatchListEnvelopeDto> {
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/tab/new/important")
            .addQueryParameter("start", "${startDate.format(DATE)} 16:00:00")
            .addQueryParameter("init", "1")
            .addQueryParameter("platform", "www")
            .addQueryParameter("version", "576")
            .build()
        return get(url, MATCHES_ENDPOINT, MatchListEnvelopeDto.serializer())
    }

    override suspend fun loadMatchDetail(matchId: MatchId): ApiResult<MatchDetailEnvelopeDto> {
        requireId(matchId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/detail/match")
            .addPathSegment(matchId.raw)
            .addQueryParameter("platform", "iphone")
            .addQueryParameter("version", "719")
            .build()
        return get(url, MATCH_DETAIL_ENDPOINT, MatchDetailEnvelopeDto.serializer())
    }

    override suspend fun loadMatchOverview(matchId: MatchId): ApiResult<MatchOverviewDto> {
        requireId(matchId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/overview/match")
            .addPathSegment(matchId.raw)
            .build()
        return get(url, MATCH_OVERVIEW_ENDPOINT, MatchOverviewDto.serializer())
    }

    override suspend fun loadMatchNews(matchId: MatchId): ApiResult<MatchNewsEnvelopeDto> {
        requireId(matchId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/news/match")
            .addPathSegment(matchId.raw)
            .build()
        return get(url, MATCH_NEWS_ENDPOINT, MatchNewsEnvelopeDto.serializer())
    }

    override suspend fun loadMatchLineup(matchId: MatchId): ApiResult<MatchLineupEnvelopeDto> {
        requireId(matchId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/v1/match/lineup")
            .addPathSegment(matchId.raw)
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, MATCH_LINEUP_ENDPOINT, MatchLineupEnvelopeDto.serializer())
    }

    override suspend fun loadMatchAnalysis(matchId: MatchId): ApiResult<MatchAnalysisDto> {
        requireId(matchId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/match/pre_analysis_v1")
            .addPathSegment(matchId.raw)
            .addQueryParameter("platform", "iphone")
            .addQueryParameter("version", "718")
            .build()
        return get(url, MATCH_ANALYSIS_ENDPOINT, MatchAnalysisDto.serializer())
    }

    override suspend fun loadCompetitionCatalog(): ApiResult<DataMenuEnvelopeDto> {
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/tab/data_menu")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("platform", "ios")
            .addQueryParameter("version", "853")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, CATALOG_ENDPOINT, DataMenuEnvelopeDto.serializer())
    }

    override suspend fun loadSeasons(
        competitionId: CompetitionId,
    ): ApiResult<List<SeasonDto>> {
        requireId(competitionId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/data/seasons")
            .addQueryParameter("competition_id", competitionId.raw)
            .addCommonSportDataParameters()
            .build()
        return get(url, SEASONS_ENDPOINT, ListSerializer(SeasonDto.serializer()))
    }

    override suspend fun loadStandings(
        seasonId: SeasonId,
    ): ApiResult<StandingEnvelopeDto> {
        requireId(seasonId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/data/standing")
            .addQueryParameter("season_id", seasonId.raw)
            .addCommonSportDataParameters()
            .build()
        return get(url, STANDINGS_ENDPOINT, StandingEnvelopeDto.serializer())
    }

    override suspend fun loadCompetitionSchedule(
        seasonId: SeasonId,
    ): ApiResult<CompetitionScheduleEnvelopeDto> {
        requireId(seasonId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/data/schedule")
            .addQueryParameter("season_id", seasonId.raw)
            .addQueryParameter("round_all", "1")
            .addCommonSportDataParameters()
            .build()
        return get(url, COMPETITION_SCHEDULE_ENDPOINT, CompetitionScheduleEnvelopeDto.serializer())
    }

    override suspend fun loadRankingTypes(
        seasonId: SeasonId,
        entity: String,
    ): ApiResult<RankingTypesEnvelopeDto> {
        requireId(seasonId.raw)
        require(entity == "person" || entity == "team")
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/data/ranking/$entity")
            .addQueryParameter("season_id", seasonId.raw)
            .addQueryParameter("type", entity)
            .addCommonSportDataParameters()
            .build()
        return get(url, RANKING_TYPES_ENDPOINT, RankingTypesEnvelopeDto.serializer())
    }

    override suspend fun loadRanking(
        seasonId: SeasonId,
        entity: String,
        metric: String,
    ): ApiResult<RankingDetailEnvelopeDto> {
        requireId(seasonId.raw)
        require(entity == "person" || entity == "team")
        require(metric.isNotEmpty() && metric.all { it.isLetterOrDigit() || it == '_' })
        val path = if (entity == "person") "person_ranking" else "team_ranking"
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/data/$path")
            .addQueryParameter("season_id", seasonId.raw)
            .addQueryParameter("type", metric)
            .addCommonSportDataParameters()
            .build()
        return get(url, RANKING_ENDPOINT, RankingDetailEnvelopeDto.serializer())
    }

    override suspend fun loadTeamSample(teamId: TeamId): ApiResult<TeamSampleDto> {
        requireEntityId(teamId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/team/sample/${teamId.raw.normalizedDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, TEAM_SAMPLE_ENDPOINT, TeamSampleDto.serializer())
    }

    override suspend fun loadTeamDetail(teamId: TeamId): ApiResult<TeamDetailDto> {
        requireEntityId(teamId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("api/data/v1/detail/team/${teamId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, TEAM_DETAIL_ENDPOINT, TeamDetailDto.serializer())
    }

    override suspend fun loadTeamStatistics(
        teamId: TeamId,
        seasonId: String?,
    ): ApiResult<TeamStatisticDto> {
        requireEntityId(teamId.raw)
        require(seasonId == null || seasonId.isNotEmpty() && seasonId.all(Char::isDigit))
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/team/statistic/${teamId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .apply { seasonId?.let { addQueryParameter("season_id", it) } }
            .build()
        return get(url, TEAM_STATISTICS_ENDPOINT, TeamStatisticDto.serializer())
    }

    override suspend fun loadTeamMembers(teamId: TeamId): ApiResult<TeamMembersEnvelopeDto> {
        requireEntityId(teamId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/v1/team/member_v2/${teamId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .build()
        return get(url, TEAM_MEMBERS_ENDPOINT, TeamMembersEnvelopeDto.serializer())
    }

    override suspend fun loadTeamSchedule(
        teamId: TeamId,
        seasonId: String?,
    ): ApiResult<TeamScheduleEnvelopeDto> {
        requireEntityId(teamId.raw)
        require(seasonId == null || seasonId.isNotEmpty() && seasonId.all { it.isDigit() || it == '-' })
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/team/schedule/${teamId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .apply { seasonId?.let { addQueryParameter("season", it) } }
            .build()
        return get(url, TEAM_SCHEDULE_ENDPOINT, TeamScheduleEnvelopeDto.serializer())
    }

    override suspend fun loadTeamTransfers(
        teamId: TeamId,
        windowId: String?,
    ): ApiResult<TeamTransferEnvelopeDto> {
        requireEntityId(teamId.raw)
        require(windowId == null || windowId.isNotEmpty() && windowId.all(Char::isDigit))
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/team/transfer/${teamId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .apply { windowId?.let { addQueryParameter("window", it) } }
            .build()
        return get(url, TEAM_TRANSFERS_ENDPOINT, TeamTransferEnvelopeDto.serializer())
    }

    override suspend fun loadEntityFeed(
        entityId: String,
        type: String,
    ): ApiResult<EntityFeedEnvelopeDto> {
        requireEntityId(entityId)
        require(type == "team" || type == "player")
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("v3/archive/app/channel/feeds")
            .addQueryParameter("id", entityId.fullDqdId())
            .addQueryParameter("type", type)
            .addQueryParameter("platform", "web")
            .addQueryParameter("size", "20")
            .build()
        return get(url, ENTITY_FEED_ENDPOINT, EntityFeedEnvelopeDto.serializer())
    }

    override suspend fun loadPlayerDetail(playerId: PlayerId): ApiResult<PlayerDetailDto> {
        requireEntityId(playerId.raw)
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/v1/detail/person/${playerId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, PLAYER_DETAIL_ENDPOINT, PlayerDetailDto.serializer())
    }

    override suspend fun loadPlayerStatistics(playerId: PlayerId): ApiResult<PlayerStatisticsDto> {
        requireEntityId(playerId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/person/statistic_new/${playerId.raw.fullDqdId()}")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, PLAYER_STATISTICS_ENDPOINT, PlayerStatisticsDto.serializer())
    }

    override suspend fun loadPlayerMatches(
        playerId: PlayerId,
        page: Int,
    ): ApiResult<PlayerMatchesEnvelopeDto> {
        requireEntityId(playerId.raw)
        require(page > 0)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/biz/dqd/person/matches/${playerId.raw.fullDqdId()}")
            .addQueryParameter("lang", "zh-cn")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "20")
            .addQueryParameter("app", "dqd")
            .build()
        return get(url, PLAYER_MATCHES_ENDPOINT, PlayerMatchesEnvelopeDto.serializer())
    }

    override suspend fun loadPlayerHeatMap(
        playerId: PlayerId,
        seasonId: String,
        teamId: TeamId,
    ): ApiResult<PlayerHeatMapDto> {
        requireEntityId(playerId.raw)
        requireEntityId(teamId.raw)
        require(seasonId.isNotEmpty() && seasonId.all(Char::isDigit))
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("sportbiz/h5/person/season/heatmap")
            .addQueryParameter("season_id", seasonId)
            .addQueryParameter("person_id", playerId.raw.normalizedDqdId())
            .addQueryParameter("team_id", teamId.raw.normalizedDqdId())
            .build()
        return get(url, PLAYER_HEAT_MAP_ENDPOINT, PlayerHeatMapDto.serializer())
    }

    override suspend fun loadPlayerShotMap(
        playerId: PlayerId,
        matchId: MatchId,
    ): ApiResult<PlayerShotMapDto> {
        requireEntityId(playerId.raw)
        requireId(matchId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("sportbiz/h5/match/person/shotmap")
            .addQueryParameter("match_id", matchId.raw)
            .addQueryParameter("person_id", playerId.raw.normalizedDqdId())
            .build()
        return get(url, PLAYER_SHOT_MAP_ENDPOINT, PlayerShotMapDto.serializer())
    }

    override suspend fun loadPlayerAbility(playerId: PlayerId): ApiResult<PlayerAbilityEnvelopeDto> {
        requireEntityId(playerId.raw)
        val url = sportDataBaseUrl.newBuilder()
            .addPathSegments("soccer/data/sofifa/v1/player_ability/${playerId.raw.normalizedDqdId()}")
            .addQueryParameter("player_type", "")
            .addQueryParameter("app", "dqd")
            .addQueryParameter("lang", "zh-cn")
            .build()
        return get(url, PLAYER_ABILITY_ENDPOINT, PlayerAbilityEnvelopeDto.serializer())
    }

    private fun HttpUrl.Builder.addCommonSportDataParameters(): HttpUrl.Builder =
        addQueryParameter("app", "dqd")
            .addQueryParameter("platform", "miniprogram")
            .addQueryParameter("version", "830")
            .addQueryParameter("lang", "zh-cn")

    private suspend fun <T> get(
        url: HttpUrl,
        endpoint: EndpointId,
        deserializer: DeserializationStrategy<T>,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body.string()
                if (!response.isSuccessful) {
                    return@withContext ApiResult.Failure(httpError(response.code, body))
                }
                try {
                    ApiResult.Success(json.decodeFromString(deserializer, body))
                } catch (_: SerializationException) {
                    ApiResult.Failure(AppError.Parse(endpoint))
                }
            }
        } catch (_: SocketTimeoutException) {
            ApiResult.Failure(AppError.Network(NetworkKind.Timeout))
        } catch (_: SSLException) {
            ApiResult.Failure(AppError.Network(NetworkKind.TlsFailure))
        } catch (_: UnknownHostException) {
            ApiResult.Failure(AppError.Network(NetworkKind.NoConnection))
        } catch (_: ConnectException) {
            ApiResult.Failure(AppError.Network(NetworkKind.NoConnection))
        } catch (_: IOException) {
            ApiResult.Failure(AppError.Network(NetworkKind.Unknown))
        }
    }

    private fun httpError(status: Int, body: String): AppError {
        if (status == 429) return AppError.RateLimited(retryAfter = null)
        val error = runCatching { json.parseToJsonElement(body) as? JsonObject }.getOrNull()
        val code = error?.get("errCode").scalarString() ?: error?.get("code").scalarString()
        val message = error?.get("errMesg").scalarString()
            ?: error?.get("errMsg").scalarString()
            ?: error?.get("message").scalarString()
        return if (code != null || message != null) {
            AppError.Server(code = code, message = message)
        } else {
            AppError.Http(status)
        }
    }

    private fun requireId(value: String) {
        require(value.isNotEmpty() && value.all(Char::isDigit))
    }

    private fun requireEntityId(value: String) = requireId(value)

    private fun String.normalizedDqdId(): String {
        val normalized = if (length >= 8 && startsWith("50")) drop(2).trimStart('0') else this
        return normalized.ifEmpty { "0" }
    }

    private fun String.fullDqdId(): String = if (length >= 8 && startsWith("50")) {
        this
    } else {
        "50${padStart(6, '0')}"
    }

    private companion object {
        val MATCHES_ENDPOINT = EndpointId("football.matches")
        val MATCH_DETAIL_ENDPOINT = EndpointId("football.match-detail")
        val MATCH_OVERVIEW_ENDPOINT = EndpointId("football.match-overview")
        val MATCH_NEWS_ENDPOINT = EndpointId("football.match-news")
        val MATCH_LINEUP_ENDPOINT = EndpointId("football.match-lineup")
        val MATCH_ANALYSIS_ENDPOINT = EndpointId("football.match-analysis")
        val CATALOG_ENDPOINT = EndpointId("football.catalog")
        val SEASONS_ENDPOINT = EndpointId("football.seasons")
        val STANDINGS_ENDPOINT = EndpointId("football.standings")
        val COMPETITION_SCHEDULE_ENDPOINT = EndpointId("football.competition-schedule")
        val RANKING_TYPES_ENDPOINT = EndpointId("football.ranking-types")
        val RANKING_ENDPOINT = EndpointId("football.ranking")
        val TEAM_SAMPLE_ENDPOINT = EndpointId("football.team-sample")
        val TEAM_DETAIL_ENDPOINT = EndpointId("football.team-detail")
        val TEAM_STATISTICS_ENDPOINT = EndpointId("football.team-statistics")
        val TEAM_MEMBERS_ENDPOINT = EndpointId("football.team-members")
        val TEAM_SCHEDULE_ENDPOINT = EndpointId("football.team-schedule")
        val TEAM_TRANSFERS_ENDPOINT = EndpointId("football.team-transfers")
        val ENTITY_FEED_ENDPOINT = EndpointId("football.entity-feed")
        val PLAYER_DETAIL_ENDPOINT = EndpointId("football.player-detail")
        val PLAYER_STATISTICS_ENDPOINT = EndpointId("football.player-statistics")
        val PLAYER_MATCHES_ENDPOINT = EndpointId("football.player-matches")
        val PLAYER_HEAT_MAP_ENDPOINT = EndpointId("football.player-heat-map")
        val PLAYER_SHOT_MAP_ENDPOINT = EndpointId("football.player-shot-map")
        val PLAYER_ABILITY_ENDPOINT = EndpointId("football.player-ability")
        val DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}
