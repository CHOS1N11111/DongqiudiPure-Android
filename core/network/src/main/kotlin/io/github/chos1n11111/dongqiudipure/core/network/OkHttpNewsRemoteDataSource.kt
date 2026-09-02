package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.network.di.ApiBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.dto.ArticleDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CommentsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FeedResponseDto
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.net.ssl.SSLException

class OkHttpNewsRemoteDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    @param:ApiBaseUrl private val baseUrl: HttpUrl,
) : NewsRemoteDataSource {

    override suspend fun loadFeed(request: FeedRequest): ApiResult<FeedResponseDto> {
        require(request.tabId.isNotEmpty() && request.tabId.all(Char::isDigit))
        require((request.after == null) == (request.page == null))
        require(request.page == null || request.page > 0)
        val url = baseUrl.newBuilder()
            .addPathSegments("app/tabs/web")
            .addPathSegment("${request.tabId}.json")
            .apply {
                if (request.after != null && request.page != null) {
                    addQueryParameter("after", request.after)
                    addQueryParameter("page", request.page.toString())
                    addQueryParameter("child_tab_id", "0")
                    addQueryParameter("user_pay_type", "")
                }
            }
            .build()
        return get(url, FEED_ENDPOINT, FeedResponseDto.serializer())
    }

    override suspend fun loadArticle(articleId: ArticleId): ApiResult<ArticleDetailEnvelopeDto> {
        val url = baseUrl.newBuilder()
            .addPathSegments("v2/article/detail")
            .addPathSegment(articleId.raw)
            .build()
        return when (val result = get(url, ARTICLE_ENDPOINT, ArticleDetailEnvelopeDto.serializer())) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val envelope = result.value
                val code = envelope.code.scalarString()
                when {
                    code == null -> ApiResult.Failure(AppError.Parse(ARTICLE_ENDPOINT))
                    code != "0" || envelope.data == null -> {
                        ApiResult.Failure(AppError.Server(code, envelope.message))
                    }

                    else -> result
                }
            }
        }
    }

    override suspend fun loadComments(request: CommentRequest): ApiResult<CommentsEnvelopeDto> {
        require((request.next == null) == (request.page == null))
        require(request.page == null || request.page > 0)
        val url = baseUrl.newBuilder()
            .addPathSegments("v2/article")
            .addPathSegment(request.articleId.raw)
            .addPathSegment("comment")
            .apply {
                if (request.next == null || request.page == null) {
                    addQueryParameter("size", "20")
                    addQueryParameter("platform", "web")
                } else {
                    addQueryParameter("sort", "down")
                    addQueryParameter("next", request.next)
                    addQueryParameter("pn", request.page.toString())
                    addQueryParameter("platform", "h5")
                    addQueryParameter("version", "0")
                }
            }
            .build()
        return when (val result = get(url, COMMENTS_ENDPOINT, CommentsEnvelopeDto.serializer())) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val envelope = result.value
                val code = envelope.errCode.scalarString()
                when {
                    code == null -> ApiResult.Failure(AppError.Parse(COMMENTS_ENDPOINT))
                    code != "0" || envelope.data == null -> ApiResult.Failure(
                        AppError.Server(
                            code = code,
                            message = envelope.errMesg ?: envelope.errMsg ?: envelope.message,
                        ),
                    )

                    else -> result
                }
            }
        }
    }

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

    private companion object {
        val FEED_ENDPOINT = EndpointId("news.feed")
        val ARTICLE_ENDPOINT = EndpointId("news.article")
        val COMMENTS_ENDPOINT = EndpointId("news.comments")
    }
}

internal fun kotlinx.serialization.json.JsonElement?.scalarString(): String? =
    (this as? JsonPrimitive)?.contentOrNull
