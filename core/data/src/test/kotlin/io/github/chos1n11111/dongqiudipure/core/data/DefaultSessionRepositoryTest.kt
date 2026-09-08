package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.NetworkKind
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.AuthRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.AuthorizationToken
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSessionRepositoryTest {
    @Test
    fun `login validates candidate before persisting authenticated session`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store)

        repository.login("fixture-user", "fixture-password")

        assertEquals("fixture-token", store.authorization)
        assertEquals(
            SessionState.Authenticated(AccountSummary(id = "42", displayName = "Fixture")),
            repository.state.value,
        )
        assertEquals("fixture-device", remote.loginDeviceId)
        assertEquals("fixture-device", remote.validationDeviceId)
    }

    @Test
    fun `failed or unconfirmed validation never persists login candidate`() = runTest {
        val errors = listOf(
            AppError.AuthenticationRequired,
            AppError.UnsupportedContract(EndpointId("auth.session")),
        )
        errors.forEach { error ->
            val remote = FakeAuthRemoteDataSource(
                validationResult = ApiResult.Failure(error),
            )
            val store = MemorySessionStore()
            val repository = repository(remote, store)

            repository.login("fixture-user", "fixture-password")

            assertNull(store.authorization)
            assertEquals(SessionState.Anonymous(error), repository.state.value)
        }
    }

    @Test
    fun `cold restore stays anonymous when the saved session cannot be confirmed`() = runTest {
        val error = AppError.UnsupportedContract(EndpointId("auth.session"))
        val store = MemorySessionStore(authorization = "saved-token")
        val remote = FakeAuthRemoteDataSource(
            validationResult = ApiResult.Failure(error),
        )
        val repository = repository(remote, store)

        repository.restore()

        assertEquals(SessionState.Anonymous(error), repository.state.value)
        assertEquals("saved-token", store.authorization)
    }

    @Test
    fun `cold restore expires invalid session and logout clears valid session`() = runTest {
        val store = MemorySessionStore(authorization = "saved-token")
        val remote = FakeAuthRemoteDataSource(
            validationResult = ApiResult.Failure(AppError.SessionExpired),
        )
        val repository = repository(remote, store)

        repository.restore()

        assertEquals(SessionState.Expired, repository.state.value)
        assertNull(store.authorization)

        remote.validationResult = ApiResult.Success(AccountSummary(displayName = "Fixture"))
        repository.login("fixture-user", "fixture-password")
        repository.logout()

        assertEquals(SessionState.Anonymous(), repository.state.value)
        assertNull(store.authorization)
    }

    @Test
    fun `foreground checks use the saved token and are rate limited`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")

        repository.refresh()
        assertEquals(1, remote.validationCalls)
        advanceTimeBy(60_000)
        remote.validationResult = ApiResult.Success(AccountSummary(id = "42", displayName = "Updated"))
        repository.refresh()
        repository.refresh()

        assertEquals(2, remote.validationCalls)
        assertEquals("fixture-token", remote.validationToken)
        assertEquals("fixture-device", remote.validationDeviceId)
        assertEquals(SessionState.Authenticated(AccountSummary("42", "Updated")), repository.state.value)
    }

    @Test
    fun `anonymous and expired sessions do not send foreground account requests`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.restore()
        repository.refresh()
        assertEquals(0, remote.validationCalls)

        repository.login("fixture-user", "fixture-password")
        advanceTimeBy(60_000)
        remote.validationResult = ApiResult.Failure(AppError.SessionExpired)
        repository.refresh()
        assertEquals(SessionState.Expired, repository.state.value)
        assertNull(store.authorization)

        advanceTimeBy(60_000)
        repository.refresh()
        assertEquals(2, remote.validationCalls)
    }

    @Test
    fun `transient foreground failures keep the previously validated account`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")
        val authenticated = repository.state.value

        listOf(
            AppError.Network(NetworkKind.Timeout), AppError.RateLimited(null), AppError.Http(503),
        ).forEach { error ->
            advanceTimeBy(60_000)
            remote.validationResult = ApiResult.Failure(error)
            repository.refresh()
            assertEquals(authenticated, repository.state.value)
            assertEquals("fixture-token", store.authorization)
        }
    }

    @Test
    fun `unconfirmed foreground responses clear account state without deleting the saved token`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")
        val error = AppError.UnsupportedContract(EndpointId("auth.session"))
        remote.validationResult = ApiResult.Failure(error)

        advanceTimeBy(60_000)
        repository.refresh()

        assertEquals(SessionState.Anonymous(error), repository.state.value)
        assertEquals("fixture-token", store.authorization)
    }

    @Test
    fun `transient cold restore can recover on a later foreground check`() = runTest {
        val remote = FakeAuthRemoteDataSource(
            validationResult = ApiResult.Failure(AppError.Network(NetworkKind.NoConnection)),
        )
        val store = MemorySessionStore("saved-token")
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.restore()
        remote.validationResult = ApiResult.Success(AccountSummary(id = "42"))

        advanceTimeBy(60_000)
        repository.refresh()

        assertEquals(SessionState.Authenticated(AccountSummary(id = "42")), repository.state.value)
        assertEquals("saved-token", store.authorization)
    }

    @Test
    fun `validation cannot silently change the account associated with a saved token`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")
        remote.validationResult = ApiResult.Success(AccountSummary(id = "99", displayName = "Other"))

        advanceTimeBy(60_000)
        repository.refresh()

        assertEquals(SessionState.Expired, repository.state.value)
        assertNull(store.authorization)
    }

    @Test
    fun `account identity is retained across an unconfirmed check of the same token`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")
        val error = AppError.UnsupportedContract(EndpointId("auth.session"))
        remote.validationResult = ApiResult.Failure(error)
        advanceTimeBy(60_000)
        repository.refresh()
        assertEquals(SessionState.Anonymous(error), repository.state.value)

        remote.validationResult = ApiResult.Success(AccountSummary(id = "99"))
        repository.restore()

        assertEquals(SessionState.Expired, repository.state.value)
        assertNull(store.authorization)
    }

    @Test
    fun `logout does not wait for login and late success cannot restore its candidate`() = runTest {
        val lateLogin = CompletableDeferred<ApiResult<AuthorizationToken>>()
        val remote = FakeAuthRemoteDataSource().apply {
            loginBlock = { withContext(NonCancellable) { lateLogin.await() } }
        }
        val store = MemorySessionStore()
        val repository = repository(remote, store)
        val login = launch { repository.login("fixture-user", "fixture-password") }
        runCurrent()

        repository.logout()
        assertEquals(SessionState.Anonymous(), repository.state.value)
        assertNull(store.authorization)
        lateLogin.complete(ApiResult.Success(AuthorizationToken("late-token")))
        login.join()

        assertEquals(0, remote.validationCalls)
        assertEquals(SessionState.Anonymous(), repository.state.value)
        assertNull(store.authorization)
    }

    @Test
    fun `late restore cannot overwrite a newer login`() = runTest {
        val lateRestore = CompletableDeferred<ApiResult<AccountSummary>>()
        val otherAccount = AccountSummary(id = "99", displayName = "Other")
        val remote = FakeAuthRemoteDataSource().apply {
            loginResult = ApiResult.Success(AuthorizationToken("new-token"))
            validationBlock = { token ->
                if (token.value == "saved-token") {
                    withContext(NonCancellable) { lateRestore.await() }
                } else {
                    ApiResult.Success(otherAccount)
                }
            }
        }
        val store = MemorySessionStore("saved-token")
        val repository = repository(remote, store)
        val restore = launch { repository.restore() }
        runCurrent()

        repository.login("other-user", "fixture-password")
        lateRestore.complete(ApiResult.Success(AccountSummary(id = "42", displayName = "Old")))
        restore.join()

        assertEquals(SessionState.Authenticated(otherAccount), repository.state.value)
        assertEquals("new-token", store.authorization)
    }

    @Test
    fun `late expiry from a previous account cannot log out the new account`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store) { testScheduler.currentTime }
        repository.login("fixture-user", "fixture-password")
        val lateRefresh = CompletableDeferred<ApiResult<AccountSummary>>()
        val otherAccount = AccountSummary(id = "99", displayName = "Other")
        remote.validationBlock = { token ->
            if (token.value == "fixture-token") {
                withContext(NonCancellable) { lateRefresh.await() }
            } else {
                ApiResult.Success(otherAccount)
            }
        }
        advanceTimeBy(60_000)
        val refresh = launch { repository.refresh() }
        runCurrent()

        remote.loginResult = ApiResult.Success(AuthorizationToken("other-token"))
        repository.login("other-user", "fixture-password")
        lateRefresh.complete(ApiResult.Failure(AppError.SessionExpired))
        refresh.join()

        assertEquals(SessionState.Authenticated(otherAccount), repository.state.value)
        assertEquals("other-token", store.authorization)
    }

    @Test
    fun `cancelling a login clears busy state and leaves no saved candidate`() = runTest {
        val gate = CompletableDeferred<ApiResult<AccountSummary>>()
        val remote = FakeAuthRemoteDataSource().apply { validationBlock = { gate.await() } }
        val store = MemorySessionStore()
        val repository = repository(remote, store)
        val login = launch { repository.login("fixture-user", "fixture-password") }
        runCurrent()
        assertEquals(SessionState.ValidatingSession, repository.state.value)

        login.cancelAndJoin()

        assertEquals(SessionState.Anonymous(), repository.state.value)
        assertNull(store.authorization)
    }

    @Test
    fun `startup restore and duplicate refresh do not interrupt an active login`() = runTest {
        val gate = CompletableDeferred<ApiResult<AuthorizationToken>>()
        val remote = FakeAuthRemoteDataSource().apply { loginBlock = { gate.await() } }
        val store = MemorySessionStore()
        val repository = repository(remote, store)
        val login = launch { repository.login("fixture-user", "fixture-password") }
        runCurrent()

        repository.restore()
        repository.refresh()
        assertEquals(SessionState.SubmittingCredentials, repository.state.value)
        gate.complete(ApiResult.Success(AuthorizationToken("fixture-token")))
        login.join()

        assertEquals(1, remote.loginCalls)
        assertEquals(1, remote.validationCalls)
        assertEquals("fixture-token", store.authorization)
    }

    @Test
    fun `failed logout storage cleanup cannot restore the old session in this process`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store)
        repository.login("fixture-user", "fixture-password")
        store.failClear = true

        repository.logout()
        repository.restore()

        assertEquals(
            SessionState.Anonymous(AppError.UnsupportedContract(EndpointId("auth.storage"))),
            repository.state.value,
        )
        assertEquals(1, remote.validationCalls)
        store.failClear = false
        repository.logout()
        assertNull(store.authorization)
    }

    @Test
    fun `malformed stored authorization cannot crash restore or start a request`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = repository(remote, MemorySessionStore("invalid\nvalue"))

        repository.restore()

        assertEquals(
            SessionState.Anonymous(AppError.UnsupportedContract(EndpointId("auth.storage"))),
            repository.state.value,
        )
        assertEquals(0, remote.validationCalls)
    }

    @Test
    fun `a failed account switch does not recover the previous account`() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val store = MemorySessionStore()
        val repository = repository(remote, store)
        repository.login("fixture-user", "fixture-password")
        val error = AppError.Server("40003", null)
        remote.loginResult = ApiResult.Failure(error)

        repository.login("other-user", "fixture-password")

        assertEquals(SessionState.Anonymous(error), repository.state.value)
        assertNull(store.authorization)
        repository.restore()
        assertEquals(SessionState.Anonymous(), repository.state.value)
        assertEquals(1, remote.validationCalls)
    }

    private fun repository(
        remote: FakeAuthRemoteDataSource,
        store: MemorySessionStore,
        clockMillis: () -> Long = { 0L },
    ) = DefaultSessionRepository(
        remote = remote,
        sessionStore = store,
        deviceIdStore = FixedDeviceIdStore(),
        clockMillis = clockMillis,
    )
}

private class FakeAuthRemoteDataSource(
    var loginResult: ApiResult<AuthorizationToken> = ApiResult.Success(
        AuthorizationToken("fixture-token"),
    ),
    var validationResult: ApiResult<AccountSummary> = ApiResult.Success(
        AccountSummary(id = "42", displayName = "Fixture"),
    ),
) : AuthRemoteDataSource {
    var loginDeviceId: String? = null
    var validationDeviceId: String? = null
    var validationToken: String? = null
    var loginCalls = 0
    var validationCalls = 0
    var loginBlock: (suspend () -> ApiResult<AuthorizationToken>)? = null
    var validationBlock: (suspend (AuthorizationToken) -> ApiResult<AccountSummary>)? = null

    override suspend fun login(
        identifier: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthorizationToken> {
        loginDeviceId = deviceId
        loginCalls++
        return loginBlock?.invoke() ?: loginResult
    }

    override suspend fun validateSession(
        authorization: AuthorizationToken,
        deviceId: String,
    ): ApiResult<AccountSummary> {
        validationDeviceId = deviceId
        validationToken = authorization.value
        validationCalls++
        return validationBlock?.invoke(authorization) ?: validationResult
    }
}

private class MemorySessionStore(
    var authorization: String? = null,
) : SessionStore {
    var failClear = false
    override suspend fun readAuthorization(): String? = authorization

    override suspend fun writeAuthorization(value: String) {
        authorization = value
    }

    override suspend fun clear() {
        check(!failClear)
        authorization = null
    }
}

private class FixedDeviceIdStore : DeviceIdStore {
    override suspend fun getOrCreate(): String = "fixture-device"
}
