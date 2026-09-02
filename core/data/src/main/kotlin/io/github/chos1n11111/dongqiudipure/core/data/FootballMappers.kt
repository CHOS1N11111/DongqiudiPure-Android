package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.CompetitionId
import io.github.chos1n11111.dongqiudipure.core.model.CompetitionRef
import io.github.chos1n11111.dongqiudipure.core.model.MatchId
import io.github.chos1n11111.dongqiudipure.core.model.MatchStatus
import io.github.chos1n11111.dongqiudipure.core.model.MatchSummary
import io.github.chos1n11111.dongqiudipure.core.model.StandingRow
import io.github.chos1n11111.dongqiudipure.core.model.StandingTable
import io.github.chos1n11111.dongqiudipure.core.model.StandingZone
import io.github.chos1n11111.dongqiudipure.core.model.TeamId
import io.github.chos1n11111.dongqiudipure.core.model.TeamRef
import io.github.chos1n11111.dongqiudipure.core.network.dto.MatchDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingDescriptionDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingEnvelopeDto
import io.github.chos1n11111.dongqiudipure.core.network.dto.StandingRowDto
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

internal fun MatchDto.toDomain(zoneId: ZoneId): DatedMatch {
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
    val competition = CompetitionRef(
        id = CompetitionId(competitionId.scalarFootball().requiredFootball()),
        name = competitionName.requiredFootball(),
        roundLabel = gameweek?.trim()?.takeIf(String::isNotEmpty)?.let { "第${it}轮" }
            ?: roundName?.trim()?.takeIf(String::isNotEmpty),
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
        ),
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
