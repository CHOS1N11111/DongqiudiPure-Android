package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.FootballRemoteDataSource
import io.github.chos1n11111.dongqiudipure.core.network.dto.*
import io.github.chos1n11111.dongqiudipure.core.testing.FixtureLoader
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FootballDataTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `repository exposes the requested important and default ranking competitions`() {
        val repository = DefaultFootballRepository(FakeFootballRemoteDataSource())

        assertEquals(
            setOf(
                "4", "3", "9", "5", "12", "43",
                "82", "83", "119", "116", "92", "158",
                "6", "14", "61", "225", "18", "262",
                "251", "226", "228",
            ),
            repository.importantCompetitions.mapTo(mutableSetOf()) { it.id.raw },
        )
        assertEquals(
            listOf("4", "3", "9", "5", "12", "43"),
            repository.defaultRankingCompetitions.map { it.id.raw },
        )
    }

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
    fun `match overview uses group minute and merges an assist with its goal`() {
        val overview = json.decodeFromString(
            MatchOverviewDto.serializer(),
            """
            {
              "events": {
                "64": {
                  "minute": "64",
                  "teamAEvents": [
                    {"person": "射手", "score": "1-0", "code": "G"},
                    {"person": "助攻者", "code": "AS"}
                  ]
                }
              }
            }
            """.trimIndent(),
        ).toDomain()

        assertEquals(1, overview.events.size)
        assertEquals("64'", overview.events.single().minuteLabel)
        assertEquals("射手", overview.events.single().primaryName)
        assertEquals("助攻者", overview.events.single().secondaryName)
    }

    @Test
    fun `match overview does not treat period marker as stoppage time`() {
        val overview = json.decodeFromString(
            MatchOverviewDto.serializer(),
            """
            {
              "events": {
                "45+45": {
                  "minute": "45",
                  "neutralEvents": [
                    {"minute_extra": "45", "person": "半场", "score": "0-0", "code": "HT"}
                  ]
                }
              }
            }
            """.trimIndent(),
        ).toDomain()

        assertEquals("45'", overview.events.single().minuteLabel)
    }

    @Test
    fun `match overview treats official empty arrays as unavailable sections`() {
        val overview = json.decodeFromString(
            MatchOverviewDto.serializer(),
            """{"events":[],"statistics":[],"archive":[],"tendencies":null,"highscorepersons":null}""",
        ).toDomain()

        assertTrue(overview.events.isEmpty())
        assertTrue(overview.statistics.isEmpty())
        assertNull(overview.report)
    }

    @Test
    fun `match news mapper keeps article identity comments and image`() {
        val news = MatchNewsEnvelopeDto(
            data = listOf(
                MatchNewsItemDto(
                    id = JsonPrimitive("6271098"),
                    title = "Fixture report",
                    thumb = "https://img1.qunliao.info/fixture.png",
                    commentsTotal = JsonPrimitive("12"),
                ),
            ),
        ).toDomain()

        assertEquals("6271098", news.single().articleId.raw)
        assertEquals(12, news.single().commentCount)
        assertEquals("https://img1.qunliao.info/fixture.png", news.single().thumbnailUrl)
    }

    @Test
    fun `lineup keeps players when an unused substitute has slot coordinates`() {
        val teamA = MatchLineupTeamDto(
            teamId = JsonPrimitive("50000001"),
            teamName = "主队",
            lineups = listOf(
                MatchLineupPlayerDto(
                    personId = JsonPrimitive("50000011"),
                    person = "首发",
                    rate = JsonPrimitive("7.2"),
                    positionX = JsonPrimitive("50"),
                    positionY = JsonPrimitive("20"),
                ),
            ),
            sub = listOf(
                MatchLineupPlayerDto(
                    personId = JsonPrimitive("50000012"),
                    person = "替补",
                    positionX = JsonPrimitive("D1"),
                    positionY = JsonPrimitive("C"),
                ),
            ),
        )
        val lineup = MatchLineupEnvelopeDto(
            persons = MatchLineupTeamsDto(home = teamA, away = teamA.copy(teamName = "客队")),
        ).toDomain()?.actual

        assertEquals(1, lineup?.home?.starters?.size)
        assertEquals(1, lineup?.home?.substitutes?.size)
        assertNull(lineup?.home?.substitutes?.single()?.gridRow)
        assertNull(lineup?.home?.substitutes?.single()?.gridColumn)
    }

    @Test
    fun `standing mapper exposes aggregate cup ties and both match links`() {
        val dto = json.decodeFromString(
            StandingEnvelopeDto.serializer(),
            """
            {
              "template": "team_point_ranking",
              "content": {
                "rounds": [{
                  "template": "team_point_ranking_aggregate",
                  "content": {
                    "name": "淘汰赛附加赛",
                    "data": [{
                      "total": {
                        "team_A_id": "50001447",
                        "team_A_name": "本菲卡",
                        "team_A_logo": "https://fixture.qunliao.info/benfica.png",
                        "team_B_id": "50001755",
                        "team_B_name": "皇家马德里",
                        "fs_A": "1",
                        "fs_B": "3"
                      },
                      "match1": {"match_id": "54373127"},
                      "match2": {"match_id": "54373139"}
                    }]
                  }
                }]
              }
            }
            """.trimIndent(),
        )

        val table = dto.toDomain(CompetitionRef(CompetitionId("6"), "欧冠", null), "25/26")

        assertEquals(1, table.knockoutStages.size)
        assertEquals("淘汰赛附加赛", table.knockoutStages.single().name)
        assertEquals("1 - 3", table.knockoutStages.single().ties.single().scoreLabel)
        assertEquals(
            listOf("54373127", "54373139"),
            table.knockoutStages.single().ties.single().matchIds.map { it.raw },
        )
    }

    @Test
    fun `player matches expose only the requested match user rating`() {
        val dto = json.decodeFromString(
            PlayerMatchesEnvelopeDto.serializer(),
            """
            {
              "matches": [
                {"match_id": "1", "scheme": "dongqiudi:///game/54000001", "dqd_rating": "8.1"},
                {"match_id": "2", "scheme": "dongqiudi:///game/54000002", "dqd_rating": "9.3"}
              ]
            }
            """.trimIndent(),
        )

        assertEquals("9.3", dto.userRatingFor(io.github.chos1n11111.dongqiudipure.core.model.MatchId("54000002")))
    }

    @Test
    fun `repository filters matches to requested competition`() = runBlocking {
        val remote = FakeFootballRemoteDataSource().apply {
            seasonResult = ApiResult.Success(seasonsFixture())
            competitionScheduleResult = ApiResult.Success(
                CompetitionScheduleEnvelopeDto(
                    template = "schedule_round",
                    content = CompetitionScheduleContentDto(
                        matches = listOf(
                            CompetitionScheduleGroupDto(
                                name = "第3轮",
                                data = matchesFixture().list?.map {
                                    it.copy(
                                        competitionName = null,
                                        roundName = null,
                                        gameweek = null,
                                    )
                                },
                            ),
                        ),
                    ),
                ),
            )
        }
        val repository = DefaultFootballRepository(remote)

        val result = repository.loadMatches(
            LocalDate.of(2026, 9, 2),
            CompetitionRef(CompetitionId("4"), "Contract League", null),
        )

        val matches = (result as DataResult.Success).value
        assertEquals(listOf("7001"), matches.map { it.id.raw })
        assertTrue(matches.all { it.competition.id.raw == "4" })
        assertEquals("Contract League", matches.single().competition.name)
        assertEquals("第3轮", matches.single().competition.roundLabel)
    }

    @Test
    fun `repository resolves current season before loading standings`() = runBlocking {
        val remote = FakeFootballRemoteDataSource().apply {
            seasonResult = ApiResult.Success(seasonsFixture())
            standingResult = ApiResult.Success(standingsFixture("standings-success.json"))
        }
        val repository = DefaultFootballRepository(remote)

        val result = repository.loadStandings(CompetitionRef(CompetitionId("4"), "Contract League", null))

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
        lateinit var competitionScheduleResult: ApiResult<CompetitionScheduleEnvelopeDto>
        var requestedSeason: SeasonId? = null

        override suspend fun loadImportantMatches(
            startDate: LocalDate,
        ): ApiResult<MatchListEnvelopeDto> = matchResult

        override suspend fun loadMatchDetail(matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId):
            ApiResult<MatchDetailEnvelopeDto> = error("Not used")

        override suspend fun loadMatchOverview(matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId):
            ApiResult<MatchOverviewDto> = error("Not used")

        override suspend fun loadMatchNews(matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId):
            ApiResult<MatchNewsEnvelopeDto> = ApiResult.Success(MatchNewsEnvelopeDto(data = emptyList()))

        override suspend fun loadMatchLineup(matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId):
            ApiResult<MatchLineupEnvelopeDto> = error("Not used")

        override suspend fun loadMatchAnalysis(matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId):
            ApiResult<MatchAnalysisDto> = error("Not used")

        override suspend fun loadCompetitionCatalog(): ApiResult<DataMenuEnvelopeDto> =
            error("Not used")

        override suspend fun loadSeasons(
            competitionId: CompetitionId,
        ): ApiResult<List<SeasonDto>> = seasonResult

        override suspend fun loadStandings(
            seasonId: SeasonId,
        ): ApiResult<StandingEnvelopeDto> {
            requestedSeason = seasonId
            return standingResult
        }

        override suspend fun loadCompetitionSchedule(
            seasonId: SeasonId,
        ): ApiResult<CompetitionScheduleEnvelopeDto> = competitionScheduleResult

        override suspend fun loadRankingTypes(
            seasonId: SeasonId,
            entity: String,
        ): ApiResult<RankingTypesEnvelopeDto> = error("Not used")

        override suspend fun loadRanking(
            seasonId: SeasonId,
            entity: String,
            metric: String,
        ): ApiResult<RankingDetailEnvelopeDto> = error("Not used")

        override suspend fun loadTeamSample(teamId: TeamId): ApiResult<TeamSampleDto> =
            error("Not used")

        override suspend fun loadTeamDetail(teamId: TeamId): ApiResult<TeamDetailDto> =
            error("Not used")

        override suspend fun loadTeamStatistics(
            teamId: TeamId,
            seasonId: String?,
        ): ApiResult<TeamStatisticDto> = error("Not used")

        override suspend fun loadTeamMembers(teamId: TeamId): ApiResult<TeamMembersEnvelopeDto> =
            error("Not used")

        override suspend fun loadTeamSchedule(
            teamId: TeamId,
            seasonId: String?,
        ): ApiResult<TeamScheduleEnvelopeDto> = error("Not used")

        override suspend fun loadTeamTransfers(
            teamId: TeamId,
            windowId: String?,
        ): ApiResult<TeamTransferEnvelopeDto> = error("Not used")

        override suspend fun loadEntityFeed(
            entityId: String,
            type: String,
        ): ApiResult<EntityFeedEnvelopeDto> = error("Not used")

        override suspend fun loadPlayerDetail(playerId: PlayerId): ApiResult<PlayerDetailDto> =
            error("Not used")

        override suspend fun loadPlayerStatistics(playerId: PlayerId): ApiResult<PlayerStatisticsDto> =
            error("Not used")

        override suspend fun loadPlayerMatches(
            playerId: PlayerId,
            page: Int,
        ): ApiResult<PlayerMatchesEnvelopeDto> = error("Not used")

        override suspend fun loadPlayerHeatMap(
            playerId: PlayerId,
            seasonId: String,
            teamId: TeamId,
        ): ApiResult<PlayerHeatMapDto> = error("Not used")

        override suspend fun loadPlayerShotMap(
            playerId: PlayerId,
            matchId: io.github.chos1n11111.dongqiudipure.core.model.MatchId,
        ): ApiResult<PlayerShotMapDto> = error("Not used")

        override suspend fun loadPlayerAbility(playerId: PlayerId): ApiResult<PlayerAbilityEnvelopeDto> =
            error("Not used")
    }

    private companion object {
        const val FIXTURE_ROOT = "contracts/football/2026-09-02"
    }
}
