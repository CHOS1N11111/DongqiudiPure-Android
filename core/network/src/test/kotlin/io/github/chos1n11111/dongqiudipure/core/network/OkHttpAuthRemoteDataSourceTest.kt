package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.network.di.NewsNetworkModule
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class OkHttpAuthRemoteDataSourceTest {
    private lateinit var server: MockWebServer
    private lateinit var remote: OkHttpAuthRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        remote = OkHttpAuthRemoteDataSource(
            client = NewsNetworkModule.provideAuthOkHttpClient(),
            json = NewsNetworkModule.provideJson(),
            baseUrl = server.url("/"),
            clientProfile = DqdClientProfile("FixtureClient/1"),
        )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `login posts credentials without authorization and extracts candidate token`() = runBlocking {
        server.enqueue(jsonResponse("""{"errCode":0,"data":{"authorization":"Bearer fixture-token"}}"""))

        val result = remote.login("fixture-user", "fixture-password", FIXTURE_UUID)

        assertEquals("Bearer fixture-token", (result as ApiResult.Success).value.value)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v2/user/login", request.target)
        assertEquals(FIXTURE_UUID, request.headers["UUID"])
        assertEquals("FixtureClient/1", request.headers["User-Agent"])
        assertNull(request.headers["Authorization"])
        assertNull(request.headers["Cookie"])
        assertEquals(
            "username=fixture-user&password=fixture-password",
            request.body?.utf8(),
        )
    }

    @Test
    fun `login maps known credential error without exposing response details`() = runBlocking {
        server.enqueue(jsonResponse("""{"errCode":40003,"errMesg":"fixture rejection"}"""))

        val result = remote.login("fixture-user", "wrong", FIXTURE_UUID)

        assertEquals(
            AppError.Server("40003", null),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `login success without authorization is an unsupported contract`() = runBlocking {
        server.enqueue(jsonResponse("""{"errCode":0,"data":{}}"""))

        val result = remote.login("fixture-user", "fixture-password", FIXTURE_UUID)

        assertEquals(
            AppError.UnsupportedContract(EndpointId("auth.login")),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `session validation carries authorization and parses minimal account`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """{"errCode":0,"data":{"is_login":true,"user":{"uid":"42","nickname":"Fixture"}}}""",
            ),
        )

        val result = remote.validateSession(AuthorizationToken("Bearer fixture-token"), FIXTURE_UUID)

        val account = (result as ApiResult.Success).value
        assertEquals("42", account.id)
        assertEquals("Fixture", account.displayName)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/v2/user/is_login", request.target)
        assertEquals("Bearer fixture-token", request.headers["Authorization"])
        assertEquals(FIXTURE_UUID, request.headers["UUID"])
    }

    @Test
    fun `session validation rejects expired and unauthorized sessions`() = runBlocking {
        server.enqueue(jsonResponse("""{"errCode":0,"data":{"is_login":false}}"""))
        server.enqueue(MockResponse.Builder().code(401).build())

        val explicitExpired = remote.validateSession(AuthorizationToken("one"), FIXTURE_UUID)
        val unauthorized = remote.validateSession(AuthorizationToken("two"), FIXTURE_UUID)

        assertEquals(AppError.SessionExpired, (explicitExpired as ApiResult.Failure).error)
        assertEquals(AppError.AuthenticationRequired, (unauthorized as ApiResult.Failure).error)
    }

    private fun jsonResponse(body: String): MockResponse = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()

    private companion object {
        const val FIXTURE_UUID = "11111111-2222-3333-4444-555555555555"
    }
}
