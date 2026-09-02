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
    @SerialName("hts_A") val homeHalfScore: JsonElement? = null,
    @SerialName("hts_B") val awayHalfScore: JsonElement? = null,
    @SerialName("ps_A") val homePenaltyScore: JsonElement? = null,
    @SerialName("ps_B") val awayPenaltyScore: JsonElement? = null,
    @SerialName("ags_A") val homeAggregateScore: JsonElement? = null,
    @SerialName("ags_B") val awayAggregateScore: JsonElement? = null,
    @SerialName("rank_A") val homeRank: JsonElement? = null,
    @SerialName("rank_B") val awayRank: JsonElement? = null,
    @SerialName("rc_A") val homeRedCards: JsonElement? = null,
    @SerialName("rc_B") val awayRedCards: JsonElement? = null,
    @SerialName("yc_A") val homeYellowCards: JsonElement? = null,
    @SerialName("yc_B") val awayYellowCards: JsonElement? = null,
    @SerialName("corner_A") val homeCorners: JsonElement? = null,
    @SerialName("corner_B") val awayCorners: JsonElement? = null,
    @SerialName("live_tag") val liveTag: String? = null,
    @SerialName("TVList") val tvList: String? = null,
    @SerialName("tips_num") val tipsCount: JsonElement? = null,
)

@Serializable
data class DataMenuEnvelopeDto(
    val data: DataMenuRootDto? = null,
)

@Serializable
data class DataMenuRootDto(
    val list: List<DataMenuCategoryDto>? = null,
)

@Serializable
data class DataMenuCategoryDto(
    val title: String? = null,
    val data: List<DataMenuGroupDto>? = null,
)

@Serializable
data class DataMenuGroupDto(
    val title: String? = null,
    val data: List<DataMenuCompetitionDto>? = null,
)

@Serializable
data class DataMenuCompetitionDto(
    val label: String? = null,
    val logo: String? = null,
    @SerialName("competition_id") val competitionId: JsonElement? = null,
    @SerialName("season_id") val seasonId: JsonElement? = null,
    @SerialName("sub_tabs") val subTabs: List<DataMenuSubTabDto>? = null,
)

@Serializable
data class DataMenuSubTabDto(
    val title: String? = null,
    val type: String? = null,
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

@Serializable
data class RankingTypesEnvelopeDto(
    val template: String? = null,
    val content: RankingTypesContentDto? = null,
)

@Serializable
data class RankingTypesContentDto(
    val data: List<RankingMetricDto>? = null,
)

@Serializable
data class RankingMetricDto(
    val name: String? = null,
    val type: String? = null,
)

@Serializable
data class RankingDetailEnvelopeDto(
    val template: String? = null,
    val content: RankingDetailContentDto? = null,
)

@Serializable
data class RankingDetailContentDto(
    val data: List<RankingRowDto>? = null,
    val header: List<String>? = null,
)

@Serializable
data class RankingRowDto(
    val rank: JsonElement? = null,
    val count: JsonElement? = null,
    @SerialName("row_2") val row2: JsonElement? = null,
    @SerialName("person_id") val personId: JsonElement? = null,
    @SerialName("person_name") val personName: String? = null,
    @SerialName("person_logo") val personLogo: String? = null,
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
)

@Serializable
data class TeamSampleDto(
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_en_name") val teamEnglishName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
    val country: String? = null,
    val city: String? = null,
    val founded: JsonElement? = null,
    @SerialName("venue_name") val venueName: String? = null,
    @SerialName("venue_capacity") val venueCapacity: JsonElement? = null,
    val rank: String? = null,
    @SerialName("market_value") val marketValue: JsonElement? = null,
    val type: String? = null,
    val description: List<TeamDescriptionDto>? = null,
)

@Serializable
data class TeamDescriptionDto(
    val key: String? = null,
    val value: String? = null,
)

@Serializable
data class TeamStatisticDto(
    @SerialName("season_list") val seasonList: List<TeamSeasonOptionDto>? = null,
    val season: TeamSeasonSummaryDto? = null,
    val statistics: TeamStatisticGroupsDto? = null,
    val person: List<TeamKeyPlayerDto>? = null,
)

@Serializable
data class TeamSeasonOptionDto(
    val name: String? = null,
    val current: Boolean? = null,
)

@Serializable
data class TeamSeasonSummaryDto(
    val name: String? = null,
    val rank: JsonElement? = null,
    val matches: TeamRecordDto? = null,
    @SerialName("five_matches") val fiveMatches: TeamRecordDto? = null,
)

@Serializable
data class TeamRecordDto(
    val win: JsonElement? = null,
    val draw: JsonElement? = null,
    val lose: JsonElement? = null,
)

@Serializable
data class TeamStatisticGroupsDto(
    val attack: List<LabeledNumberDto>? = null,
    val organize: List<LabeledNumberDto>? = null,
    val defensive: List<LabeledNumberDto>? = null,
    val discipline: List<LabeledNumberDto>? = null,
)

@Serializable
data class LabeledNumberDto(
    val type: String? = null,
    val number: JsonElement? = null,
)

@Serializable
data class TeamKeyPlayerDto(
    val type: String? = null,
    val person: SimplePersonDto? = null,
    val number: JsonElement? = null,
)

@Serializable
data class SimplePersonDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
)

@Serializable
data class TeamMembersEnvelopeDto(
    val code: JsonElement? = null,
    val data: TeamMembersDataDto? = null,
)

@Serializable
data class TeamMembersDataDto(
    val list: List<TeamMemberGroupDto>? = null,
)

@Serializable
data class TeamMemberGroupDto(
    val title: String? = null,
    val type: String? = null,
    val data: List<TeamMemberDto>? = null,
)

@Serializable
data class TeamMemberDto(
    @SerialName("person_id") val personId: JsonElement? = null,
    @SerialName("person_name") val personName: String? = null,
    @SerialName("person_logo") val personLogo: String? = null,
    val shirtnumber: JsonElement? = null,
    val type: String? = null,
    @SerialName("nationality_name") val nationalityName: String? = null,
    val age: String? = null,
    val statistic: List<Map<String, JsonElement>>? = null,
)

@Serializable
data class TeamScheduleEnvelopeDto(
    val data: List<MatchDto>? = null,
)

@Serializable
data class CompetitionScheduleEnvelopeDto(
    val template: String? = null,
    val content: CompetitionScheduleContentDto? = null,
)

@Serializable
data class CompetitionScheduleContentDto(
    val matches: List<CompetitionScheduleGroupDto>? = null,
)

@Serializable
data class CompetitionScheduleGroupDto(
    val name: String? = null,
    val data: List<MatchDto>? = null,
)

@Serializable
data class PlayerDetailDto(
    @SerialName("base_info") val baseInfo: PlayerBaseInfoDto? = null,
    @SerialName("honor_info") val honorInfo: List<PlayerHonorDto>? = null,
    @SerialName("transfer_info") val transferInfo: List<PlayerTransferDto>? = null,
    @SerialName("injury_records") val injuryRecords: PlayerInjuryRecordsDto? = null,
)

@Serializable
data class PlayerBaseInfoDto(
    @SerialName("person_id") val personId: JsonElement? = null,
    @SerialName("person_name") val personName: String? = null,
    @SerialName("person_en_name") val personEnglishName: String? = null,
    @SerialName("person_logo") val personLogo: String? = null,
    val nationality: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val height: JsonElement? = null,
    val weight: JsonElement? = null,
    val foot: String? = null,
    val position: String? = null,
    val age: String? = null,
    @SerialName("market_value") val marketValue: JsonElement? = null,
    val contract: String? = null,
    @SerialName("team_info") val teamInfo: PlayerTeamInfoDto? = null,
)

@Serializable
data class PlayerTeamInfoDto(
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
    val shirtnumber: JsonElement? = null,
    val role: String? = null,
)

@Serializable
data class PlayerHonorDto(
    val name: String? = null,
    val logo: String? = null,
    val times: JsonElement? = null,
    @SerialName("honor_list") val honorList: List<PlayerHonorSeasonDto>? = null,
)

@Serializable
data class PlayerHonorSeasonDto(
    @SerialName("season_name") val seasonName: String? = null,
)

@Serializable
data class PlayerTransferDto(
    val type: String? = null,
    val money: String? = null,
    @SerialName("announced_date") val announcedDate: String? = null,
    @SerialName("from_team_id") val fromTeamId: JsonElement? = null,
    @SerialName("from_club_name") val fromTeamName: String? = null,
    @SerialName("from_team_logo") val fromTeamLogo: String? = null,
    @SerialName("to_team_id") val toTeamId: JsonElement? = null,
    @SerialName("to_club_name") val toTeamName: String? = null,
    @SerialName("to_team_logo") val toTeamLogo: String? = null,
)

@Serializable
data class PlayerInjuryRecordsDto(
    val history: List<PlayerInjuryDto>? = null,
)

@Serializable
data class PlayerInjuryDto(
    val injury: String? = null,
    @SerialName("date_from") val dateFrom: String? = null,
    @SerialName("date_until") val dateUntil: String? = null,
    @SerialName("games_missed") val gamesMissed: JsonElement? = null,
    val teams: List<PlayerInjuryTeamDto>? = null,
)

@Serializable
data class PlayerInjuryTeamDto(
    val name: String? = null,
)

@Serializable
data class PlayerStatisticsDto(
    val total: List<PlayerCareerDto>? = null,
    val league: List<PlayerCareerDto>? = null,
    val cup: List<PlayerCareerDto>? = null,
    val international: List<PlayerCareerDto>? = null,
)

@Serializable
data class PlayerCareerDto(
    val season: PlayerCareerSeasonDto? = null,
    val team: PlayerCareerTeamDto? = null,
    val list: List<PlayerCareerValueDto>? = null,
)

@Serializable
data class PlayerCareerSeasonDto(
    val name: String? = null,
)

@Serializable
data class PlayerCareerTeamDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
)

@Serializable
data class PlayerCareerValueDto(
    val title: String? = null,
    val value: JsonElement? = null,
)

@Serializable
data class PlayerAbilityEnvelopeDto(
    val data: PlayerAbilityDto? = null,
)

@Serializable
data class PlayerAbilityDto(
    val average: PlayerAbilityAverageDto? = null,
    val redar: List<PlayerAbilityValueDto>? = null,
    val version: String? = null,
)

@Serializable
data class PlayerAbilityAverageDto(
    val `val`: JsonElement? = null,
)

@Serializable
data class PlayerAbilityValueDto(
    val name: String? = null,
    val `val`: JsonElement? = null,
)
