package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchListEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.SeasonDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FootballDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `match mapper converts UTC kickoff without inventing scores`() {
        val dto = matchesFixture().list!!.first()

        val result = dto.toDomain(ZoneId.of("Asia/Shanghai"))

        assertEquals(LocalDate.of(2026, 9, 2), result.date)
        assertEquals("20:00", (result.match.status as MatchStatus.NotStarted).kickoffLabel)
        assertNull(result.match.homeScore)
        assertNull(result.match.awayScore)
        assertEquals("第3轮", result.match.competition.roundLabel)
        assertEquals("https://fixture.qunliao.info/teams/7101.png", result.match.home.crestUrl)
    }

    @Test
    fun `standing mapper preserves nullable fields crests and zones`() {
        val competition = CompetitionRef(CompetitionId("4"), "Contract League", null)
        val table = standingsFixture("standings-success.json").toDomain(competition, "26/27")

        assertEquals(3, table.rows.size)
        assertEquals(4, table.rows[0].goalDifference)
        assertEquals(StandingZone.ChampionsLeague, table.rows[0].zone)
        assertNull(table.rows[1].zone)
        assertEquals(StandingZone.Relegation, table.rows[2].zone)
        assertEquals("https://fixture.qunliao.info/teams/7102.png", table.rows[1].team.crestUrl)
    }

    @Test
    fun `repository filters matches to requested six competitions`() = runBlocking {
        val remote = FakeFootballRemoteDataSource().apply {
            matchResult = ApiResult.Success(matchesFixture())
        }
        val repository = DefaultFootballRepository(remote)

        val result = repository.loadMatches(LocalDate.of(2026, 9, 2))

        val matches = (result as DataResult.Success).value
        assertEquals(listOf("7001"), matches.map { it.id.raw })
        assertTrue(matches.all { it.competition.id.raw in setOf("4", "3", "9", "5", "12", "43") })
    }

    @Test
    fun `repository resolves current season before loading standings`() = runBlocking {
        val remote = FakeFootballRemoteDataSource().apply {
            seasonResult = ApiResult.Success(seasonsFixture())
            standingResult = ApiResult.Success(standingsFixture("standings-success.json"))
        }
        val repository = DefaultFootballRepository(remote)

        val result = repository.loadStandings(CompetitionId("4"))

        val table = (result as DataResult.Success).value!!
        assertEquals("26/27", table.seasonLabel)
        assertEquals(SeasonId("8001"), remote.requestedSeason)
    }

    private fun matchesFixture(): MatchListEnvelopeDto = json.decodeFromString(
        MatchListEnvelopeDto.serializer(),
        fixture("matches-success.json"),
    )

    private fun seasonsFixture(): List<SeasonDto> = json.decodeFromString(
        fixture("seasons-success.json"),
    )

    private fun standingsFixture(name: String): StandingEnvelopeDto = json.decodeFromString(
        StandingEnvelopeDto.serializer(),
        fixture(name),
    )

    private fun fixture(name: String): String = FixtureLoader.read(
        path = "$FIXTURE_ROOT/$name",
        classLoader = requireNotNull(javaClass.classLoader),
    )

    private class FakeFootballRemoteDataSource : FootballRemoteDataSource {
        lateinit var matchResult: ApiResult<MatchListEnvelopeDto>
        lateinit var seasonResult: ApiResult<List<SeasonDto>>
        lateinit var standingResult: ApiResult<StandingEnvelopeDto>
        var requestedSeason: SeasonId? = null

        override suspend fun loadImportantMatches(): ApiResult<MatchListEnvelopeDto> = matchResult

        override suspend fun loadSeasons(
            competitionId: CompetitionId,
        ): ApiResult<List<SeasonDto>> = seasonResult

        override suspend fun loadStandings(
            seasonId: SeasonId,
        ): ApiResult<StandingEnvelopeDto> {
            requestedSeason = seasonId
            return standingResult
        }
    }

    private companion object {
        const val FIXTURE_ROOT = "contracts/football/2026-09-02"
    }
}
