package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.network.di.ApiBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.di.SportDataBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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

    override suspend fun loadImportantMatches(): ApiResult<MatchListEnvelopeDto> {
        val url = apiBaseUrl.newBuilder()
            .addPathSegments("data/tab/new/important")
            .addQueryParameter("init", "1")
            .build()
        return get(url, MATCHES_ENDPOINT, MatchListEnvelopeDto.serializer())
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

    private companion object {
        val MATCHES_ENDPOINT = EndpointId("football.matches")
        val SEASONS_ENDPOINT = EndpointId("football.seasons")
        val STANDINGS_ENDPOINT = EndpointId("football.standings")
    }
}
