package io.github.chos1n11111.dongqiudipure.core.data

import androidx.paging.PagingSource
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.DataResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.SeasonId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.network.ApiResult
import io.github.chos1n11111.dongqiudipure.core.network.EntityFeedRequest
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
    fun `entity feed page preserves upstream order and exposes android cursor`() = runBlocking {
        val remote = FakeFootballRemoteDataSource().apply {
            entityFeedResult = ApiResult.Success(
                EntityFeedEnvelopeDto(
                    code = JsonPrimitive(0),
                    data = FeedResponseDto(
                        articles = listOf(
                            FeedArticleDto(id = JsonPrimitive("102"), title = "Second upstream item"),
                            FeedArticleDto(id = JsonPrimitive("101"), title = "First upstream item"),
                        ),
                        next = "https://api.dongqiudi.com/v3/archive/app/channel/feeds" +
                            "?id=50000513&type=team&size=20&platform=android&version=" +
                            "&page=2&after=1788417307&offset=1",
                    ),
                ),
            )
        }

        val result = EntityFeedPagingSource(remote, entityId = "513", type = "team").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false,
            ),
        ) as PagingSource.LoadResult.Page

        assertEquals(listOf("102", "101"), result.data.map { it.id.raw })
        assertEquals(EntityFeedPageKey("1788417307", 2, 1), result.nextKey)
        assertEquals(EntityFeedRequest(entityId = "513", type = "team"), remote.requestedEntityFeed)
    }

    @Test
    fun `team sample separates world rank from market value`() {
        val profile = TeamSampleDto(
            teamId = JsonPrimitive("50000804"),
            teamName = "拜仁慕尼黑",
            rank = "世界排名第1  总身价10.4亿欧",
            marketValue = JsonPrimitive("1040000000"),
        ).toDomain()

        assertEquals("世界排名第1", profile.rankLabel)
        assertEquals("10.4亿欧", profile.marketValueLabel)
    }

    @Test
    fun `lineup keeps players when an unused substitute has slot coordinates`() {
        val teamA = MatchLineupTeamDto(
            teamId = JsonPrimitive("50000001"),
            teamName = "主队",
            teamMarketValue = "2.21亿欧",
            teamAge = "27.3岁",
            coach = "主教练甲",
            coachLogo = "https://fixture.qunliao.info/coach.jpg",
            coachRole = "主教练",
            lineups = listOf(
                MatchLineupPlayerDto(
                    personId = JsonPrimitive("50000011"),
                    person = "首发",
                    rate = JsonPrimitive("7.2"),
                    isMvp = JsonPrimitive(1),
                    captain = JsonPrimitive(1),
                    positionX = JsonPrimitive("50"),
                    positionY = JsonPrimitive("20"),
                    events = listOf(
                        MatchLineupPlayerEventDto(
                            type = "YC",
                            minute = JsonPrimitive(81),
                            eventPic = "https://fixture.qunliao.info/card.png",
                        ),
                    ),
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
            base = MatchLineupBaseDto(
                weather = "局部有云",
                temperature = "14°C",
                weatherInfo = MatchLineupWeatherInfoDto(altitude = JsonPrimitive("106")),
                field = "维拉公园球场",
                referee = "裁判甲",
            ),
            persons = MatchLineupTeamsDto(home = teamA, away = teamA.copy(teamName = "客队")),
            sideline = MatchSidelineDto(
                home = listOf(MatchSidelinePlayerDto(name = "伤员", reason = "受伤")),
            ),
        ).toDomain()

        assertEquals(1, lineup?.actual?.home?.starters?.size)
        assertTrue(lineup?.actual?.home?.starters?.single()?.isMvp == true)
        assertTrue(lineup?.actual?.home?.starters?.single()?.isCaptain == true)
        assertEquals("https://fixture.qunliao.info/card.png", lineup?.actual?.home?.starters?.single()?.events?.single()?.iconUrl)
        assertEquals(1, lineup?.actual?.home?.substitutes?.size)
        assertNull(lineup?.actual?.home?.substitutes?.single()?.gridRow)
        assertNull(lineup?.actual?.home?.substitutes?.single()?.gridColumn)
        assertEquals("2.21亿欧", lineup?.actual?.home?.marketValueLabel)
        assertEquals("27.3岁", lineup?.actual?.home?.averageAgeLabel)
        assertEquals("主教练甲", lineup?.actual?.home?.coach)
        assertEquals("主教练", lineup?.actual?.home?.coachRole)
        assertEquals("https://fixture.qunliao.info/coach.jpg", lineup?.actual?.home?.coachAvatarUrl)
        assertEquals("维拉公园球场", lineup?.info?.venue)
        assertEquals("裁判甲", lineup?.info?.referee)
        assertEquals("106m", lineup?.info?.altitude)
        assertEquals("伤员", lineup?.actual?.home?.absentees?.single()?.name)
        assertEquals("受伤", lineup?.actual?.home?.absentees?.single()?.reason)
    }

    @Test
    fun `historical squad treats dash shirt number as missing`() {
        val squad = TeamMembersEnvelopeDto(
            code = JsonPrimitive(0),
            data = TeamMembersDataDto(
                list = listOf(
                    TeamMemberGroupDto(
                        title = "前锋",
                        type = "Attacker",
                        data = listOf(
                            TeamMemberDto(
                                personId = JsonPrimitive("230636"),
                                personName = "查普林",
                                shirtnumber = JsonPrimitive("-"),
                                type = "Attacker",
                            ),
                        ),
                    ),
                ),
            ),
        ).toDomain("2025-2026")

        assertNull(squad.groups.single().members.single().shirtNumber)
        assertEquals("2025-2026", squad.selectedSeasonId)
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

    @Test
    fun `player detail keeps original facts characteristics careers and injury duration`() {
        val dto = json.decodeFromString(
            PlayerDetailDto.serializer(),
            """
            {
              "base_info": {"person_id":"50466810","person_name":"萨卡"},
              "base_info_v_1": [{"type":"周薪","value":"30万英镑"}],
              "character_info": {
                "styles":["喜欢内切"],
                "strength":{"strong":["传中"]},
                "weakness":{"weak":["抢断"]}
              },
              "player_career_info": [{
                "team_id":"50000513","team_name":"阿森纳","appearance":"315",
                "goals":"83","assist":"81"
              }],
              "injury_records":{"history":[{
                "injury":"跟腱问题","days":29,"games_missed":7
              }]}
            }
            """.trimIndent(),
        ).toDomain()

        assertEquals("30万英镑", dto.facts.single().value)
        assertEquals(listOf("喜欢内切"), dto.characteristics?.styles)
        assertEquals(315, dto.clubCareer.single().appearances)
        assertEquals(29, dto.injuries.single().durationDays)
    }

    @Test
    fun `player statistics map the four original summary fields`() {
        val dto = json.decodeFromString(
            PlayerStatisticsDto.serializer(),
            """
            {"league":[{
              "id":"1",
              "season":{"season_id":"27502","name":"2026/2027"},
              "team":{"id":"50000513","name":"阿森纳"},
              "base_info":{
                "appearances":"2","starts":"2","avg_appearances_time":"79",
                "goals":"2","assists":"0","substitute_in":"0"
              }
            }],"tabs_default":"league"}
            """.trimIndent(),
        ).toDomain()

        val summary = dto.entries.getValue(dto.defaultScope).single().summary
        assertEquals(listOf("出场/首发", "场均时间", "进球", "助攻"), summary.map { it.label })
        assertEquals("2/2", summary.first().value)
    }

    @Test
    fun `team detail keeps rank history coaches and record leaders`() {
        val dto = json.decodeFromString(
            TeamDetailDto.serializer(),
            """
            {
              "base_info":{"team_id":"50000513","team_name":"阿森纳"},
              "base_info_v_1":[{"type":"成立时间","value":"1886"}],
              "history_rank":{"season":["25/26"],"data":[{"rank":4,"competition_clubs":20}]},
              "history_coach":[{
                "time":"6年","win":10,"draw":2,"loss":1,"win_rate":"76.9",
                "person":{"id":"50002641","name":"阿尔特塔"}
              }],
              "goals_info":[{
                "rank":1,"count":"228球",
                "person":{"id":"511","name":"亨利","nationality":{"name":"法国"}}
              }]
            }
            """.trimIndent(),
        ).toDomain()

        assertEquals(4, dto.rankHistory.single().rank)
        assertEquals("6年", dto.historicalCoaches.single().durationLabel)
        assertEquals("228球", dto.topScorers.single().countLabel)
    }

    @Test
    fun `team statistics and squad preserve original seasons and characteristics`() {
        val statistics = json.decodeFromString(
            TeamStatisticDto.serializer(),
            """
            {
              "season_list":[{"name":"2026/2027","current":true,"url":"https://x?season_id=27502"}],
              "season":{"name":"2026/2027","rank":2},
              "characteristics":{"styles":["控球"],"strength":{"very_strong":["边路进攻"]}},
              "ranking_trend":{"weeks":[{"week":1,"rank":2,"window_start":"2026-08-21"}]}
            }
            """.trimIndent(),
        ).toDomain()
        val squad = json.decodeFromString(
            TeamMembersEnvelopeDto.serializer(),
            """
            {
              "code":0,
              "seasons":[{"name":"2026/2027","current":true,"url":"https://x?season=0&current=1"}],
              "data":{"list":[{
                "title":"前锋","type":"attacker","statistics":["出场","进球"],
                "data":[{"person_id":"50466810","person_name":"萨卡","statistic":[{"出场":"2"},{"进球":"2"}]}]
              }]}
            }
            """.trimIndent(),
        ).toDomain(null)

        assertEquals("2026/2027", statistics.seasonLabel)
        assertEquals(listOf("控球"), statistics.characteristics?.styles)
        assertEquals(2, statistics.rankingTrend.single().rank)
        assertEquals("0", squad.selectedSeasonId)
        assertEquals(listOf("出场", "进球"), squad.groups.single().statisticLabels)
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
        lateinit var entityFeedResult: ApiResult<EntityFeedEnvelopeDto>
        var requestedSeason: SeasonId? = null
        var requestedEntityFeed: EntityFeedRequest? = null

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

        override suspend fun loadTeamMembers(
            teamId: TeamId,
            seasonId: String?,
        ): ApiResult<TeamMembersEnvelopeDto> =
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
            request: EntityFeedRequest,
        ): ApiResult<EntityFeedEnvelopeDto> {
            requestedEntityFeed = request
            return entityFeedResult
        }

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
