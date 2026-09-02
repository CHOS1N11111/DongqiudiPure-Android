package io.github.chos1n11111.dongqiudipure.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MatchListEnvelopeDto(
    val list: List<MatchDto>? = null,
    val nextDate: String? = null,
    val prevDate: String? = null,
)

@Serializable
data class MatchDto(
    @SerialName("relate_type") val relateType: String? = null,
    @SerialName("cmp_type") val competitionType: String? = null,
    @SerialName("match_id") val matchId: JsonElement? = null,
    @SerialName("competition_id") val competitionId: JsonElement? = null,
    @SerialName("competition_name") val competitionName: String? = null,
    @SerialName("round_name") val roundName: String? = null,
    @SerialName("gameweek") val gameweek: String? = null,
    @SerialName("team_A_id") val homeTeamId: JsonElement? = null,
    @SerialName("team_A_name") val homeTeamName: String? = null,
    @SerialName("team_A_logo") val homeTeamLogo: String? = null,
    @SerialName("team_B_id") val awayTeamId: JsonElement? = null,
    @SerialName("team_B_name") val awayTeamName: String? = null,
    @SerialName("team_B_logo") val awayTeamLogo: String? = null,
    @SerialName("start_play") val startPlay: String? = null,
    val status: String? = null,
    @SerialName("fs_A") val homeScore: JsonElement? = null,
    @SerialName("fs_B") val awayScore: JsonElement? = null,
    val minute: JsonElement? = null,
    @SerialName("minute_extra") val minuteExtra: JsonElement? = null,
)

@Serializable
data class SeasonDto(
    @SerialName("season_id") val seasonId: JsonElement? = null,
    @SerialName("season_name") val seasonName: String? = null,
)

@Serializable
data class StandingEnvelopeDto(
    val template: String? = null,
    val content: StandingContentDto? = null,
)

@Serializable
data class StandingContentDto(
    val rounds: List<StandingRoundDto>? = null,
)

@Serializable
data class StandingRoundDto(
    val template: String? = null,
    val content: StandingRoundContentDto? = null,
)

@Serializable
data class StandingRoundContentDto(
    val data: List<StandingRowDto>? = null,
    val desc: List<StandingDescriptionDto>? = null,
)

@Serializable
data class StandingRowDto(
    val rank: JsonElement? = null,
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
    @SerialName("matches_total") val matchesTotal: JsonElement? = null,
    @SerialName("matches_won") val matchesWon: JsonElement? = null,
    @SerialName("matches_draw") val matchesDraw: JsonElement? = null,
    @SerialName("matches_lost") val matchesLost: JsonElement? = null,
    @SerialName("goals_pro") val goalsFor: JsonElement? = null,
    @SerialName("goals_against") val goalsAgainst: JsonElement? = null,
    val points: JsonElement? = null,
)

@Serializable
data class StandingDescriptionDto(
    val from: JsonElement? = null,
    val to: JsonElement? = null,
    val text: String? = null,
)
