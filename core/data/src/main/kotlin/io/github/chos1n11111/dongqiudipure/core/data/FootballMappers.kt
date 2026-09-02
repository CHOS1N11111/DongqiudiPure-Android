package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionCatalogGroup
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.CareerEntry
import io.github.chos1n11111.dongqiudipure.core.model.FormResult
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.PlayerAbility
import io.github.chos1n11111.dongqiudipure.core.model.PlayerHonor
import io.github.chos1n11111.dongqiudipure.core.model.PlayerId
import io.github.chos1n11111.dongqiudipure.core.model.PlayerInjury
import io.github.chos1n11111.dongqiudipure.core.model.PlayerOverview
import io.github.chos1n11111.dongqiudipure.core.model.PlayerPosition
import io.github.chos1n11111.dongqiudipure.core.model.PlayerProfile
import io.github.chos1n11111.dongqiudipure.core.model.PlayerRef
import io.github.chos1n11111.dongqiudipure.core.model.PlayerSeasonStat
import io.github.chos1n11111.dongqiudipure.core.model.PlayerTransfer
import io.github.chos1n11111.dongqiudipure.core.model.RankingMetric
import io.github.chos1n11111.dongqiudipure.core.model.RankingRow
import io.github.chos1n11111.dongqiudipure.core.model.SquadMember
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamKeyPlayer
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatisticCategory
import io.github.chos1n11111.dongqiudipure.core.model.TeamStatistics
import io.github.chos1n11111.dongqiudipure.core.model.TeamType
import io.github.chos1n11111.dongqiudipure.core.model.TeamProfile
import io.github.chos1n11111.dongqiudipure.core.model.StatisticRankingTable
import io.github.chos1n11111.dongqiudipure.core.network.dto.DataMenuEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.CompetitionScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerAbilityEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerDetailDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerStatisticsDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.PlayerTransferDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingDetailEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.RankingTypesEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingDescriptionDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingRowDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamMembersEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamSampleDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamScheduleEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.TeamStatisticDto
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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
        val value = row.count.scalarFootball().displayable()
            ?: row.row2.scalarFootball().displayable()
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
            )
        } else {
            val teamId = row.teamId.scalarFootball().displayable() ?: return@mapNotNull null
            val name = row.teamName.displayable() ?: return@mapNotNull null
            val team = TeamRef(TeamId(teamId.fullDqdId()), name, safeFootballMediaUrl(row.teamLogo))
            RankingRow(rank, name, team.crestUrl, value, team = team)
        }
    }
    return StatisticRankingTable(competition, seasonLabel, valueLabel, rows)
}

internal fun TeamScheduleEnvelopeDto.toDomain(zoneId: ZoneId): List<MatchSummary> =
    data.orEmpty()
        .asSequence()
        .filter { it.relateType == null || it.relateType == "match" }
        .filter { it.competitionType == null || it.competitionType == "soccer" }
        .map { it.toDomain(zoneId) }
        .sortedBy { it.kickoff }
        .map { it.match }
        .toList()

internal fun TeamSampleDto.toDomain(): TeamProfile {
    val id = teamId.scalarFootball().requiredFootball().fullDqdId()
    val descriptions = description.orEmpty().associate { it.key.orEmpty() to it.value.orEmpty() }
    return TeamProfile(
        id = TeamId(id),
        name = teamName.requiredFootball(),
        crestUrl = safeFootballMediaUrl(teamLogo),
        englishName = teamEnglishName.displayable(),
        country = country.displayable(),
        city = city.displayable(),
        competitionName = descriptions.keys.firstOrNull { it.endsWith("排名") }?.removeSuffix("排名"),
        venue = venueName.displayable(),
        venueCapacity = venueCapacity.scalarFootball().displayable(),
        foundedLabel = founded.scalarFootball().displayable(),
        rankLabel = rank.displayable(),
        marketValueLabel = marketValue.scalarFootball().formatEuroValue(),
        type = when (type?.lowercase()) {
            "club" -> TeamType.Club
            "national", "nation" -> TeamType.National
            else -> TeamType.Unknown
        },
        recentForm = descriptions["最近5场"].toForm(),
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
        seasonLabel = seasonList.orEmpty().firstOrNull { it.current == true }?.name.displayable()
            ?: seasonList.orEmpty().firstOrNull()?.name.displayable(),
        rankLabel = season?.rank.scalarFootball().displayable()?.let { "第${it}" },
        recordLabel = recordLabel,
        // The endpoint only returns aggregate win/draw/loss counts here, not match order.
        recentForm = emptyList(),
        categories = categories,
        keyPlayers = keyPlayers,
    )
}

internal fun TeamMembersEnvelopeDto.toDomain(): List<SquadMember> {
    if (code.optionalFootballInt() != 0) throw ContractViolation()
    return data?.list.orEmpty().flatMap { group ->
        group.data.orEmpty().mapNotNull { member ->
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
                    member.type.displayable() ?: group.title.displayable()
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
            )
        }
    }
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
        )
    }
    return PlayerOverview(profile, honors, transfers, injuries)
}

internal fun PlayerStatisticsDto.toDomain(): List<CareerEntry> = total.orEmpty().mapNotNull { entry ->
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
    val rows = rounds.flatMap { round ->
        val roundContent = round.content ?: throw ContractViolation()
        val descriptions = roundContent.desc.orEmpty()
        (roundContent.data ?: throw ContractViolation()).mapIndexed { index, row ->
            row.toDomain(zoneAt(index, descriptions))
        }
    }
    return StandingTable(
        competition = competition,
        seasonLabel = seasonLabel.requiredFootball(),
        rows = rows,
    )
}

private fun StandingRowDto.toDomain(zone: StandingZone?): StandingRow {
    val goalsForValue = goalsFor.optionalFootballInt()
    val goalsAgainstValue = goalsAgainst.optionalFootballInt()
    return StandingRow(
        rank = rank.requiredFootballInt(),
        team = TeamRef(
            id = TeamId(teamId.scalarFootball().requiredFootball()),
            name = teamName.requiredFootball(),
            crestUrl = safeFootballMediaUrl(teamLogo),
        ),
        played = matchesTotal.optionalFootballInt(),
        won = matchesWon.optionalFootballInt(),
        drawn = matchesDraw.optionalFootballInt(),
        lost = matchesLost.optionalFootballInt(),
        goalDifference = if (goalsForValue != null && goalsAgainstValue != null) {
            goalsForValue - goalsAgainstValue
        } else {
            null
        },
        points = points.optionalFootballInt(),
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

private val MATCH_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val KICKOFF_TIME = DateTimeFormatter.ofPattern("HH:mm")
