package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.isRetryable
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.AuthRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.AuthorizationToken
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class DefaultSessionRepository internal constructor(
    private val remote: AuthRemoteDataSource,
    private val sessionStore: SessionStore,
    private val deviceIdStore: DeviceIdStore,
    private val clockMillis: () -> Long,
) : SessionRepository {
    @Inject
    internal constructor(
        remote: AuthRemoteDataSource,
        sessionStore: SessionStore,
        deviceIdStore: DeviceIdStore,
    ) : this(remote, sessionStore, deviceIdStore, { TimeUnit.NANOSECONDS.toMillis(System.nanoTime()) })

    private val operationMutex = Mutex()
    private var activeOperation: Operation? = null
    private var lastCheckMillis: Long? = null
    private var validatedAccountId: String? = null
    private var storageBlocked = false
    private val _state = MutableStateFlow<SessionState>(SessionState.Restoring)
    override val state: StateFlow<SessionState> = _state.asStateFlow()

    override suspend fun restore() = checkSavedSession(OperationKind.Restore)

    override suspend fun refresh() = checkSavedSession(OperationKind.Refresh)

    private suspend fun checkSavedSession(kind: OperationKind) = runOperation(kind) { operation ->
        val credentials = operation.update {
            if (storageBlocked) {
                _state.value = SessionState.Anonymous(storageError())
                return@update null
            }
            val token = storageCall { sessionStore.readAuthorization()?.let(::AuthorizationToken) }
                .getOrElse {
                    _state.value = SessionState.Anonymous(storageError())
                    return@update null
                }
            if (token == null) {
                validatedAccountId = null
                _state.value = SessionState.Anonymous()
                return@update null
            }
            val deviceId = readDeviceId() ?: return@update null
            lastCheckMillis = clockMillis()
            token to deviceId
        } ?: return@runOperation

        val result = remote.validateSession(credentials.first, credentials.second)
        operation.update {
            when (result) {
                is ApiResult.Success -> {
                    val currentId = result.value.id
                    if (validatedAccountId != null && currentId != null && validatedAccountId != currentId) {
                        expireSession()
                    } else {
                        validatedAccountId = currentId ?: validatedAccountId
                        _state.value = SessionState.Authenticated(result.value)
                    }
                }
                is ApiResult.Failure -> {
                    if (result.error.isAuthenticationFailure()) {
                        expireSession()
                    } else if (kind == OperationKind.Refresh &&
                        operation.previousState is SessionState.Authenticated &&
                        result.error.isTransientFailure()
                    ) {
                        _state.value = operation.previousState
                    } else {
                        // Unconfirmed sessions stay anonymous, but a later retry may recover the saved token.
                        _state.value = SessionState.Anonymous(result.error)
                    }
                }
            }
        }
    }

    override suspend fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            operationMutex.withLock {
                if (activeOperation == null && _state.value !is SessionState.Authenticated) {
                    _state.value = SessionState.Anonymous(AppError.Server("40002", null))
                }
            }
            return
        }

        runOperation(OperationKind.Login) { operation ->
            val deviceId = operation.update {
                if (!clearStoredSession()) {
                    _state.value = SessionState.Anonymous(storageError())
                    return@update null
                }
                readDeviceId()
            } ?: return@runOperation

            when (val loginResult = remote.login(identifier.trim(), password, deviceId)) {
                is ApiResult.Failure -> operation.update {
                    _state.value = SessionState.Anonymous(loginResult.error)
                }
                is ApiResult.Success -> {
                    operation.update { _state.value = SessionState.ValidatingSession }
                        ?: return@runOperation
                    val validation = remote.validateSession(loginResult.value, deviceId)
                    operation.update {
                        when (validation) {
                            is ApiResult.Failure -> _state.value = SessionState.Anonymous(validation.error)
                            is ApiResult.Success -> {
                                val stored = storageCall {
                                    sessionStore.writeAuthorization(loginResult.value.value)
                                }.isSuccess
                                if (stored) {
                                    lastCheckMillis = clockMillis()
                                    validatedAccountId = validation.value.id
                                    _state.value = SessionState.Authenticated(validation.value)
                                } else {
                                    clearStoredSession()
                                    _state.value = SessionState.Anonymous(storageError())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun logout(): Unit = withContext(NonCancellable) {
        operationMutex.withLock {
            activeOperation?.job?.cancel()
            activeOperation = null
            _state.value = SessionState.Anonymous()
            lastCheckMillis = null
            if (!clearStoredSession()) _state.value = SessionState.Anonymous(storageError())
        }
    }

    private suspend fun runOperation(kind: OperationKind, block: suspend (Operation) -> Unit) = coroutineScope {
        val operation = operationMutex.withLock {
            if (kind != OperationKind.Login && activeOperation != null) return@withLock null
            if (kind == OperationKind.Refresh && !shouldRefresh()) return@withLock null
            activeOperation?.job?.cancel()
            Operation(coroutineContext.job, _state.value).also {
                activeOperation = it
                when (kind) {
                    OperationKind.Login -> {
                        lastCheckMillis = null
                        _state.value = SessionState.SubmittingCredentials
                    }
                    OperationKind.Restore -> _state.value = SessionState.Restoring
                    OperationKind.Refresh -> Unit
                }
            }
        } ?: return@coroutineScope

        try {
            block(operation)
        } finally {
            withContext(NonCancellable) {
                operationMutex.withLock {
                    if (activeOperation === operation) {
                        activeOperation = null
                        if (_state.value.isBusy()) {
                            val cleared = kind != OperationKind.Login || clearStoredSession()
                            _state.value = SessionState.Anonymous(if (cleared) null else storageError())
                        }
                    }
                }
            }
        }
    }

    // Only state/storage mutations hold the lock. Network work can be cancelled or superseded.
    private suspend fun <T> Operation.update(block: suspend () -> T): T? = operationMutex.withLock {
        if (activeOperation !== this) return@withLock null
        block()
    }

    private fun shouldRefresh(): Boolean {
        val current = _state.value
        val eligible = current is SessionState.Authenticated ||
            (current is SessionState.Anonymous && current.error?.isRetryable == true)
        return eligible && !storageBlocked &&
            (lastCheckMillis?.let { clockMillis() - it >= REFRESH_INTERVAL_MILLIS } ?: true)
    }

    private suspend fun readDeviceId(): String? = storageCall { deviceIdStore.getOrCreate() }
        .getOrElse {
            _state.value = SessionState.Anonymous(storageError())
            null
        }

    private suspend fun clearStoredSession(): Boolean {
        validatedAccountId = null
        val cleared = storageCall { sessionStore.clear() }.isSuccess
        storageBlocked = !cleared
        return cleared
    }

    private suspend fun expireSession() {
        _state.value = SessionState.Expired
        if (!clearStoredSession()) _state.value = SessionState.Anonymous(storageError())
    }

    private suspend fun <T> storageCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        Result.failure(error)
    }

    private fun AppError.isAuthenticationFailure(): Boolean =
        this == AppError.AuthenticationRequired || this == AppError.SessionExpired

    private fun AppError.isTransientFailure(): Boolean =
        this is AppError.Network || this is AppError.RateLimited || (this is AppError.Http && status >= 500)

    private fun SessionState.isBusy(): Boolean =
        this == SessionState.Restoring || this == SessionState.SubmittingCredentials ||
            this == SessionState.ValidatingSession

    private fun storageError(): AppError = AppError.UnsupportedContract(STORAGE_ENDPOINT)

    private class Operation(val job: Job, val previousState: SessionState)

    private enum class OperationKind { Restore, Login, Refresh }

    private companion object {
        val STORAGE_ENDPOINT = EndpointId("auth.storage")
        const val REFRESH_INTERVAL_MILLIS = 60_000L
    }
}
