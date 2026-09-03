package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.FormResult
import io.github.chos1n11111.dongqiudipure.core.model.FootballCharacteristics
import io.github.chos1n11111.dongqiudipure.core.model.HeatPoint
import io.github.chos1n11111.dongqiudipure.core.model.HistoricalCoach
import io.github.chos1n11111.dongqiudipure.core.model.Absentee
import io.github.chos1n11111.dongqiudipure.core.model.AnalysisMatch
import io.github.chos1n11111.dongqiudipure.core.model.ArticleId
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayer
import io.github.chos1n11111.dongqiudipure.core.model.LineupPlayerEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchAnalysis
import io.github.chos1n11111.dongqiudipure.core.model.MatchArticle
import io.github.chos1n11111.dongqiudipure.core.model.MatchEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchEventKind
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchInfo
import io.github.chos1n11111.dongqiudipure.core.model.MatchListEvent
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineup
import io.github.chos1n11111.dongqiudipure.core.model.MatchLineupBundle
import io.github.chos1n11111.dongqiudipure.core.model.MatchMomentumPoint
import io.github.chos1n11111.dongqiudipure.core.model.MatchOverview
import io.github.chos1n11111.dongqiudipure.core.model.MatchRating
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.MarketValuePoint
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionMatchStage
import io.github.chos1n11111.dongqiudipure.core.model.KnockoutStage
import io.github.chos1n11111.dongqiudipure.core.model.KnockoutTie
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHonor
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerInjury
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHeatMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfileFact
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerCareerSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPage
import io.github.chos1n11111.dongqiudipure.core.model.PlayerMatchPerformance
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShot
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShotMap
import io.github.chos1n11111.dongqiudipure.core.model.PlayerShotSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticEntry
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticsData
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatisticScope
import io.github.chos1n11111.dongqiudipure.core.model.PlayerStatSection
import io.github.chos1n11111.dongqiudipure.core.model.PlayerTransfer
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingRow
import io.github.chos1n11111.dongqiudipure.core.model.SeasonOption
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StandingGroup
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamFact
import io.github.chos1n11111.dongqiudipure.core.model.TeamHonor
import io.github.chos1n11111.dongqiudipure.core.model.TeamKeyPlayer
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatisticCategory
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamType
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.TeamMemberGroupKind
import io.github.chos1n11111.dongqiudipure.core.model.TeamRankHistoryPoint
import io.github.chos1n11111.dongqiudipure.core.model.TeamRankingTrendPoint
import io.github.chos1n11111.dongqiudipure.core.model.TeamRankingTrendMatch
import io.github.chos1n11111.dongqiudipure.core.model.TeamRecordEntry
import io.github.chos1n11111.dongqiudipure.core.model.TeamScheduleData
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadData
import io.github.chos1n11111.dongqiudipure.core.model.TeamSquadGroup
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferData
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferEntry
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferGroup
import io.github.chos1n11111.dongqiudipure.core.model.TeamTransferWindow
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.model.StatItem
import io.github.chos1n11111.dongqiudipure.core.model.TeamLineup
import io.github.chos1n11111.dongqiudipure.core.network.dto.DataMenuEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.FootballCharacteristicsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CompetitionScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchAnalysisDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchAnalysisMatchDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchArchiveDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchEventDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchEventMinuteDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchHighScorePersonsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupPlayerDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupTeamDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchMediaDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchNewsEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchNewsItemDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchOverviewDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchTendenciesDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerAbilityEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerCareerDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerCareerSummaryDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerHeatMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerMatchesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerShotMapDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerTransferDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingTypesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingDescriptionDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingRowDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamMembersEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamSampleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamStatisticDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamRecordLeaderDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamTransferEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamTransferTeamDto
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal data class DatedMatch(
    val date: LocalDate,
    val kickoff: Instant,
    val match: MatchSummary,
)

internal fun MatchDto.toDomain(
    zoneId: ZoneId,
    fallbackCompetition: CompetitionRef? = null,
    fallbackRoundLabel: String? = null,
): DatedMatch {
    val kickoffUtc = try {
        LocalDateTime.parse(startPlay.requiredFootball(), MATCH_DATE_TIME)
    } catch (_: DateTimeParseException) {
        throw ContractViolation()
    }
    val kickoff = kickoffUtc.toInstant(ZoneOffset.UTC)
    val localKickoff = kickoff.atZone(zoneId)
    val rawStatus = status.requiredFootball()
    val mappedStatus = when (rawStatus.lowercase()) {
        "fixture", "notstarted", "not_started" -> MatchStatus.NotStarted(
            localKickoff.format(KICKOFF_TIME),
        )
        "playing", "live" -> MatchStatus.Live(liveMinuteLabel())
        "halftime", "half_time" -> MatchStatus.HalfTime
        "played", "finished" -> MatchStatus.Finished
        "postponed" -> MatchStatus.Postponed
        "cancelled", "canceled" -> MatchStatus.Cancelled
        else -> MatchStatus.Unknown(rawStatus)
    }
    val resolvedCompetitionId = competitionId.scalarFootball().displayable()?.normalizedDqdId()
        ?: fallbackCompetition?.id?.raw
        ?: throw ContractViolation()
    val competition = CompetitionRef(
        id = CompetitionId(resolvedCompetitionId),
        name = competitionName.displayable()
            ?: fallbackCompetition?.name.requiredFootball(),
        roundLabel = gameweek?.trim()?.takeIf(String::isNotEmpty)?.let { "第${it}轮" }
            ?: roundName.displayable()
            ?: fallbackRoundLabel.displayable(),
        logoUrl = fallbackCompetition?.logoUrl,
    )
    return DatedMatch(
        date = localKickoff.toLocalDate(),
        kickoff = kickoff,
        match = MatchSummary(
            id = MatchId(matchId.scalarFootball().requiredFootball()),
            competition = competition,
            home = TeamRef(
                id = TeamId(homeTeamId.scalarFootball().requiredFootball()),
                name = homeTeamName.requiredFootball(),
                crestUrl = safeFootballMediaUrl(homeTeamLogo),
            ),
            away = TeamRef(
                id = TeamId(awayTeamId.scalarFootball().requiredFootball()),
                name = awayTeamName.requiredFootball(),
                crestUrl = safeFootballMediaUrl(awayTeamLogo),
            ),
            homeScore = homeScore.optionalFootballInt(),
            awayScore = awayScore.optionalFootballInt(),
            status = mappedStatus,
            homeHalfScore = homeHalfScore.optionalFootballInt(),
            awayHalfScore = awayHalfScore.optionalFootballInt(),
            homePenaltyScore = homePenaltyScore.optionalFootballInt(),
            awayPenaltyScore = awayPenaltyScore.optionalFootballInt(),
            homeAggregateScore = homeAggregateScore.optionalFootballInt(),
            awayAggregateScore = awayAggregateScore.optionalFootballInt(),
            homeRank = homeRank.scalarFootball().displayable(),
            awayRank = awayRank.scalarFootball().displayable(),
            homeRedCards = homeRedCards.optionalFootballInt(),
            awayRedCards = awayRedCards.optionalFootballInt(),
            homeYellowCards = homeYellowCards.optionalFootballInt(),
            awayYellowCards = awayYellowCards.optionalFootballInt(),
            homeCorners = homeCorners.optionalFootballInt(),
            awayCorners = awayCorners.optionalFootballInt(),
            liveLabel = liveTag.displayable() ?: tvList.displayable(),
            tipsCount = tipsCount.optionalFootballInt(),
            kickoffLabel = localKickoff.format(KICKOFF_TIME),
            dateLabel = localKickoff.format(MATCH_DATE_LABEL),
            matchInfoLabel = matchTitle.displayable(),
            homeEvents = homeEvents.orEmpty().mapNotNull { event ->
                event.title.displayable()?.let { MatchListEvent(it, event.code.displayable()) }
            },
            awayEvents = awayEvents.orEmpty().mapNotNull { event ->
                event.title.displayable()?.let { MatchListEvent(it, event.code.displayable()) }
            },
        ),
    )
}

internal fun CompetitionScheduleEnvelopeDto.toDomain(
    competition: CompetitionRef,
    zoneId: ZoneId,
): List<DatedMatch> {
    if (template?.startsWith("schedule_") != true) throw ContractViolation()
    val groups = content?.matches ?: throw ContractViolation()
    return groups
        .flatMap { group ->
            val roundLabel = group.name.displayable()
            (group.data ?: throw ContractViolation()).map { match ->
                match.toDomain(zoneId, competition, roundLabel)
            }
        }
        .distinctBy { it.match.id }
        .sortedBy { it.kickoff }
}

internal fun MatchOverviewDto.toDomain(): MatchOverview {
    val eventGroups = events.decodeOverviewValue<Map<String, MatchEventMinuteDto>>().orEmpty()
    val statisticData = statistics.decodeOverviewValue<MatchStatisticsDto>()
    val archiveData = archive.decodeOverviewValue<MatchArchiveDto>()
    val tendencyData = tendencies.decodeOverviewValue<MatchTendenciesDto>()
    val highScores = highscorepersons.decodeOverviewValue<MatchHighScorePersonsDto>()
    val mappedEvents = eventGroups
        .toList()
        .sortedBy { (key, value) -> value.minute?.toIntOrNull() ?: key.substringBefore('+').toIntOrNull() ?: 0 }
        .flatMap { (_, group) ->
            mapEventSide(group.teamAEvents.orEmpty(), isHome = true, fallbackMinute = group.minute) +
                mapEventSide(group.teamBEvents.orEmpty(), isHome = false, fallbackMinute = group.minute) +
                mapEventSide(group.neutralEvents.orEmpty(), isHome = true, fallbackMinute = group.minute)
        }
    val mappedStats = statisticData?.list.orEmpty().mapIndexedNotNull { index, item ->
        val name = item.type.displayable() ?: return@mapIndexedNotNull null
        val homeValue = item.home?.value.scalarFootball().displayable()
        val awayValue = item.away?.value.scalarFootball().displayable()
        StatItem(
            id = item.id.displayable() ?: name,
            name = name,
            homeValue = homeValue?.let { if (name.contains("控球") && !it.endsWith('%')) "$it%" else it },
            awayValue = awayValue?.let { if (name.contains("控球") && !it.endsWith('%')) "$it%" else it },
            homeFraction = item.home?.per.scalarFootball()?.toFloatOrNull(),
            awayFraction = item.away?.per.scalarFootball()?.toFloatOrNull(),
            displayOrder = index,
        )
    }
    val homeTeam = statisticData?.home?.toTeamRef()
    val awayTeam = statisticData?.away?.toTeamRef()
    val ratings = listOfNotNull(
        highScores?.home?.toRating(homeTeam),
        highScores?.away?.toRating(awayTeam),
    )
    return MatchOverview(
        events = mappedEvents,
        statistics = mappedStats,
        report = archiveData?.let { item ->
            val id = item.scheme.articleIdFromScheme() ?: return@let null
            val title = item.title.displayable() ?: return@let null
            MatchArticle(
                articleId = ArticleId(id),
                title = title,
                thumbnailUrl = safeFootballMediaUrl(item.thumb),
                commentCount = item.commentsTotal.optionalFootballInt(),
            )
        },
        highlights = gifCollection.orEmpty().mapNotNull(MatchMediaDto::toDomain),
        relatedNews = emptyList(),
        momentum = tendencyData?.data.orEmpty().mapNotNull { point ->
            val minute = point.x.optionalFootballInt() ?: return@mapNotNull null
            val value = point.y.scalarFootball()?.toFloatOrNull() ?: return@mapNotNull null
            MatchMomentumPoint(minute, value)
        },
        topRatings = ratings,
    )
}

internal fun MatchNewsEnvelopeDto.toDomain(): List<MatchArticle> =
    data.orEmpty().mapNotNull(MatchNewsItemDto::toDomain).distinctBy { it.articleId }

internal fun MatchLineupEnvelopeDto.toDomain(): MatchLineupBundle? {
    val info = MatchInfo(
        venue = base?.field.displayable(),
        referee = base?.referee.displayable()?.takeUnless { it == "暂无信息" },
        weather = base?.weather.displayable(),
        temperature = base?.temperature.displayable(),
        attendance = base?.attendance.displayable(),
        altitude = base?.weatherInfo?.altitude.scalarFootball().displayable()?.let { altitude ->
            if (altitude.endsWith("m", ignoreCase = true)) altitude else "${altitude}m"
        },
    )
    val actual = persons.toDomainLineup(sideline)
    val forecast = forecasts.toDomainLineup(sideline)
    return MatchLineupBundle(actual = actual, forecast = forecast, info = info)
        .takeIf { actual != null || forecast != null || info.hasContent() }
}

internal fun MatchAnalysisDto.toDomain(): MatchAnalysis = MatchAnalysis(
    headToHeadTitle = battle_history?.name.displayable(),
    headToHead = battle_history?.list.orEmpty().mapNotNull(MatchAnalysisMatchDto::toDomain),
    recentTitle = recent_record?.name.displayable(),
    homeRecent = recent_record?.home.orEmpty().mapNotNull(MatchAnalysisMatchDto::toDomain),
    awayRecent = recent_record?.away.orEmpty().mapNotNull(MatchAnalysisMatchDto::toDomain),
    futureTitle = feature_matches?.name.displayable(),
    homeFuture = feature_matches?.home.orEmpty().mapNotNull(MatchAnalysisMatchDto::toDomain),
    awayFuture = feature_matches?.away.orEmpty().mapNotNull(MatchAnalysisMatchDto::toDomain),
    homeAbsentees = sideline?.home.orEmpty().mapNotNull { it.toAbsentee() },
    awayAbsentees = sideline?.away.orEmpty().mapNotNull { it.toAbsentee() },
)

private fun mapEventSide(
    events: List<MatchEventDto>,
    isHome: Boolean,
    fallbackMinute: String?,
): List<MatchEvent> = buildList {
    val consumed = mutableSetOf<Int>()
    events.forEachIndexed { index, event ->
        if (index in consumed) return@forEachIndexed
        val code = event.code?.uppercase().orEmpty()
        if (code == "AS" || code == "SO") return@forEachIndexed
        val companionCode = when (code) {
            "G", "PG", "PSG", "OG" -> "AS"
            "SI" -> "SO"
            else -> null
        }
        val companionIndex = companionCode?.let { expected ->
            events.indices.firstOrNull { it !in consumed && it != index && events[it].code?.uppercase() == expected }
        }
        companionIndex?.let(consumed::add)
        val primary = event.person.displayable() ?: event.reason.displayable()
            ?: event.code.displayable() ?: return@forEachIndexed
        add(
            MatchEvent(
                minuteLabel = event.minuteLabel(fallbackMinute),
                kind = code.toEventKind(),
                primaryName = primary,
                secondaryName = companionIndex?.let { events[it].person.displayable() }
                    ?: event.reason.displayable(),
                scoreAfter = event.score.displayable(),
                isHome = isHome,
            ),
        )
    }
}

private fun MatchEventDto.minuteLabel(fallbackMinute: String?): String {
    val minute = this.minute.displayable() ?: fallbackMinute.displayable() ?: "–"
    if (code?.uppercase() == "HT" || code?.uppercase() == "FT") return "$minute'"
    val extra = minuteExtra.displayable()?.takeUnless { it == "0" }
    return if (extra == null) "$minute'" else "$minute+$extra'"
}

private fun String.toEventKind(): MatchEventKind = when (this) {
    "G" -> MatchEventKind.Goal
    "OG" -> MatchEventKind.OwnGoal
    "PG", "PSG" -> MatchEventKind.PenaltyGoal
    "YC" -> MatchEventKind.YellowCard
    "RC" -> MatchEventKind.RedCard
    "Y2C", "SY" -> MatchEventKind.SecondYellow
    "SI" -> MatchEventKind.Substitution
    "VAR" -> MatchEventKind.VarReview
    else -> MatchEventKind.Unknown(ifEmpty { "事件" })
}

private fun MatchMediaDto.toDomain(): MatchArticle? {
    val rawId = id.scalarFootball().displayable() ?: return null
    val safeTitle = title.displayable() ?: return null
    return MatchArticle(
        articleId = ArticleId(rawId),
        title = safeTitle,
        thumbnailUrl = safeFootballMediaUrl(thumb),
        commentCount = comments_total.optionalFootballInt(),
        minuteLabel = time.scalarFootball().displayable()?.let { "$it'" },
        scoreLabel = score.displayable(),
    )
}

private fun MatchNewsItemDto.toDomain(): MatchArticle? {
    val rawId = id.scalarFootball().displayable()
        ?: scheme.articleIdFromScheme()
        ?: return null
    val safeTitle = title.displayable() ?: return null
    return MatchArticle(
        articleId = ArticleId(rawId),
        title = safeTitle,
        thumbnailUrl = safeFootballMediaUrl(thumb ?: cover.scalarFootball()),
        commentCount = commentsTotal.optionalFootballInt(),
    )
}

private fun MatchLineupPlayerDto.toRating(team: TeamRef?): MatchRating? {
    val safeTeam = team ?: return null
    val id = personId.scalarFootball().displayable() ?: return null
    val name = person.displayable() ?: return null
    val rating = rate.scalarFootball().displayable()?.takeUnless { it == "0" } ?: return null
    return MatchRating(
        player = PlayerRef(PlayerId(id), name, safeFootballMediaUrl(logo)),
        team = safeTeam,
        ratingLabel = rating,
        isMvp = isMvp.optionalFootballInt() == 1,
    )
}

private fun io.github.chos1n11111.dongqiudipure.core.network.dto.MatchLineupTeamsDto?.toDomainLineup(
    sideline: io.github.chos1n11111.dongqiudipure.core.network.dto.MatchSidelineDto?,
): MatchLineup? {
    val teams = this ?: return null
    val home = teams.home?.toDomainTeamLineup(sideline?.home.orEmpty()) ?: return null
    val away = teams.away?.toDomainTeamLineup(sideline?.away.orEmpty()) ?: return null
    val hasPlayers = home.starters.isNotEmpty() || away.starters.isNotEmpty() ||
        home.substitutes.isNotEmpty() || away.substitutes.isNotEmpty()
    return MatchLineup(home, away).takeIf { hasPlayers }
}

private fun MatchLineupTeamDto.toDomainTeamLineup(
    sidelined: List<io.github.chos1n11111.dongqiudipure.core.network.dto.MatchSidelinePlayerDto>,
): TeamLineup? {
    val id = teamId.scalarFootball().displayable() ?: return null
    val name = teamName.displayable() ?: return null
    return TeamLineup(
        team = TeamRef(TeamId(id), name, safeFootballMediaUrl(teamLogo)),
        formation = formation.displayable(),
        starters = lineups.orEmpty().mapNotNull(MatchLineupPlayerDto::toDomainPlayer),
        substitutes = sub.orEmpty().mapNotNull(MatchLineupPlayerDto::toDomainPlayer),
        coach = coach.displayable(),
        absentees = sidelined.mapNotNull { item ->
            item.toAbsentee()
        },
        coachRole = coachRole.displayable(),
        coachAvatarUrl = safeFootballMediaUrl(coachLogo),
        marketValueLabel = teamMarketValue.displayable(),
        averageAgeLabel = teamAge.displayable(),
    )
}

private fun MatchLineupPlayerDto.toDomainPlayer(): LineupPlayer? {
    val id = personId.scalarFootball().displayable() ?: return null
    val name = person.displayable() ?: return null
    return LineupPlayer(
        id = PlayerId(id),
        name = name,
        shirtNumber = shirtnumber.optionalFootballInt(),
        position = position.toPlayerPosition(),
        gridRow = positionY.scalarFootball()?.toIntOrNull(),
        gridColumn = positionX.scalarFootball()?.toIntOrNull(),
        avatarUrl = safeFootballMediaUrl(logo),
        ratingLabel = rate.scalarFootball().displayable()?.takeUnless { it == "0" },
        isMvp = isMvp.optionalFootballInt() == 1,
        nationality = nationalityName.displayable(),
        events = events.orEmpty().mapNotNull { event ->
            val type = event.type.displayable() ?: return@mapNotNull null
            val minute = event.minute.scalarFootball().displayable()
            val extra = event.minuteExtra.scalarFootball().displayable()?.takeUnless { it == "0" }
            LineupPlayerEvent(
                type = type,
                minuteLabel = minute?.let {
                    if (extra == null) "$it'" else "$it+$extra'"
                },
            )
        },
    )
}

private fun MatchAnalysisMatchDto.toDomain(): AnalysisMatch? {
    val resolvedHome = homeName.displayable() ?: home.displayable() ?: return null
    val resolvedAway = awayName.displayable() ?: away.displayable() ?: return null
    val matchId = match_href?.let { MATCH_ID_PATTERN.find(it)?.groupValues?.getOrNull(1) }
        ?.let(::MatchId)
    val dateLabel = start_time.displayable()
        ?: listOfNotNull(year.displayable(), date.displayable()).joinToString("-")
            .takeIf(String::isNotEmpty)
    return AnalysisMatch(
        matchId = matchId,
        dateLabel = dateLabel,
        competitionName = competition.displayable() ?: competition_name.displayable(),
        homeTeamId = homeHref.teamIdFromScheme()?.let(::TeamId),
        homeName = resolvedHome,
        homeLogoUrl = safeFootballMediaUrl(homeLogo),
        awayTeamId = awayHref.teamIdFromScheme()?.let(::TeamId),
        awayName = resolvedAway,
        awayLogoUrl = safeFootballMediaUrl(awayLogo),
        scoreLabel = score.displayable(),
    )
}

private fun io.github.chos1n11111.dongqiudipure.core.network.dto.MatchSidelinePlayerDto.toAbsentee(): Absentee? {
    val name = person.displayable() ?: personName.displayable() ?: return null
    return Absentee(name, reason.displayable() ?: injury.displayable())
}

private fun io.github.chos1n11111.dongqiudipure.core.network.dto.MatchStatisticTeamDto.toTeamRef(): TeamRef? {
    val rawId = id.scalarFootball().displayable() ?: return null
    val safeName = name.displayable() ?: return null
    return TeamRef(TeamId(rawId), safeName, safeFootballMediaUrl(logo))
}

private fun MatchInfo.hasContent(): Boolean =
    venue != null || referee != null || weather != null || temperature != null || attendance != null ||
        altitude != null

private inline fun <reified T> JsonElement?.decodeOverviewValue(): T? = when (this) {
    null, JsonNull -> null
    is JsonArray -> if (isEmpty()) null else throw ContractViolation()
    else -> runCatching { FOOTBALL_JSON.decodeFromJsonElement<T>(this) }
        .getOrElse { throw ContractViolation() }
}

private fun String?.articleIdFromScheme(): String? =
    this?.let { ARTICLE_ID_PATTERN.find(it)?.groupValues?.getOrNull(1) }

private fun String?.teamIdFromScheme(): String? =
    this?.let { TEAM_ID_PATTERN.find(it)?.groupValues?.getOrNull(1) }

internal fun DataMenuEnvelopeDto.toDomain(): List<CompetitionCatalogGroup> {
    val football = data?.list?.firstOrNull { it.title == "足球" } ?: throw ContractViolation()
    val seen = mutableSetOf<String>()
    return football.data.orEmpty().mapNotNull { group ->
        val groupName = group.title.displayable() ?: return@mapNotNull null
        val competitions = group.data.orEmpty().mapNotNull competition@{ item ->
            val rawId = item.competitionId.scalarFootball().displayable()
                ?: return@competition null
            val id = rawId.normalizedDqdId()
            if (!seen.add(id)) return@competition null
            val name = item.label.displayable() ?: return@competition null
            CompetitionRef(
                id = CompetitionId(id),
                name = name,
                roundLabel = null,
                logoUrl = safeFootballMediaUrl(item.logo),
                catalogId = item.id.scalarFootball().displayable(),
            )
        }
        CompetitionCatalogGroup(groupName, competitions).takeIf { competitions.isNotEmpty() }
    }
}

internal fun RankingTypesEnvelopeDto.toDomain(): List<RankingMetric> {
    if (template != "ranking_types") throw ContractViolation()
    return content?.data.orEmpty().mapNotNull { metric ->
        val id = metric.type.displayable() ?: return@mapNotNull null
        val name = metric.name.displayable() ?: return@mapNotNull null
        RankingMetric(id, name)
    }
}

internal fun RankingDetailEnvelopeDto.toDomain(
    competition: CompetitionRef,
    seasonLabel: String,
    entity: String,
): StatisticRankingTable {
    val expectedTemplate = if (entity == "person") "person_ranking" else "team_statistic_ranking"
    if (template != expectedTemplate && !(entity == "team" && template == "team_ranking")) {
        throw ContractViolation()
    }
    val valueLabel = content?.header?.lastOrNull().displayable() ?: "数据"
    val rows = content?.data.orEmpty().mapNotNull { row ->
        val rank = row.rank.scalarFootball().displayable() ?: return@mapNotNull null
        val columns = listOf(row.row1, row.row2, row.row3, row.row4, row.row5, row.row6)
            .map { it.scalarFootball().displayable() }
            .dropLastWhile { it == null }
        val value = row.count.scalarFootball().displayable()
            ?: columns.lastOrNull()
        if (entity == "person") {
            val playerId = row.personId.scalarFootball().displayable() ?: return@mapNotNull null
            val name = row.personName.displayable() ?: return@mapNotNull null
            val teamId = row.teamId.scalarFootball().displayable()
            val teamName = row.teamName.displayable()
            RankingRow(
                rankLabel = rank,
                name = name,
                imageUrl = safeFootballMediaUrl(row.personLogo),
                value = value,
                playerId = PlayerId(playerId.fullDqdId()),
                team = if (teamId != null && teamName != null) {
                    TeamRef(TeamId(teamId.fullDqdId()), teamName, safeFootballMediaUrl(row.teamLogo))
                } else {
                    null
                },
                columns = columns,
            )
        } else {
            val teamId = row.teamId.scalarFootball().displayable() ?: return@mapNotNull null
            val name = row.teamName.displayable() ?: return@mapNotNull null
            val team = TeamRef(TeamId(teamId.fullDqdId()), name, safeFootballMediaUrl(row.teamLogo))
            RankingRow(rank, name, team.crestUrl, value, team = team, columns = columns)
        }
    }
    return StatisticRankingTable(
        competition = competition,
        seasonLabel = seasonLabel,
        valueColumnLabel = valueLabel,
        rows = rows,
        headers = content?.header.orEmpty().mapNotNull { it.displayable() },
    )
}

internal fun TeamScheduleEnvelopeDto.toDomain(
    zoneId: ZoneId,
    requestedSeasonId: String?,
): TeamScheduleData {
    val seasons = seasonList.orEmpty().mapNotNull { option ->
        val label = option.name.displayable() ?: return@mapNotNull null
        val id = option.url.queryValue("season") ?: label.replace('/', '-')
        SeasonOption(id = id, label = label, isCurrent = option.current == true)
    }
    val selectedSeasonId = requestedSeasonId
        ?: seasons.firstOrNull { it.isCurrent }?.id
        ?: seasons.firstOrNull()?.id
    val matches = data.orEmpty()
        .asSequence()
        .filter { it.relateType == null || it.relateType == "match" }
        .filter { it.competitionType == null || it.competitionType == "soccer" }
        .map { it.toDomain(zoneId) }
        .sortedBy { it.kickoff }
        .map { it.match }
        .toList()
    return TeamScheduleData(seasons, selectedSeasonId, matches)
}

internal fun TeamSampleDto.toDomain(): TeamProfile {
    val id = teamId.scalarFootball().requiredFootball().fullDqdId()
    val descriptions = description.orEmpty().associate { it.key.orEmpty() to it.value.orEmpty() }
    val leagueRank = descriptions.entries.firstOrNull { it.key.endsWith("排名") }
    val rankOnly = rank.displayable()
        ?.substringBefore("总身价")
        ?.trim()
        ?.takeIf(String::isNotEmpty)
    return TeamProfile(
        id = TeamId(id),
        name = teamName.requiredFootball(),
        crestUrl = safeFootballMediaUrl(teamLogo),
        englishName = teamEnglishName.displayable(),
        country = country.displayable(),
        city = city.displayable(),
        competitionName = leagueRank?.key?.removeSuffix("排名"),
        venue = venueName.displayable(),
        venueCapacity = venueCapacity.scalarFootball().displayable(),
        foundedLabel = founded.scalarFootball().displayable(),
        rankLabel = rankOnly,
        marketValueLabel = marketValue.scalarFootball().formatEuroValue(),
        leagueRankLabel = leagueRank?.value.displayable(),
        leagueRecordLabel = descriptions["联赛战绩"].displayable(),
        type = when (type?.lowercase()) {
            "club" -> TeamType.Club
            "national", "nation" -> TeamType.National
            else -> TeamType.Unknown
        },
        recentForm = descriptions["最近5场"].toForm(),
    )
}

internal fun TeamDetailDto.toDomain(): TeamProfile {
    val base = baseInfo ?: throw ContractViolation()
    val id = base.teamId.scalarFootball().requiredFootball().fullDqdId()
    val mappedFacts = facts.orEmpty().mapNotNull { fact ->
            val label = fact.type.displayable() ?: return@mapNotNull null
            val value = fact.value.displayable() ?: return@mapNotNull null
            TeamFact(label, value)
        }.ifEmpty {
            listOfNotNull(
                base.address.displayable()?.let { TeamFact("地址", it) },
                base.telephone.displayable()?.let { TeamFact("电话", it) },
                base.email.displayable()?.let { TeamFact("邮箱", it) },
            )
        }.distinctBy { it.label to it.value }
    val trophies = trophyInfo.orEmpty().mapNotNull { trophy ->
        TeamHonor(
            name = trophy.competitionName.displayable() ?: return@mapNotNull null,
            imageUrl = safeFootballMediaUrl(trophy.trophyImage),
            timesLabel = trophy.times.scalarFootball().displayable(),
            seasons = trophy.lists.orEmpty().mapNotNull { it.seasonName.displayable() },
        )
    }
    val honors = if (trophies.isNotEmpty()) trophies else honorInfo.orEmpty().mapNotNull { honor ->
        TeamHonor(
            name = honor.name.displayable() ?: return@mapNotNull null,
            imageUrl = safeFootballMediaUrl(honor.logo),
            timesLabel = honor.times.scalarFootball().displayable(),
            seasons = honor.honorList.orEmpty().mapNotNull { it.seasonName.displayable() },
        )
    }
    val coaches = historyCoach.orEmpty().mapNotNull { coach ->
        val person = coach.person ?: return@mapNotNull null
        val personId = person.id.scalarFootball().displayable() ?: return@mapNotNull null
        val name = person.name.displayable() ?: return@mapNotNull null
        val record = listOfNotNull(
            coach.win.scalarFootball().displayable()?.let { "${it}胜" },
            coach.draw.scalarFootball().displayable()?.let { "${it}平" },
            coach.loss.scalarFootball().displayable()?.let { "${it}负" },
        ).joinToString(" ").displayable()
        HistoricalCoach(
            player = PlayerRef(
                id = PlayerId(personId.fullDqdId()),
                name = name,
                avatarUrl = safeFootballMediaUrl(person.logo),
            ),
            startDate = coach.startDate.displayable(),
            endDate = coach.endDate.displayable(),
            recordLabel = record,
            winRateLabel = coach.winRate.scalarFootball().displayable()?.let { "$it%" },
            durationLabel = coach.time.displayable(),
        )
    }
    val rankHistory = historyRank?.season.orEmpty().mapIndexedNotNull { index, label ->
        val item = historyRank?.data?.getOrNull(index) ?: return@mapIndexedNotNull null
        TeamRankHistoryPoint(
            seasonLabel = label,
            rank = item.rank.optionalFootballInt() ?: return@mapIndexedNotNull null,
            teamCount = item.competitionClubs.optionalFootballInt(),
        )
    }
    return TeamProfile(
        id = TeamId(id),
        name = base.teamName.requiredFootball(),
        crestUrl = safeFootballMediaUrl(base.teamLogo),
        englishName = base.teamEnglishName.displayable(),
        country = base.country.displayable(),
        city = base.city.displayable(),
        competitionName = null,
        venue = base.venueName.displayable(),
        venueCapacity = base.venueCapacity.scalarFootball().displayable(),
        foundedLabel = base.founded.scalarFootball().displayable(),
        rankLabel = base.rank.displayable(),
        marketValueLabel = base.marketValue.scalarFootball().formatEuroValue(),
        type = when (base.type?.lowercase()) {
            "club" -> TeamType.Club
            "national", "nation" -> TeamType.National
            else -> TeamType.Unknown
        },
        recentForm = emptyList(),
        facts = mappedFacts,
        honors = honors,
        historicalCoaches = coaches,
        rankHistory = rankHistory,
        topScorers = goalsInfo.toTeamRecordEntries(),
        appearanceLeaders = appsInfo.toTeamRecordEntries(),
        description = base.description.scalarFootball().displayable()
            ?: historyInfo.scalarFootball().displayable()
            ?: archiveInfo.scalarFootball().displayable(),
    )
}

internal fun TeamStatisticDto.toDomain(): TeamStatistics {
    val categories = listOf(
        "进攻" to statistics?.attack,
        "组织" to statistics?.organize,
        "防守" to statistics?.defensive,
        "纪律" to statistics?.discipline,
    ).mapNotNull { (name, items) ->
        val values = items.orEmpty().mapIndexedNotNull { index, item ->
            val label = item.type.displayable() ?: return@mapIndexedNotNull null
            PlayerSeasonStat("$name-$index", label, item.number.scalarFootball().displayable(), index)
        }
        TeamStatisticCategory(name, values).takeIf { values.isNotEmpty() }
    }
    val keyPlayers = person.orEmpty().mapNotNull { item ->
        val rawPerson = item.person ?: return@mapNotNull null
        val id = rawPerson.id.scalarFootball().displayable() ?: return@mapNotNull null
        TeamKeyPlayer(
            metric = item.type.displayable() ?: return@mapNotNull null,
            player = PlayerRef(
                PlayerId(id.fullDqdId()),
                rawPerson.name.requiredFootball(),
                safeFootballMediaUrl(rawPerson.logo),
            ),
            value = item.number.scalarFootball().displayable(),
        )
    }
    val record = season?.matches
    val recordLabel = if (record != null) {
        listOfNotNull(
            record.win.scalarFootball().displayable()?.let { "${it}胜" },
            record.draw.scalarFootball().displayable()?.let { "${it}平" },
            record.lose.scalarFootball().displayable()?.let { "${it}负" },
        ).joinToString(" ").displayable()
    } else {
        null
    }
    return TeamStatistics(
        seasonLabel = season?.name.displayable()
            ?: seasonList.orEmpty().firstOrNull { it.current == true }?.name.displayable()
            ?: seasonList.orEmpty().firstOrNull()?.name.displayable(),
        rankLabel = season?.rank.scalarFootball().displayable()?.let { "第${it}" },
        recordLabel = recordLabel,
        // The endpoint only returns aggregate win/draw/loss counts here, not match order.
        recentForm = emptyList(),
        categories = categories,
        keyPlayers = keyPlayers,
        seasons = seasonList.orEmpty().mapNotNull { option ->
            val label = option.name.displayable() ?: return@mapNotNull null
            val id = option.url.queryValue("season_id")
                ?: option.id.scalarFootball().displayable()
                ?: return@mapNotNull null
            SeasonOption(id, label, option.current == true)
        },
        selectedSeasonId = seasonList.orEmpty().firstOrNull { it.current == true }
            ?.url.queryValue("season_id"),
        rankingTrend = rankingTrend?.weeks.orEmpty().mapIndexedNotNull { index, week ->
            TeamRankingTrendPoint(
                weekLabel = week.week.scalarFootball().displayable() ?: (index + 1).toString(),
                rank = week.rank.optionalFootballInt() ?: return@mapIndexedNotNull null,
                dateLabel = listOfNotNull(
                    week.windowStart.displayable(),
                    week.windowEnd.displayable(),
                ).joinToString(" - ").displayable(),
                match = week.matches.orEmpty().firstOrNull()?.let { match ->
                    val homeId = match.homeTeamId.scalarFootball().displayable()
                        ?: return@let null
                    val awayId = match.awayTeamId.scalarFootball().displayable()
                        ?: return@let null
                    TeamRankingTrendMatch(
                        home = TeamRef(
                            TeamId(homeId.fullDqdId()),
                            match.homeTeamName.displayable() ?: return@let null,
                            safeFootballMediaUrl(match.homeTeamLogo),
                        ),
                        away = TeamRef(
                            TeamId(awayId.fullDqdId()),
                            match.awayTeamName.displayable() ?: return@let null,
                            safeFootballMediaUrl(match.awayTeamLogo),
                        ),
                        homeScore = match.homeScore.optionalFootballInt(),
                        awayScore = match.awayScore.optionalFootballInt(),
                    )
                },
            )
        },
        characteristics = characteristics.toDomain(),
    )
}

private fun List<TeamRecordLeaderDto>?.toTeamRecordEntries(): List<TeamRecordEntry> =
    orEmpty().mapNotNull { item ->
        val person = item.person ?: return@mapNotNull null
        val id = person.id.scalarFootball().displayable() ?: return@mapNotNull null
        TeamRecordEntry(
            rank = item.rank.optionalFootballInt() ?: return@mapNotNull null,
            player = PlayerRef(
                id = PlayerId(id.fullDqdId()),
                name = person.name.displayable() ?: return@mapNotNull null,
                avatarUrl = safeFootballMediaUrl(person.logo),
            ),
            countLabel = item.count.scalarFootball().displayable() ?: return@mapNotNull null,
            birthdayLabel = person.dateOfBirth.displayable(),
            nationality = person.nationality?.name.displayable(),
        )
    }

private fun FootballCharacteristicsDto?.toDomain(): FootballCharacteristics? {
    if (this == null) return null
    return FootballCharacteristics(
        styles = styles.orEmpty().mapNotNull(String::displayable),
        veryStrong = strength?.veryStrong.orEmpty().mapNotNull(String::displayable),
        strong = strength?.strong.orEmpty().mapNotNull(String::displayable),
        weak = weakness?.weak.orEmpty().mapNotNull(String::displayable),
        veryWeak = weakness?.veryWeak.orEmpty().mapNotNull(String::displayable),
    ).takeIf {
        it.styles.isNotEmpty() || it.veryStrong.isNotEmpty() || it.strong.isNotEmpty() ||
            it.weak.isNotEmpty() || it.veryWeak.isNotEmpty()
    }
}

internal fun TeamMembersEnvelopeDto.toDomain(requestedSeasonId: String?): TeamSquadData {
    if (code.optionalFootballInt() != 0) throw ContractViolation()
    val mappedSeasons = seasons.orEmpty().mapNotNull { option ->
        val label = option.name.displayable() ?: return@mapNotNull null
        val id = option.url.queryValue("season")
            ?: option.id.scalarFootball().displayable()
            ?: return@mapNotNull null
        SeasonOption(id, label, option.current == true)
    }
    val groups = data?.list.orEmpty().mapNotNull { group ->
        val groupTitle = group.title.displayable() ?: return@mapNotNull null
        val kind = group.type.toTeamMemberGroupKind()
        val members = group.data.orEmpty().mapNotNull { member ->
            val id = member.personId.scalarFootball().displayable() ?: return@mapNotNull null
            val name = member.personName.displayable() ?: return@mapNotNull null
            SquadMember(
                id = PlayerId(id.fullDqdId()),
                name = name,
                shirtNumber = member.shirtnumber.optionalFootballInt(),
                position = member.type.toPlayerPosition(),
                nationality = member.nationalityName.displayable(),
                ageLabel = member.age.displayable(),
                avatarUrl = safeFootballMediaUrl(member.personLogo),
                roleLabel = if (member.type.toPlayerPosition() == PlayerPosition.Unknown) {
                    member.type.displayable() ?: groupTitle
                } else {
                    null
                },
                stats = member.statistic.orEmpty().flatMap { values ->
                    values.entries.mapIndexed { index, entry ->
                        PlayerSeasonStat(
                            id = "${id}-${entry.key}",
                            label = entry.key,
                            value = entry.value.scalarFootball().displayable(),
                            displayOrder = index,
                        )
                    }
                },
                salaryLabel = member.weeklySalary.displayable()?.let { "${it}万/周" },
                isCaptain = member.captainLogo.displayable() != null,
            )
        }
        TeamSquadGroup(
            title = groupTitle,
            kind = kind,
            members = members,
            statisticLabels = group.statistics.orEmpty().mapNotNull(String::displayable),
        ).takeIf { members.isNotEmpty() }
    }.sortedBy { TEAM_GROUP_ORDER.indexOf(it.kind).let { index -> if (index < 0) Int.MAX_VALUE else index } }
    return TeamSquadData(
        seasons = mappedSeasons,
        selectedSeasonId = requestedSeasonId
            ?: mappedSeasons.firstOrNull { it.isCurrent }?.id
            ?: mappedSeasons.firstOrNull()?.id,
        groups = groups,
    )
}

internal fun TeamTransferEnvelopeDto.toDomain(requestedWindowId: String?): TeamTransferData {
    if (errCode.optionalFootballInt()?.let { it != 200 && it != 0 } == true) throw ContractViolation()
    val raw = data ?: return TeamTransferData(emptyList(), null, emptyList())
    val windows = raw.windows.orEmpty().mapNotNull { window ->
        val label = window.name.displayable() ?: return@mapNotNull null
        val id = window.url.queryValue("window") ?: return@mapNotNull null
        TeamTransferWindow(id, label, window.current == true)
    }
    val selected = requestedWindowId
        ?: windows.firstOrNull { it.isCurrent }?.id
        ?: windows.firstOrNull()?.id
    val selectedLabel = windows.firstOrNull { it.id == selected }?.label
    val groups = selectedLabel?.let { raw.transfer.orEmpty()[it] }
        .orEmpty()
        .mapNotNull { group ->
            val entries = group.data.orEmpty().mapNotNull entry@{ entry ->
                val person = entry.person ?: return@entry null
                val personId = person.id.scalarFootball().displayable() ?: return@entry null
                val name = person.name.displayable() ?: return@entry null
                TeamTransferEntry(
                    player = PlayerRef(
                        PlayerId(personId.fullDqdId()),
                        name,
                        safeFootballMediaUrl(person.logo),
                    ),
                    ageLabel = person.age.displayable(),
                    roleLabel = person.role.displayable(),
                    nationality = person.nation.displayable(),
                    feeLabel = person.valueMoney.displayable(),
                    dateLabel = person.startDate.displayable(),
                    fromTeam = entry.fromTeam.toTeamRef(),
                    toTeam = entry.toTeam.toTeamRef(),
                )
            }
            TeamTransferGroup(
                title = group.title.displayable() ?: return@mapNotNull null,
                valueLabel = group.value.displayable(),
                entries = entries,
            ).takeIf { entries.isNotEmpty() }
        }
    return TeamTransferData(windows, selected, groups)
}

internal fun PlayerDetailDto.toDomain(): PlayerOverview {
    val base = baseInfo ?: throw ContractViolation()
    val id = base.personId.scalarFootball().requiredFootball().fullDqdId()
    val team = base.teamInfo?.let { raw ->
        val teamId = raw.teamId.scalarFootball().displayable()
        val name = raw.teamName.displayable()
        if (teamId != null && name != null) {
            TeamRef(TeamId(teamId.fullDqdId()), name, safeFootballMediaUrl(raw.teamLogo))
        } else {
            null
        }
    }
    val profile = PlayerProfile(
        id = PlayerId(id),
        name = base.personName.requiredFootball(),
        avatarUrl = safeFootballMediaUrl(base.personLogo),
        englishName = base.personEnglishName.displayable(),
        team = team,
        position = base.position.toPlayerPosition(),
        shirtNumber = base.teamInfo?.shirtnumber.optionalFootballInt(),
        nationality = base.nationality.displayable(),
        ageLabel = base.age.displayable(),
        birthdayLabel = base.dateOfBirth.displayable(),
        heightLabel = base.height.scalarFootball().displayable()?.let { "${it}cm" },
        weightLabel = base.weight.scalarFootball().displayable()?.let { "${it}kg" },
        footLabel = base.foot.displayable(),
        marketValueLabel = base.marketValue.scalarFootball().formatPlayerMarketValue(),
        contractUntil = base.contract.displayable(),
        nickname = base.nickname.displayable(),
        otherNationalities = base.otherNationality.orEmpty().mapNotNull { it.displayable() },
        weeklySalaryLabel = base.weeklySalary.scalarFootball().displayable()
            ?.takeUnless { it == "0" },
    )
    val honors = honorInfo.orEmpty().mapNotNull { honor ->
        val name = honor.name.displayable() ?: return@mapNotNull null
        PlayerHonor(
            name,
            safeFootballMediaUrl(honor.logo),
            honor.times.scalarFootball().displayable(),
            honor.honorList.orEmpty().mapNotNull { it.seasonName.displayable() },
        )
    }
    val transfers = transferInfo.orEmpty().mapNotNull { transfer ->
        val from = transfer.fromTeamRef()
        val to = transfer.toTeamRef()
        if (from == null && to == null) return@mapNotNull null
        PlayerTransfer(
            transfer.announcedDate.displayable(),
            transfer.type.displayable(),
            transfer.money.displayable(),
            from,
            to,
        )
    }
    val injuries = injuryRecords?.history.orEmpty().mapNotNull { injury ->
        PlayerInjury(
            type = injury.injury.displayable() ?: return@mapNotNull null,
            teamName = injury.teams?.firstOrNull()?.name.displayable(),
            startDate = injury.dateFrom.displayable(),
            endDate = injury.dateUntil.displayable(),
            gamesMissed = injury.gamesMissed.optionalFootballInt(),
            durationDays = injury.days.optionalFootballInt(),
        )
    }
    val marketValues = historyMarketValues.orEmpty()
        .values
        .flatten()
        .mapNotNull { point ->
            val date = point.recordDate.displayable() ?: return@mapNotNull null
            val label = point.marketValueText.displayable() ?: return@mapNotNull null
            val rawTeam = point.teamInfo
            val pointTeam = if (rawTeam != null) {
                val rawId = rawTeam.id.scalarFootball().displayable()
                val teamName = rawTeam.name.displayable()
                if (rawId != null && teamName != null) {
                    TeamRef(
                        TeamId(rawId.fullDqdId()),
                        teamName,
                        safeFootballMediaUrl(rawTeam.logo),
                    )
                } else {
                    null
                }
            } else {
                null
            }
            MarketValuePoint(
                dateLabel = date,
                valueLabel = label,
                value = point.marketValue.scalarFootball()?.toLongOrNull(),
                team = pointTeam,
            )
        }
        .sortedBy { it.dateLabel }
    val mappedFacts = facts.orEmpty().mapNotNull { fact ->
        PlayerProfileFact(
            label = fact.type.displayable() ?: return@mapNotNull null,
            value = fact.value.displayable() ?: return@mapNotNull null,
        )
    }
    return PlayerOverview(
        profile = profile,
        honors = honors,
        transfers = transfers,
        injuries = injuries,
        marketValues = marketValues,
        facts = mappedFacts,
        characteristics = characterInfo.toDomain(),
        clubCareer = playerCareerInfo.toCareerSummaries(),
        nationalCareer = playerNationCareerInfo.toCareerSummaries(),
    )
}

private fun List<PlayerCareerSummaryDto>?.toCareerSummaries(): List<PlayerCareerSummary> =
    orEmpty().mapNotNull { entry ->
        val teamId = entry.teamId.scalarFootball().displayable() ?: return@mapNotNull null
        val teamName = entry.teamName.displayable() ?: return@mapNotNull null
        PlayerCareerSummary(
            team = TeamRef(
                id = TeamId(teamId.fullDqdId()),
                name = teamName,
                crestUrl = safeFootballMediaUrl(entry.teamLogo),
            ),
            startDate = entry.startDate.displayable(),
            endDate = entry.endDate.displayable(),
            appearances = entry.appearance.optionalFootballInt(),
            goals = entry.goals.optionalFootballInt(),
            assists = entry.assist.optionalFootballInt(),
            goalsConceded = entry.goalsConceded.optionalFootballInt(),
            cleanSheets = entry.cleanSheets.optionalFootballInt(),
        )
    }

internal fun PlayerStatisticsDto.toCareerEntries(): List<CareerEntry> = total.orEmpty().mapNotNull { entry ->
    val values = entry.list.orEmpty().associate { it.title.orEmpty() to it.value.scalarFootball() }
    CareerEntry(
        seasonLabel = entry.season?.name.displayable() ?: return@mapNotNull null,
        teamName = entry.team?.name.displayable() ?: return@mapNotNull null,
        competitionName = null,
        appearances = values["出场"].toNullableInt(),
        goals = values["进球"].toNullableInt(),
        starts = values["首发"].toNullableInt(),
        assists = values["助攻"].toNullableInt(),
        yellowCards = values["黄牌"].toNullableInt(),
        redCards = values["红牌"].toNullableInt(),
    )
}

internal fun PlayerStatisticsDto.toDomain(): PlayerStatisticsData {
    val rawByScope = linkedMapOf(
        PlayerStatisticScope.Total to total.orEmpty(),
        PlayerStatisticScope.League to league.orEmpty(),
        PlayerStatisticScope.Cup to cup.orEmpty(),
        PlayerStatisticScope.NationalTeam to international.orEmpty(),
    )
    val mapped = rawByScope.mapValues { (scope, entries) ->
        entries.mapIndexedNotNull { index, entry -> entry.toDomain(scope, index) }
    }
    val defaultScope = when (tabsDefault?.lowercase()) {
        "league" -> PlayerStatisticScope.League
        "cup" -> PlayerStatisticScope.Cup
        "international", "national" -> PlayerStatisticScope.NationalTeam
        else -> PlayerStatisticScope.Total
    }
    return PlayerStatisticsData(defaultScope, mapped)
}

private fun PlayerCareerDto.toDomain(
    scope: PlayerStatisticScope,
    index: Int,
): PlayerStatisticEntry? {
    val rawSeason = season ?: return null
    val seasonId = rawSeason.seasonId.scalarFootball().displayable()
        ?: rawSeason.id.scalarFootball().displayable()
        ?: return null
    val seasonLabel = rawSeason.name.displayable() ?: return null
    val rawTeam = team ?: return null
    val teamId = rawTeam.id.scalarFootball().displayable() ?: return null
    val teamName = rawTeam.name.displayable() ?: return null
    val entryId = id.scalarFootball().displayable() ?: "$seasonId-$teamId-${scope.name}-$index"
    val mappedTeam = TeamRef(
        TeamId(teamId.fullDqdId()),
        teamName,
        safeFootballMediaUrl(rawTeam.logo),
    )
    val mappedCompetition = competition?.let { rawCompetition ->
        val competitionId = rawCompetition.id.scalarFootball().displayable()
        val name = rawCompetition.shortName.displayable() ?: rawCompetition.name.displayable()
        if (competitionId != null && name != null) {
            CompetitionRef(
                CompetitionId(competitionId.normalizedDqdId()),
                name,
                roundLabel = null,
                logoUrl = safeFootballMediaUrl(rawCompetition.logo),
            )
        } else {
            null
        }
    }
    val summary = if (!list.isNullOrEmpty()) {
        list.orEmpty().mapIndexedNotNull { valueIndex, value ->
            PlayerSeasonStat(
                id = "$entryId-summary-$valueIndex",
                label = value.title.displayable() ?: return@mapIndexedNotNull null,
                value = value.value.scalarFootball().displayable(),
                displayOrder = valueIndex,
            )
        }
    } else {
        baseInfo.toPlayerSummaryValues("$entryId-summary")
    }
    val sections = listOf(
        "进攻" to attack,
        "传球" to pass,
        "防守" to defense,
        "纪律" to discipline,
        "跑动" to running,
    ).mapNotNull { (name, values) ->
        val mappedValues = values.toStatValues("$entryId-$name")
        PlayerStatSection(name, mappedValues).takeIf { mappedValues.isNotEmpty() }
    }
    return PlayerStatisticEntry(
        id = entryId,
        season = SeasonOption(seasonId, seasonLabel, index == 0),
        competition = mappedCompetition,
        team = mappedTeam,
        summary = summary,
        sections = sections,
    )
}

internal fun PlayerMatchesEnvelopeDto.toDomain(zoneId: ZoneId): PlayerMatchPage {
    val mapped = matches.orEmpty().mapNotNull { item ->
        runCatching {
            val fullMatchId = MATCH_ID_PATTERN.find(item.scheme.orEmpty())
                ?.groupValues
                ?.getOrNull(1)
                ?: item.matchId.scalarFootball().requiredFootball().fullDqdId()
            val homeId = item.homeTeamId.scalarFootball().requiredFootball().fullDqdId()
            val awayId = item.awayTeamId.scalarFootball().requiredFootball().fullDqdId()
            val hasScore = item.homeScore.scalarFootball().displayable() != null &&
                item.awayScore.scalarFootball().displayable() != null
            val match = item.copy(
                matchId = JsonPrimitive(fullMatchId),
                homeTeamId = JsonPrimitive(homeId),
                awayTeamId = JsonPrimitive(awayId),
                status = item.status.displayable() ?: if (hasScore) "Played" else "Fixture",
            ).toDomain(zoneId).match
            PlayerMatchPerformance(
                match = match,
                minutesLabel = item.minute.scalarFootball().displayable(),
                goals = item.goals.optionalFootballInt(),
                assists = item.assists.optionalFootballInt(),
                cardsLabel = item.cards.scalarFootball().displayable(),
                ratingLabel = item.rating.scalarFootball().displayable(),
                userRatingLabel = item.dqdRating.scalarFootball().displayable(),
            )
        }.getOrNull()
    }
    return PlayerMatchPage(
        matches = mapped,
        page = page.optionalFootballInt() ?: 1,
        totalPages = totalPage.optionalFootballInt() ?: 1,
    )
}

internal fun PlayerMatchesEnvelopeDto.userRatingFor(matchId: MatchId): String? =
    matches.orEmpty().firstOrNull { item ->
        val schemeMatchId = MATCH_ID_PATTERN.find(item.scheme.orEmpty())
            ?.groupValues
            ?.getOrNull(1)
        schemeMatchId == matchId.raw || item.matchId.scalarFootball() == matchId.raw
    }?.dqdRating.scalarFootball().displayable()

internal fun PlayerHeatMapDto.toDomain(): PlayerHeatMap = PlayerHeatMap(
    points = heatmap.orEmpty().mapNotNull { point ->
        val x = point.x.scalarFootball()?.toFloatOrNull() ?: return@mapNotNull null
        val y = point.y.scalarFootball()?.toFloatOrNull() ?: return@mapNotNull null
        HeatPoint(x, y).takeIf { x in 0f..100f && y in 0f..100f }
    },
    direction = direction.displayable(),
)

internal fun PlayerShotMapDto.toDomain(matchId: MatchId): PlayerShotMap {
    val rawSummary = shots.orEmpty().firstOrNull { it.minute.scalarFootball() == null }
    val summary = rawSummary?.let { raw ->
        PlayerShotSummary(
            expectedGoalsLabel = raw.xg.scalarFootball().displayable(),
            total = raw.total.optionalFootballInt(),
            goals = raw.goals.optionalFootballInt(),
            onTarget = raw.onTarget.optionalFootballInt(),
            offTarget = raw.offTarget.optionalFootballInt(),
        )
    }
    val mappedShots = shots.orEmpty().mapNotNull { shot ->
        val minute = shot.minute.optionalFootballInt() ?: return@mapNotNull null
        val extra = shot.minuteExtra.optionalFootballInt()
        PlayerShot(
            minuteLabel = if (extra != null && extra > 0) "$minute+$extra'" else "$minute'",
            x = shot.startX.scalarFootball()?.toFloatOrNull(),
            y = shot.startY.scalarFootball()?.toFloatOrNull(),
            outcome = shot.outcomeText.displayable(),
            situation = shot.situationText.displayable(),
            shotType = shot.shotTypeText.displayable(),
            expectedGoalsLabel = shot.xg.scalarFootball().displayable(),
        )
    }
    return PlayerShotMap(matchId, summary, mappedShots)
}

internal fun PlayerAbilityEnvelopeDto.toDomain(): PlayerAbility? {
    val raw = data ?: return null
    val attributes = raw.redar.orEmpty().mapIndexedNotNull { index, attribute ->
        val name = attribute.name.displayable() ?: return@mapIndexedNotNull null
        PlayerSeasonStat(
            id = "ability-$index",
            label = name,
            value = attribute.`val`.scalarFootball().displayable(),
            displayOrder = index,
        )
    }
    return PlayerAbility(
        overall = raw.average?.`val`.optionalFootballInt(),
        version = raw.version.displayable(),
        attributes = attributes,
    )
}

internal fun StandingEnvelopeDto.toDomain(
    competition: CompetitionRef,
    seasonLabel: String,
): StandingTable {
    if (template != "team_point_ranking") throw ContractViolation()
    val rounds = content?.rounds ?: throw ContractViolation()
    val rows = mutableListOf<StandingRow>()
    val groups = mutableListOf<StandingGroup>()
    val knockoutStages = mutableListOf<KnockoutStage>()
    val matchStages = mutableListOf<CompetitionMatchStage>()
    rounds.forEach { round ->
        val roundContent = round.content ?: throw ContractViolation()
        when (round.template) {
            "team_point_ranking_group" -> {
                roundContent.data.orEmpty().forEach { group ->
                    val groupName = group.string("name") ?: return@forEach
                    val descriptions = group.descriptions("desc")
                    val groupRows = (group["data"] as? JsonArray).orEmpty()
                        .mapNotNull { it as? JsonObject }
                        .mapIndexed { index, row -> row.toStandingRow(zoneAt(index, descriptions)) }
                    if (groupRows.isNotEmpty()) groups += StandingGroup(groupName, groupRows)
                }
            }

            "team_point_ranking_knockout" -> {
                val ties = roundContent.data.orEmpty().mapNotNull { node ->
                    val home = (node["TeamA"] as? JsonObject).toTeamRefOrNull()
                    val away = (node["TeamB"] as? JsonObject).toTeamRefOrNull()
                    if (home == null && away == null) return@mapNotNull null
                    val matchIds = (node["matches"] as? JsonArray).orEmpty().mapNotNull { match ->
                        (match as? JsonObject)?.string("match_id")?.let(::MatchId)
                    }
                    KnockoutTie(
                        home = home,
                        away = away,
                        scoreLabel = node.string("total_score") ?: node.string("match_score"),
                        winner = node.string("winner"),
                        matchIds = matchIds,
                    )
                }
                if (ties.isNotEmpty()) {
                    knockoutStages += KnockoutStage(
                        name = roundContent.name.displayable() ?: "淘汰赛",
                        ties = ties,
                    )
                }
            }

            "team_point_ranking_aggregate" -> {
                val ties = roundContent.data.orEmpty().mapNotNull(JsonObject::toAggregateTie)
                if (ties.isNotEmpty()) {
                    knockoutStages += KnockoutStage(
                        name = roundContent.name.displayable() ?: "淘汰赛",
                        ties = ties,
                    )
                }
            }

            "team_point_ranking_match" -> {
                val stageMatches = roundContent.data.orEmpty().mapNotNull { node ->
                    runCatching {
                        FOOTBALL_JSON.decodeFromJsonElement(MatchDto.serializer(), node)
                            .toDomain(
                                zoneId = ZoneId.systemDefault(),
                                fallbackCompetition = competition,
                                fallbackRoundLabel = roundContent.name,
                            ).match
                    }.getOrNull()
                }
                if (stageMatches.isNotEmpty()) {
                    matchStages += CompetitionMatchStage(
                        name = roundContent.name.displayable() ?: "赛程",
                        matches = stageMatches,
                    )
                }
            }

            "team_point_ranking_regular" -> {
                val descriptions = roundContent.desc.orEmpty()
                rows += roundContent.data.orEmpty().mapIndexed { index, row ->
                    row.toStandingRow(zoneAt(index, descriptions))
                }
            }
        }
    }
    return StandingTable(
        competition = competition,
        seasonLabel = seasonLabel.requiredFootball(),
        rows = rows,
        groups = groups,
        knockoutStages = knockoutStages,
        matchStages = matchStages,
    )
}

private fun JsonObject.toAggregateTie(): KnockoutTie? {
    val total = this["total"] as? JsonObject ?: return null
    val home = total.toPrefixedTeamRef("team_A")
    val away = total.toPrefixedTeamRef("team_B")
    if (home == null && away == null) return null
    val homeScore = total.string("fs_A")
    val awayScore = total.string("fs_B")
    val homePenalty = total.string("ps_A")
    val awayPenalty = total.string("ps_B")
    val scoreLabel = if (homeScore != null && awayScore != null) {
        buildString {
            append(homeScore)
            append(" - ")
            append(awayScore)
            if (homePenalty != null && awayPenalty != null) {
                append(" (")
                append(homePenalty)
                append(" - ")
                append(awayPenalty)
                append(')')
            }
        }
    } else {
        null
    }
    val decidingHome = homePenalty?.toIntOrNull() ?: homeScore?.toIntOrNull()
    val decidingAway = awayPenalty?.toIntOrNull() ?: awayScore?.toIntOrNull()
    val winner = when {
        decidingHome == null || decidingAway == null || decidingHome == decidingAway -> null
        decidingHome > decidingAway -> "left"
        else -> "right"
    }
    val matchIds = listOf("match1", "match2").mapNotNull { key ->
        (this[key] as? JsonObject)?.string("match_id")?.let(::MatchId)
    }
    return KnockoutTie(home, away, scoreLabel, winner, matchIds)
}

private fun JsonObject.toStandingRow(zone: StandingZone?): StandingRow {
    val goalsForValue = value("goals_pro").optionalFootballInt()
    val goalsAgainstValue = value("goals_against").optionalFootballInt()
    return StandingRow(
        rank = value("rank").requiredFootballInt(),
        team = TeamRef(
            id = TeamId(value("team_id").scalarFootball().requiredFootball().fullDqdId()),
            name = string("team_name").requiredFootball(),
            crestUrl = safeFootballMediaUrl(string("team_logo")),
        ),
        played = value("matches_total").optionalFootballInt(),
        won = value("matches_won").optionalFootballInt(),
        drawn = value("matches_draw").optionalFootballInt(),
        lost = value("matches_lost").optionalFootballInt(),
        goalDifference = if (goalsForValue != null && goalsAgainstValue != null) {
            goalsForValue - goalsAgainstValue
        } else {
            null
        },
        points = value("points").optionalFootballInt(),
        zone = zone,
    )
}

private fun zoneAt(
    index: Int,
    descriptions: List<StandingDescriptionDto>,
): StandingZone? = descriptions.firstNotNullOfOrNull { description ->
    val start = description.from.optionalFootballInt() ?: return@firstNotNullOfOrNull null
    val end = description.to.optionalFootballInt() ?: return@firstNotNullOfOrNull null
    if (index !in start..end) return@firstNotNullOfOrNull null
    when {
        description.text.orEmpty().contains("欧冠") -> StandingZone.ChampionsLeague
        description.text.orEmpty().contains("欧联") -> StandingZone.EuropaLeague
        description.text.orEmpty().contains("欧协") -> StandingZone.ConferenceLeague
        description.text.orEmpty().contains("升级") -> StandingZone.Promotion
        description.text.orEmpty().contains("降级") -> StandingZone.Relegation
        else -> null
    }
}

private fun MatchDto.liveMinuteLabel(): String? {
    val regular = minute.scalarFootball()?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val extra = minuteExtra.scalarFootball()?.trim()?.takeIf(String::isNotEmpty)
    return if (extra == null || extra == "0") "$regular'" else "$regular+$extra'"
}

private fun JsonElement?.requiredFootballInt(): Int =
    optionalFootballInt() ?: throw ContractViolation()

private fun JsonElement?.optionalFootballInt(): Int? {
    val value = scalarFootball()?.trim() ?: return null
    if (value.isEmpty()) return null
    return value.toIntOrNull() ?: throw ContractViolation()
}

internal fun JsonElement?.scalarFootball(): String? =
    (this as? JsonPrimitive)?.contentOrNull

private fun String?.requiredFootball(): String =
    this?.trim()?.takeIf(String::isNotEmpty) ?: throw ContractViolation()

private fun String?.displayable(): String? =
    this?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

private fun String.normalizedDqdId(): String {
    val normalized = if (length >= 8 && startsWith("50")) drop(2).trimStart('0') else this
    return normalized.ifEmpty { "0" }
}

private fun String.fullDqdId(): String = if (length >= 8 && startsWith("50")) {
    this
} else {
    "50${padStart(6, '0')}"
}

private fun String?.formatEuroValue(): String? {
    val value = displayable() ?: return null
    val amount = value.toLongOrNull() ?: return value
    return when {
        amount >= 100_000_000L -> "${formatDecimal(amount / 100_000_000.0)}亿欧"
        amount >= 10_000L -> "${formatDecimal(amount / 10_000.0)}万欧"
        else -> "${amount}欧"
    }
}

private fun String?.formatPlayerMarketValue(): String? {
    val value = displayable() ?: return null
    return value.toLongOrNull()?.let { "${it}万欧" } ?: value
}

private fun formatDecimal(value: Double): String =
    String.format(java.util.Locale.ROOT, "%.2f", value).trimEnd('0').trimEnd('.')

private fun String?.toForm(): List<FormResult> = displayable().orEmpty().mapNotNull { result ->
    when (result) {
        '胜', 'W', 'w' -> FormResult.Win
        '平', 'D', 'd' -> FormResult.Draw
        '负', 'L', 'l' -> FormResult.Loss
        else -> null
    }
}

private fun String?.toPlayerPosition(): PlayerPosition = when (displayable()?.lowercase()) {
    "门将", "守门员", "goalkeeper", "gk" -> PlayerPosition.Goalkeeper
    "后卫", "defender", "df" -> PlayerPosition.Defender
    "中场", "midfielder", "mf" -> PlayerPosition.Midfielder
    "前锋", "forward", "fw", "striker", "attacker" -> PlayerPosition.Forward
    else -> PlayerPosition.Unknown
}

private fun String?.toTeamMemberGroupKind(): TeamMemberGroupKind = when (displayable()?.lowercase()) {
    "coach" -> TeamMemberGroupKind.Coaches
    "manager" -> TeamMemberGroupKind.Staff
    "attacker", "forward" -> TeamMemberGroupKind.Forwards
    "midfielder" -> TeamMemberGroupKind.Midfielders
    "defender" -> TeamMemberGroupKind.Defenders
    "goalkeeper" -> TeamMemberGroupKind.Goalkeepers
    else -> TeamMemberGroupKind.Other
}

private fun JsonObject?.toStatValues(idPrefix: String): List<PlayerSeasonStat> =
    this.orEmpty().entries.mapIndexedNotNull { index, (key, element) ->
        val value = element.scalarFootball().displayable() ?: return@mapIndexedNotNull null
        val label = PLAYER_STAT_LABELS[key] ?: key.replace('_', ' ')
        PlayerSeasonStat(
            id = "$idPrefix-$key",
            label = label,
            value = if (key.endsWith("_rate") && !value.endsWith('%')) "$value%" else value,
            displayOrder = index,
        )
    }

private fun JsonObject?.toPlayerSummaryValues(idPrefix: String): List<PlayerSeasonStat> {
    val values = this ?: return emptyList()
    val appearances = values["appearances"].scalarFootball().displayable()
    val starts = values["starts"].scalarFootball().displayable()
    return listOfNotNull(
        listOfNotNull(appearances, starts).joinToString("/")
            .takeIf(String::isNotEmpty)
            ?.let { PlayerSeasonStat("$idPrefix-appearances", "出场/首发", it, 0) },
        values["avg_appearances_time"].scalarFootball().displayable()
            ?.let { PlayerSeasonStat("$idPrefix-time", "场均时间", it, 1) },
        values["goals"].scalarFootball().displayable()
            ?.let { PlayerSeasonStat("$idPrefix-goals", "进球", it, 2) },
        values["assists"].scalarFootball().displayable()
            ?.let { PlayerSeasonStat("$idPrefix-assists", "助攻", it, 3) },
    )
}

private fun String?.queryValue(name: String): String? =
    this?.toHttpUrlOrNull()?.queryParameter(name).displayable()

private fun TeamTransferTeamDto?.toTeamRef(): TeamRef? {
    val raw = this ?: return null
    return transferTeamRef(raw.id.scalarFootball(), raw.name, raw.logo)
}

private fun JsonObject?.toTeamRefOrNull(): TeamRef? {
    val raw = this ?: return null
    return transferTeamRef(raw.string("id"), raw.string("name"), raw.string("logo"))
}

private fun JsonObject.toPrefixedTeamRef(prefix: String): TeamRef? = transferTeamRef(
    id = string("${prefix}_id"),
    name = string("${prefix}_name"),
    logo = string("${prefix}_logo"),
)

private fun JsonObject.value(key: String): JsonElement? = this[key]

private fun JsonObject.string(key: String): String? = this[key].scalarFootball().displayable()

private fun JsonObject.descriptions(key: String): List<StandingDescriptionDto> =
    (this[key] as? JsonArray).orEmpty().mapNotNull { item ->
        val value = item as? JsonObject ?: return@mapNotNull null
        StandingDescriptionDto(
            from = value["from"],
            to = value["to"],
            text = value.string("text"),
        )
    }

private fun String?.toNullableInt(): Int? = displayable()?.toIntOrNull()

private fun PlayerTransferDto.fromTeamRef(): TeamRef? =
    transferTeamRef(fromTeamId.scalarFootball(), fromTeamName, fromTeamLogo)

private fun PlayerTransferDto.toTeamRef(): TeamRef? =
    transferTeamRef(toTeamId.scalarFootball(), toTeamName, toTeamLogo)

private fun transferTeamRef(id: String?, name: String?, logo: String?): TeamRef? {
    val safeId = id.displayable() ?: return null
    val safeName = name.displayable() ?: return null
    return TeamRef(TeamId(safeId.fullDqdId()), safeName, safeFootballMediaUrl(logo))
}

private fun safeFootballMediaUrl(raw: String?): String? {
    val value = raw?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val uri = runCatching { URI(value) }.getOrNull() ?: return null
    return value.takeIf {
        uri.scheme == "https" &&
            (uri.host == "qunliao.info" || uri.host?.endsWith(".qunliao.info") == true)
    }
}

private val TEAM_GROUP_ORDER = listOf(
    TeamMemberGroupKind.Coaches,
    TeamMemberGroupKind.Staff,
    TeamMemberGroupKind.Forwards,
    TeamMemberGroupKind.Midfielders,
    TeamMemberGroupKind.Defenders,
    TeamMemberGroupKind.Goalkeepers,
    TeamMemberGroupKind.Other,
)

private val PLAYER_STAT_LABELS = mapOf(
    "appearances" to "出场",
    "starts" to "首发",
    "goals" to "进球",
    "assists" to "助攻",
    "substitute_in" to "替补出场",
    "avg_appearances_time" to "场均时间",
    "expected_goals" to "预期进球",
    "shots" to "射门",
    "fouled" to "被犯规",
    "offsides" to "越位",
    "avg_goals" to "场均进球",
    "freq_goals" to "进球频率",
    "shots_on_target_rate" to "射正率",
    "big_chance_missed" to "错失重大机会",
    "penalty_won" to "赢得点球",
    "avg_dribbles_won" to "场均过人",
    "dribbles_rate" to "过人成功率",
    "expected_assists" to "预期助攻",
    "passes" to "传球",
    "avg_passes" to "场均传球",
    "key_passes" to "关键传球",
    "success_pass_rate" to "传球成功率",
    "avg_touches" to "场均触球",
    "big_chance_created" to "创造重大机会",
    "avg_longballs" to "场均长传",
    "longballs_rate" to "长传成功率",
    "avg_crosses" to "场均传中",
    "crosses_rate" to "传中成功率",
    "avg_dispossessed" to "场均丢失球权",
    "avg_tackles" to "场均抢断",
    "avg_interceptions" to "场均拦截",
    "avg_clearances" to "场均解围",
    "avg_aerials" to "场均争顶",
    "aerial_rate" to "争顶成功率",
    "blocked_shots" to "封堵射门",
    "error_lead_to_goal" to "致失球失误",
    "avg_was_dribbled" to "场均被过",
    "fouls" to "犯规",
    "yellow_cards" to "黄牌",
    "red_cards" to "红牌",
    "appearances_time" to "上场时间",
    "avg_distance_covered" to "场均跑动距离",
    "avg_number_of_sprints" to "场均冲刺",
    "top_speed" to "最高速度",
    "avg_ball_carries_distance" to "场均带球距离",
    "avg_progression" to "场均推进距离",
    "avg_progressive_ball_carries" to "场均向前带球距离",
)

private val FOOTBALL_JSON = Json { ignoreUnknownKeys = true }

private val MATCH_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val KICKOFF_TIME = DateTimeFormatter.ofPattern("HH:mm")
private val MATCH_DATE_LABEL = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val ARTICLE_ID_PATTERN = Regex("(?:news|article)/(\\d+)")
private val MATCH_ID_PATTERN = Regex("(?:game|match)/(\\d+)")
private val TEAM_ID_PATTERN = Regex("team/(\\d+)")
