package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.network.di.ApiBaseUrl
import io.github.chos1n11111.dongqiudipure.core.network.di.AuthClient
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OkHttpAuthRemoteDataSource @Inject constructor(
    @param:AuthClient private val client: OkHttpClient,
    private val json: Json,
    @param:ApiBaseUrl private val baseUrl: HttpUrl,
    private val clientProfile: DqdClientProfile,
) : AuthRemoteDataSource {

    override suspend fun login(
        identifier: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthorizationToken> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.resolve("v2/user/login")!!)
            .headers(deviceHeaders(deviceId))
            .post(
                FormBody.Builder()
                    .add("username", identifier)
                    .add("password", password)
                    .build(),
            )
            .build()

        execute(request, LOGIN_ENDPOINT) { response, root ->
            val businessError = root.businessError()
            if (businessError != null) return@execute ApiResult.Failure(businessError)

            val rawAuthorization = response.header(AUTHORIZATION_HEADER)
                ?.takeIf(String::isNotBlank)
                ?: root.findFirstString(AUTHORIZATION_KEYS)
            val authorization = rawAuthorization?.safeAuthorizationValue()

            if (authorization == null) {
                ApiResult.Failure(AppError.UnsupportedContract(LOGIN_ENDPOINT))
            } else {
                ApiResult.Success(AuthorizationToken(authorization))
            }
        }
    }

    override suspend fun validateSession(
        authorization: AuthorizationToken,
        deviceId: String,
    ): ApiResult<AccountSummary> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.resolve("v2/user/is_login")!!)
            .headers(deviceHeaders(deviceId))
            .header(AUTHORIZATION_HEADER, authorization.value)
            .get()
            .build()

        execute(request, SESSION_ENDPOINT) { _, root ->
            val businessError = root.businessError(sessionValidation = true)
            if (businessError != null) return@execute ApiResult.Failure(businessError)

            when (root.explicitLoginState()) {
                false -> ApiResult.Failure(AppError.SessionExpired)
                true -> ApiResult.Success(root.accountSummary())
                null -> {
                    if (root.hasSuccessCode()) {
                        ApiResult.Success(root.accountSummary())
                    } else {
                        ApiResult.Failure(AppError.UnsupportedContract(SESSION_ENDPOINT))
                    }
                }
            }
        }
    }

    private fun deviceHeaders(deviceId: String) = okhttp3.Headers.Builder()
        .add("Accept", "application/json")
        .add("User-Agent", clientProfile.userAgent)
        .add(UUID_HEADER, deviceId)
        .build()

    private inline fun <T> execute(
        request: Request,
        endpoint: EndpointId,
        parse: (Response, JsonObject) -> ApiResult<T>,
    ): ApiResult<T> {
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body.string()
                if (response.code == 401 || response.code == 403) {
                    return ApiResult.Failure(AppError.AuthenticationRequired)
                }
                if (response.code == 429) {
                    return ApiResult.Failure(AppError.RateLimited(retryAfter = null))
                }

                val root = body.parseObjectOrNull()
                if (!response.isSuccessful) {
                    return ApiResult.Failure(root.httpError(response.code))
                }
                if (root == null) {
                    return ApiResult.Failure(AppError.Parse(endpoint))
                }
                parse(response, root)
            }
        } catch (_: SocketTimeoutException) {
            ApiResult.Failure(AppError.Network(NetworkKind.Timeout))
        } catch (_: SSLException) {
            ApiResult.Failure(AppError.Network(NetworkKind.TlsFailure))
        } catch (_: java.net.UnknownHostException) {
            ApiResult.Failure(AppError.Network(NetworkKind.NoConnection))
        } catch (_: ConnectException) {
            ApiResult.Failure(AppError.Network(NetworkKind.NoConnection))
        } catch (_: IOException) {
            ApiResult.Failure(AppError.Network(NetworkKind.Unknown))
        }
    }

    private fun String.parseObjectOrNull(): JsonObject? = try {
        json.parseToJsonElement(this) as? JsonObject
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private companion object {
        val LOGIN_ENDPOINT = EndpointId("auth.login")
        val SESSION_ENDPOINT = EndpointId("auth.session")
        const val AUTHORIZATION_HEADER = "Authorization"
        const val UUID_HEADER = "UUID"
        val AUTHORIZATION_KEYS = setOf(
            "authorization",
            "access_token",
            "accesstoken",
            "auth_token",
            "authtoken",
            "token",
        )
    }
}

private fun JsonObject?.businessError(sessionValidation: Boolean = false): AppError? {
    this ?: return null
    val code = sequenceOf("errCode", "err_code", "code")
        .mapNotNull { key -> this[key].primitiveContent() }
        .firstOrNull()
        ?: return null
    if (code.lowercase() in setOf("0", "200", "ok", "success")) return null

    return if (sessionValidation) {
        AppError.SessionExpired
    } else {
        // Login response text is not retained because some services reflect account identifiers.
        AppError.Server(code = code, message = null)
    }
}

private fun JsonObject?.httpError(status: Int): AppError {
    val code = sequenceOf("errCode", "err_code", "code")
        .mapNotNull { key -> this?.get(key).primitiveContent() }
        .firstOrNull()
    return if (code != null) {
        AppError.Server(code = code, message = null)
    } else {
        AppError.Http(status)
    }
}

private fun JsonObject.hasSuccessCode(): Boolean {
    val code = sequenceOf("errCode", "err_code", "code")
        .mapNotNull { key -> this[key].primitiveContent() }
        .firstOrNull()
        ?: return false
    return code.lowercase() in setOf("0", "200", "ok", "success")
}

private fun JsonObject.explicitLoginState(): Boolean? {
    val value = findFirstElement(setOf("is_login", "islogin", "logged_in", "loggedin"))
        as? JsonPrimitive
        ?: return null
    value.booleanOrNull?.let { return it }
    return when (value.contentOrNull?.lowercase()) {
        "1", "yes", "true" -> true
        "0", "no", "false" -> false
        else -> null
    }
}

private fun JsonObject.accountSummary(): AccountSummary {
    val accountObject = findFirstObject(setOf("user", "user_info", "userinfo", "member"))
        ?: (this["data"] as? JsonObject)
        ?: this
    return AccountSummary(
        id = accountObject.findFirstString(setOf("user_id", "userid", "uid", "id")),
        displayName = accountObject.findFirstString(
            setOf("username", "user_name", "nickname", "nick_name", "name"),
        ),
    )
}

private fun JsonObject?.findFirstString(keys: Set<String>): String? =
    this?.findFirstElement(keys).primitiveContent()?.takeIf(String::isNotBlank)

private fun JsonObject.findFirstObject(keys: Set<String>): JsonObject? {
    entries.forEach { (key, value) ->
        if (keys.matches(key) && value is JsonObject) return value
    }
    entries.forEach { (_, value) ->
        when (value) {
            is JsonObject -> value.findFirstObject(keys)?.let { return it }
            is kotlinx.serialization.json.JsonArray -> value.forEach { child ->
                if (child is JsonObject) child.findFirstObject(keys)?.let { return it }
            }
            else -> Unit
        }
    }
    return null
}

private fun JsonObject.findFirstElement(keys: Set<String>): JsonElement? {
    entries.forEach { (key, value) ->
        if (keys.matches(key)) return value
    }
    entries.forEach { (_, value) ->
        when (value) {
            is JsonObject -> value.findFirstElement(keys)?.let { return it }
            is kotlinx.serialization.json.JsonArray -> value.forEach { child ->
                if (child is JsonObject) child.findFirstElement(keys)?.let { return it }
            }
            else -> Unit
        }
    }
    return null
}

private fun String.normalizedKey(): String = lowercase().replace("-", "").replace("_", "")

private fun Set<String>.matches(key: String): Boolean =
    any { candidate -> candidate.normalizedKey() == key.normalizedKey() }

private fun JsonElement?.primitiveContent(): String? =
    (this as? JsonPrimitive)?.contentOrNull

private fun String.safeAuthorizationValue(): String? = trim()
    .takeIf { value ->
        value.isNotEmpty() &&
            value.length <= 16_384 &&
            value.none(Char::isISOControl)
    }
