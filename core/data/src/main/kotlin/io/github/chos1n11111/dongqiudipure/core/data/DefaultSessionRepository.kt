package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.AuthRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.AuthorizationToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DefaultSessionRepository @Inject internal constructor(
    private val remote: AuthRemoteDataSource,
    private val sessionStore: SessionStore,
    private val deviceIdStore: DeviceIdStore,
) : SessionRepository {
    private val operationMutex = Mutex()
    private val _state = MutableStateFlow<SessionState>(SessionState.Restoring)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    override suspend fun restore() = operationMutex.withLock {
        _state.value = SessionState.Restoring
        val savedAuthorization = storageCall { sessionStore.readAuthorization() }
            .getOrElse {
                _state.value = SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
                return@withLock
            }
        if (savedAuthorization == null) {
            _state.value = SessionState.Anonymous()
            return@withLock
        }

        val result = validate(AuthorizationToken(savedAuthorization))
        when (result) {
            is ApiResult.Success -> _state.value = SessionState.Authenticated(result.value)
            is ApiResult.Failure -> {
                if (result.error.isAuthenticationFailure()) {
                    _state.value = if (storageCall { sessionStore.clear() }.isSuccess) {
                        SessionState.Expired
                    } else {
                        SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
                    }
                } else {
                    // Keep an encrypted session after transient failures so a later retry can recover it.
                    _state.value = SessionState.Anonymous(result.error)
                }
            }
        }
    }

    override suspend fun login(identifier: String, password: String) = operationMutex.withLock {
        if (identifier.isBlank() || password.isBlank()) {
            _state.value = SessionState.Anonymous(
                AppError.Server(code = "40002", message = null),
            )
            return@withLock
        }

        if (storageCall { sessionStore.clear() }.isFailure) {
            _state.value = SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
            return@withLock
        }
        _state.value = SessionState.SubmittingCredentials
        val deviceId = storageCall { deviceIdStore.getOrCreate() }
            .getOrElse {
                _state.value = SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
                return@withLock
            }
        when (val loginResult = remote.login(identifier.trim(), password, deviceId)) {
            is ApiResult.Failure -> _state.value = SessionState.Anonymous(loginResult.error)
            is ApiResult.Success -> {
                _state.value = SessionState.ValidatingSession
                when (val validation = remote.validateSession(loginResult.value, deviceId)) {
                    is ApiResult.Failure -> {
                        storageCall { sessionStore.clear() }
                        _state.value = SessionState.Anonymous(validation.error)
                    }
                    is ApiResult.Success -> {
                        val stored = storageCall {
                            sessionStore.writeAuthorization(loginResult.value.value)
                        }.isSuccess
                        _state.value = if (stored) {
                            SessionState.Authenticated(validation.value)
                        } else {
                            SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
                        }
                    }
                }
            }
        }
    }

    override suspend fun logout() = operationMutex.withLock {
        _state.value = if (storageCall { sessionStore.clear() }.isSuccess) {
            SessionState.Anonymous()
        } else {
            SessionState.Anonymous(AppError.UnsupportedContract(STORAGE_ENDPOINT))
        }
    }

    private suspend fun validate(token: AuthorizationToken): ApiResult<AccountSummary> {
        val deviceId = try {
            deviceIdStore.getOrCreate()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            return ApiResult.Failure(AppError.UnsupportedContract(STORAGE_ENDPOINT))
        }
        return remote.validateSession(token, deviceId)
    }

    private suspend fun <T> storageCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        Result.failure(error)
    }

    private fun AppError.isAuthenticationFailure(): Boolean =
        this == AppError.AuthenticationRequired || this == AppError.SessionExpired

    private companion object {
        val STORAGE_ENDPOINT = EndpointId("auth.storage")
    }
}
