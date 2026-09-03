package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AppError
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.EndpointId
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.network.di.NewsNetworkModule
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OkHttpFootballRemoteDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var remote: OkHttpFootballRemoteDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        remote = OkHttpFootballRemoteDataSource(
            client = NewsNetworkModule.provideOkHttpClient(),
            json = NewsNetworkModule.provideJson(),
            apiBaseUrl = server.url("/"),
            sportDataBaseUrl = server.url("/"),
        )
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `football requests match current anonymous web contracts`() = runBlocking {
        server.enqueue(jsonResponse(fixture("matches-success.json")))
        server.enqueue(jsonResponse(fixture("seasons-success.json")))
        server.enqueue(jsonResponse(fixture("standings-success.json")))
        server.enqueue(jsonResponse("""{"template":"schedule_round","content":{"matches":[]}}"""))

        val matches = remote.loadImportantMatches(LocalDate.of(2026, 9, 2))
        val seasons = remote.loadSeasons(CompetitionId("4"))
        val standings = remote.loadStandings(SeasonId("8001"))
        val schedule = remote.loadCompetitionSchedule(SeasonId("8001"))

        assertEquals(2, (matches as ApiResult.Success).value.list?.size)
        assertEquals("8001", (seasons as ApiResult.Success).value.first().seasonId.scalarString())
        assertEquals("team_point_ranking", (standings as ApiResult.Success).value.template)
        assertEquals("schedule_round", (schedule as ApiResult.Success).value.template)
        assertEquals(
            "/data/tab/new/important?start=2026-09-02%2016%3A00%3A00&init=1&platform=www&version=576",
            server.takeRequest().target,
        )
        assertEquals(
            "/soccer/biz/data/seasons?competition_id=4&app=dqd&platform=miniprogram&version=830&lang=zh-cn",
            server.takeRequest().target,
        )
        assertEquals(
            "/soccer/biz/data/standing?season_id=8001&app=dqd&platform=miniprogram&version=830&lang=zh-cn",
            server.takeRequest().target,
        )
        assertEquals(
            "/soccer/biz/data/schedule?season_id=8001&round_all=1&app=dqd&platform=miniprogram&version=830&lang=zh-cn",
            server.takeRequest().target,
        )
    }

    @Test
    fun `malformed standings maps to standings parse error`() = runBlocking {
        server.enqueue(jsonResponse("{"))

        val result = remote.loadStandings(SeasonId("8001"))

        assertEquals(
            AppError.Parse(EndpointId("football.standings")),
            (result as ApiResult.Failure).error,
        )
    }

    @Test
    fun `match related news uses the endpoint exposed by the official detail page`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """{"code":0,"message":"","data":[{"id":"6271098","title":"Fixture report"}]}""",
            ),
        )

        val result = remote.loadMatchNews(io.github.chos1n11111.dongqiudipure.core.model.MatchId("54470956"))

        assertEquals("6271098", (result as ApiResult.Success).value.data?.single()?.id.scalarString())
        assertEquals("/data/news/match/54470956", server.takeRequest().target)
    }

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private fun jsonResponse(body: String): MockResponse = MockResponse.Builder()
        .code(200)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build()

    private companion object {
        const val FIXTURE_ROOT = "contracts/football/2026-09-02"
    }
}
