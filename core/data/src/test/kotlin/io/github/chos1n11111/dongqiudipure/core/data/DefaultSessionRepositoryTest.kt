package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.AuthRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.AuthorizationToken
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
    fun `failed validation never persists login candidate`() = runTest {
        val remote = FakeAuthRemoteDataSource(
            validationResult = ApiResult.Failure(AppError.AuthenticationRequired),
        )
        val store = MemorySessionStore()
        val repository = repository(remote, store)

        repository.login("fixture-user", "fixture-password")

        assertNull(store.authorization)
        assertEquals(
            SessionState.Anonymous(AppError.AuthenticationRequired),
            repository.state.value,
        )
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

    private fun repository(
        remote: FakeAuthRemoteDataSource,
        store: MemorySessionStore,
    ) = DefaultSessionRepository(
        remote = remote,
        sessionStore = store,
        deviceIdStore = FixedDeviceIdStore(),
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

    override suspend fun login(
        identifier: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthorizationToken> {
        loginDeviceId = deviceId
        return loginResult
    }

    override suspend fun validateSession(
        authorization: AuthorizationToken,
        deviceId: String,
    ): ApiResult<AccountSummary> {
        validationDeviceId = deviceId
        return validationResult
    }
}

private class MemorySessionStore(
    var authorization: String? = null,
) : SessionStore {
    override suspend fun readAuthorization(): String? = authorization

    override suspend fun writeAuthorization(value: String) {
        authorization = value
    }

    override suspend fun clear() {
        authorization = null
    }
}

private class FixedDeviceIdStore : DeviceIdStore {
    override suspend fun getOrCreate(): String = "fixture-device"
}
