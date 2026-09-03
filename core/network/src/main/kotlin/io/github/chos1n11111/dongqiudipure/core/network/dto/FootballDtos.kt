package io.github.chos1n11111.dongqiudipure.core.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

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
    @SerialName("match_title") val matchTitle: String? = null,
    val scheme: String? = null,
    val goals: JsonElement? = null,
    val assists: JsonElement? = null,
    val cards: JsonElement? = null,
    val rating: JsonElement? = null,
    @SerialName("dqd_rating") val dqdRating: JsonElement? = null,
    @SerialName("team_A_events") val homeEvents: List<MatchListEventDto>? = null,
    @SerialName("team_B_events") val awayEvents: List<MatchListEventDto>? = null,
)

@Serializable
data class MatchListEventDto(
    val title: String? = null,
    val code: String? = null,
    val sort: JsonElement? = null,
)

@Serializable
data class MatchDetailEnvelopeDto(
    val matchSample: MatchDto? = null,
    val matchFormation: MatchFormationSummaryDto? = null,
)

@Serializable
data class MatchFormationSummaryDto(
    @SerialName("attendance_rate") val attendance: String? = null,
    val referee: String? = null,
    val weather: String? = null,
    val temperature: String? = null,
    val field: String? = null,
)

@Serializable
data class MatchOverviewDto(
    val events: JsonElement? = null,
    val statistics: JsonElement? = null,
    val archive: JsonElement? = null,
    val gifCollection: List<MatchMediaDto>? = null,
    val tendencies: JsonElement? = null,
    val highscorepersons: JsonElement? = null,
)

@Serializable
data class MatchNewsEnvelopeDto(
    val data: List<MatchNewsItemDto>? = null,
)

@Serializable
data class MatchNewsItemDto(
    val id: JsonElement? = null,
    val title: String? = null,
    val scheme: String? = null,
    val thumb: String? = null,
    val cover: JsonElement? = null,
    @SerialName("comments_total") val commentsTotal: JsonElement? = null,
)

@Serializable
data class MatchEventMinuteDto(
    val minute: String? = null,
    val teamAEvents: List<MatchEventDto>? = null,
    val teamBEvents: List<MatchEventDto>? = null,
    val neutralEvents: List<MatchEventDto>? = null,
)

@Serializable
data class MatchEventDto(
    val minute: String? = null,
    @SerialName("minute_extra") val minuteExtra: String? = null,
    val person: String? = null,
    @SerialName("person_id") val personId: JsonElement? = null,
    val score: String? = null,
    val reason: String? = null,
    val code: String? = null,
)

@Serializable
data class MatchStatisticsDto(
    @SerialName("team_A") val home: MatchStatisticTeamDto? = null,
    @SerialName("team_B") val away: MatchStatisticTeamDto? = null,
    val list: List<MatchStatisticItemDto>? = null,
)

@Serializable
data class MatchStatisticTeamDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
)

@Serializable
data class MatchStatisticItemDto(
    @SerialName("en_type") val id: String? = null,
    val type: String? = null,
    @SerialName("team_A") val home: MatchStatisticValueDto? = null,
    @SerialName("team_B") val away: MatchStatisticValueDto? = null,
)

@Serializable
data class MatchStatisticValueDto(
    val value: JsonElement? = null,
    val per: JsonElement? = null,
)

@Serializable
data class MatchArchiveDto(
    val title: String? = null,
    val scheme: String? = null,
    val thumb: String? = null,
    val commentsTotal: JsonElement? = null,
)

@Serializable
data class MatchMediaDto(
    val id: JsonElement? = null,
    val title: String? = null,
    val thumb: String? = null,
    val comments_total: JsonElement? = null,
    val time: JsonElement? = null,
    val score: String? = null,
)

@Serializable
data class MatchTendenciesDto(
    val data: List<MatchTendencyPointDto>? = null,
)

@Serializable
data class MatchTendencyPointDto(
    val x: JsonElement? = null,
    val y: JsonElement? = null,
)

@Serializable
data class MatchHighScorePersonsDto(
    @SerialName("team_A_person") val home: MatchLineupPlayerDto? = null,
    @SerialName("team_B_person") val away: MatchLineupPlayerDto? = null,
)

@Serializable
data class MatchLineupEnvelopeDto(
    val base: MatchLineupBaseDto? = null,
    val persons: MatchLineupTeamsDto? = null,
    val forecasts: MatchLineupTeamsDto? = null,
    val sideline: MatchSidelineDto? = null,
)

@Serializable
data class MatchLineupBaseDto(
    @SerialName("attendance_rate") val attendance: String? = null,
    val weather: String? = null,
    val temperature: String? = null,
    @SerialName("weather_info") val weatherInfo: MatchLineupWeatherInfoDto? = null,
    val field: String? = null,
    val referee: String? = null,
)

@Serializable
data class MatchLineupWeatherInfoDto(
    val altitude: JsonElement? = null,
)

@Serializable
data class MatchLineupTeamsDto(
    @SerialName("team_A") val home: MatchLineupTeamDto? = null,
    @SerialName("team_B") val away: MatchLineupTeamDto? = null,
)

@Serializable
data class MatchLineupTeamDto(
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
    @SerialName("team_market_value") val teamMarketValue: String? = null,
    @SerialName("team_age") val teamAge: String? = null,
    @SerialName("team_coach") val coach: String? = null,
    @SerialName("team_coach_logo") val coachLogo: String? = null,
    @SerialName("team_coach_role") val coachRole: String? = null,
    val formation: String? = null,
    val lineups: List<MatchLineupPlayerDto>? = null,
    val sub: List<MatchLineupPlayerDto>? = null,
)

@Serializable
data class MatchLineupPlayerDto(
    @SerialName("person_id") val personId: JsonElement? = null,
    val person: String? = null,
    val logo: String? = null,
    val rate: JsonElement? = null,
    @SerialName("is_mvp") val isMvp: JsonElement? = null,
    val captain: JsonElement? = null,
    val shirtnumber: JsonElement? = null,
    val position: String? = null,
    @SerialName("position_x") val positionX: JsonElement? = null,
    @SerialName("position_y") val positionY: JsonElement? = null,
    @SerialName("nationality_name") val nationalityName: String? = null,
    @Serializable(with = MatchLineupEventsSerializer::class)
    val events: List<MatchLineupPlayerEventDto> = emptyList(),
)

@Serializable
data class MatchLineupPlayerEventDto(
    val type: String? = null,
    val minute: JsonElement? = null,
    @SerialName("minute_extra") val minuteExtra: JsonElement? = null,
    @SerialName("event_pic") val eventPic: String? = null,
)

private object MatchLineupEventsSerializer : KSerializer<List<MatchLineupPlayerEventDto>> {
    private val delegate = ListSerializer(MatchLineupPlayerEventDto.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<MatchLineupPlayerEventDto> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Match lineup events require JSON")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> jsonDecoder.json.decodeFromJsonElement(delegate, element)
            JsonNull -> emptyList()
            is JsonPrimitive -> if (element.content.isBlank()) {
                emptyList()
            } else {
                throw SerializationException("Unexpected match lineup events value")
            }
            else -> throw SerializationException("Unexpected match lineup events value")
        }
    }

    override fun serialize(encoder: Encoder, value: List<MatchLineupPlayerEventDto>) {
        encoder.encodeSerializableValue(delegate, value)
    }
}

@Serializable
data class MatchSidelineDto(
    @SerialName("team_A") val home: List<MatchSidelinePlayerDto>? = null,
    @SerialName("team_B") val away: List<MatchSidelinePlayerDto>? = null,
)

@Serializable
data class MatchSidelinePlayerDto(
    val person: String? = null,
    @SerialName("person_name") val personName: String? = null,
    val name: String? = null,
    val reason: String? = null,
    val injury: String? = null,
)

@Serializable
data class MatchAnalysisDto(
    val battle_history: MatchAnalysisListDto? = null,
    val recent_record: MatchAnalysisSidesDto? = null,
    val feature_matches: MatchAnalysisSidesDto? = null,
    val sideline: MatchSidelineDto? = null,
)

@Serializable
data class MatchAnalysisListDto(
    val name: String? = null,
    val list: List<MatchAnalysisMatchDto>? = null,
)

@Serializable
data class MatchAnalysisSidesDto(
    val name: String? = null,
    @SerialName("team_A") val home: List<MatchAnalysisMatchDto>? = null,
    @SerialName("team_B") val away: List<MatchAnalysisMatchDto>? = null,
)

@Serializable
data class MatchAnalysisMatchDto(
    val date: String? = null,
    val year: String? = null,
    val competition: String? = null,
    val competition_name: String? = null,
    @SerialName("team_A_name") val homeName: String? = null,
    @SerialName("team_A_logo") val homeLogo: String? = null,
    @SerialName("team_A_href") val homeHref: String? = null,
    @SerialName("team_B_name") val awayName: String? = null,
    @SerialName("team_B_logo") val awayLogo: String? = null,
    @SerialName("team_B_href") val awayHref: String? = null,
    val home: String? = null,
    val away: String? = null,
    val score: String? = null,
    val start_time: String? = null,
    val match_href: String? = null,
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
    val id: JsonElement? = null,
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
    val name: String? = null,
    val data: List<JsonObject>? = null,
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
    @SerialName("row_1") val row1: JsonElement? = null,
    @SerialName("row_2") val row2: JsonElement? = null,
    @SerialName("row_3") val row3: JsonElement? = null,
    @SerialName("row_4") val row4: JsonElement? = null,
    @SerialName("row_5") val row5: JsonElement? = null,
    @SerialName("row_6") val row6: JsonElement? = null,
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
data class TeamDetailDto(
    @SerialName("base_info") val baseInfo: TeamDetailBaseInfoDto? = null,
    @SerialName("trophy_info") val trophyInfo: List<TeamTrophyDto>? = null,
    @SerialName("honor_info") val honorInfo: List<TeamHonorDto>? = null,
    @SerialName("history_info") val historyInfo: JsonElement? = null,
    @SerialName("archive_info") val archiveInfo: JsonElement? = null,
    @SerialName("history_coach") val historyCoach: List<TeamHistoricalCoachDto>? = null,
    @SerialName("base_info_v_1") val facts: List<TeamFactDto>? = null,
    @SerialName("history_rank") val historyRank: TeamHistoryRankDto? = null,
    @SerialName("goals_info") val goalsInfo: List<TeamRecordLeaderDto>? = null,
    @SerialName("apps_info") val appsInfo: List<TeamRecordLeaderDto>? = null,
)

@Serializable
data class TeamDetailBaseInfoDto(
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
    val address: String? = null,
    val telephone: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    val description: JsonElement? = null,
)

@Serializable
data class TeamFactDto(
    val type: String? = null,
    val value: String? = null,
)

@Serializable
data class TeamTrophyDto(
    @SerialName("competition_name") val competitionName: String? = null,
    @SerialName("trophy_img") val trophyImage: String? = null,
    val times: JsonElement? = null,
    val lists: List<TeamTrophySeasonDto>? = null,
)

@Serializable
data class TeamTrophySeasonDto(
    @SerialName("season_name") val seasonName: String? = null,
)

@Serializable
data class TeamHonorDto(
    val name: String? = null,
    val logo: String? = null,
    val times: JsonElement? = null,
    @SerialName("honor_list") val honorList: List<PlayerHonorSeasonDto>? = null,
)

@Serializable
data class TeamHistoricalCoachDto(
    val win: JsonElement? = null,
    val draw: JsonElement? = null,
    val loss: JsonElement? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("win_rate") val winRate: JsonElement? = null,
    val time: String? = null,
    val person: SimplePersonDto? = null,
)

@Serializable
data class TeamHistoryRankDto(
    val data: List<TeamHistoryRankEntryDto>? = null,
    val season: List<String>? = null,
)

@Serializable
data class TeamHistoryRankEntryDto(
    val rank: JsonElement? = null,
    @SerialName("competition_clubs") val competitionClubs: JsonElement? = null,
)

@Serializable
data class TeamRecordLeaderDto(
    val rank: JsonElement? = null,
    val count: JsonElement? = null,
    val person: TeamRecordPersonDto? = null,
)

@Serializable
data class TeamRecordPersonDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    val nationality: TeamRecordNationalityDto? = null,
)

@Serializable
data class TeamRecordNationalityDto(
    val name: String? = null,
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
    val characteristics: FootballCharacteristicsDto? = null,
    @SerialName("ranking_trend") val rankingTrend: TeamRankingTrendDto? = null,
)

@Serializable
data class TeamRankingTrendDto(
    val weeks: List<TeamRankingTrendWeekDto>? = null,
)

@Serializable
data class TeamRankingTrendWeekDto(
    val week: JsonElement? = null,
    val rank: JsonElement? = null,
    @SerialName("window_start") val windowStart: String? = null,
    @SerialName("window_end") val windowEnd: String? = null,
    val matches: List<TeamRankingTrendMatchDto>? = null,
)

@Serializable
data class TeamRankingTrendMatchDto(
    @SerialName("home_team_id") val homeTeamId: JsonElement? = null,
    @SerialName("home_team_name") val homeTeamName: String? = null,
    @SerialName("home_team_logo") val homeTeamLogo: String? = null,
    @SerialName("away_team_id") val awayTeamId: JsonElement? = null,
    @SerialName("away_team_name") val awayTeamName: String? = null,
    @SerialName("away_team_logo") val awayTeamLogo: String? = null,
    @SerialName("home_score") val homeScore: JsonElement? = null,
    @SerialName("away_score") val awayScore: JsonElement? = null,
)

@Serializable
data class FootballCharacteristicsDto(
    val styles: List<String>? = null,
    val strength: FootballCharacteristicLevelsDto? = null,
    val weakness: FootballCharacteristicLevelsDto? = null,
)

@Serializable
data class FootballCharacteristicLevelsDto(
    @SerialName("very_strong") val veryStrong: List<String>? = null,
    val strong: List<String>? = null,
    val weak: List<String>? = null,
    @SerialName("very_weak") val veryWeak: List<String>? = null,
)

@Serializable
data class TeamSeasonOptionDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val url: String? = null,
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
    val seasons: List<TeamSeasonOptionDto>? = null,
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
    val statistics: List<String>? = null,
    @SerialName("show_type") val showType: JsonElement? = null,
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
    @SerialName("weekly_salary") val weeklySalary: String? = null,
    @SerialName("transfer_data") val transferData: JsonElement? = null,
    @SerialName("captain_logo") val captainLogo: String? = null,
)

@Serializable
data class TeamScheduleEnvelopeDto(
    @SerialName("season_list") val seasonList: List<TeamSeasonOptionDto>? = null,
    val data: List<MatchDto>? = null,
)

@Serializable
data class TeamTransferEnvelopeDto(
    val data: TeamTransferDataDto? = null,
    val errCode: JsonElement? = null,
    val message: String? = null,
)

@Serializable
data class TeamTransferDataDto(
    val windows: List<TeamTransferWindowDto>? = null,
    val transfer: Map<String, List<TeamTransferGroupDto>>? = null,
)

@Serializable
data class TeamTransferWindowDto(
    val name: String? = null,
    val url: String? = null,
    val current: Boolean? = null,
)

@Serializable
data class TeamTransferGroupDto(
    val title: String? = null,
    val value: String? = null,
    val data: List<TeamTransferEntryDto>? = null,
)

@Serializable
data class TeamTransferEntryDto(
    val person: TeamTransferPersonDto? = null,
    @SerialName("from_team") val fromTeam: TeamTransferTeamDto? = null,
    @SerialName("to_team") val toTeam: TeamTransferTeamDto? = null,
)

@Serializable
data class TeamTransferPersonDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
    val age: String? = null,
    val role: String? = null,
    val nation: String? = null,
    @SerialName("value_money") val valueMoney: String? = null,
    @SerialName("start_date") val startDate: String? = null,
)

@Serializable
data class TeamTransferTeamDto(
    val id: JsonElement? = null,
    val name: String? = null,
    val logo: String? = null,
)

@Serializable
data class EntityFeedEnvelopeDto(
    val code: JsonElement? = null,
    val message: String? = null,
    val data: FeedResponseDto? = null,
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
    @SerialName("history_market_values")
    val historyMarketValues: Map<String, List<PlayerMarketValueDto>>? = null,
    @SerialName("base_info_v_1") val facts: List<PlayerProfileFactDto>? = null,
    @SerialName("character_info") val characterInfo: FootballCharacteristicsDto? = null,
    @SerialName("player_career_info") val playerCareerInfo: List<PlayerCareerSummaryDto>? = null,
    @SerialName("player_nation_career_info")
    val playerNationCareerInfo: List<PlayerCareerSummaryDto>? = null,
)

@Serializable
data class PlayerProfileFactDto(
    val type: String? = null,
    val value: String? = null,
)

@Serializable
data class PlayerCareerSummaryDto(
    @SerialName("team_id") val teamId: JsonElement? = null,
    @SerialName("team_name") val teamName: String? = null,
    @SerialName("team_logo") val teamLogo: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    val appearance: JsonElement? = null,
    val goals: JsonElement? = null,
    val assist: JsonElement? = null,
    @SerialName("goals_conceded") val goalsConceded: JsonElement? = null,
    @SerialName("clean_sheets") val cleanSheets: JsonElement? = null,
)

@Serializable
data class PlayerMarketValueDto(
    @SerialName("team_info") val teamInfo: PlayerCareerTeamDto? = null,
    @SerialName("record_date") val recordDate: String? = null,
    @SerialName("market_value") val marketValue: JsonElement? = null,
    @SerialName("market_value_text") val marketValueText: String? = null,
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
    val nickname: String? = null,
    @SerialName("other_nationality") val otherNationality: List<String>? = null,
    @SerialName("weekly_salary") val weeklySalary: JsonElement? = null,
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
    val days: JsonElement? = null,
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
    @SerialName("tabs_default") val tabsDefault: String? = null,
)

@Serializable
data class PlayerCareerDto(
    val id: JsonElement? = null,
    val competition: PlayerCompetitionDto? = null,
    val season: PlayerCareerSeasonDto? = null,
    val team: PlayerCareerTeamDto? = null,
    val list: List<PlayerCareerValueDto>? = null,
    @SerialName("base_info") val baseInfo: JsonObject? = null,
    val attack: JsonObject? = null,
    val pass: JsonObject? = null,
    val defense: JsonObject? = null,
    val discipline: JsonObject? = null,
    val running: JsonObject? = null,
    val descriptions: List<String>? = null,
)

@Serializable
data class PlayerCompetitionDto(
    val id: JsonElement? = null,
    val name: String? = null,
    @SerialName("short_name") val shortName: String? = null,
    val logo: String? = null,
)

@Serializable
data class PlayerCareerSeasonDto(
    val id: JsonElement? = null,
    @SerialName("season_id") val seasonId: JsonElement? = null,
    val name: String? = null,
    @SerialName("current_season") val currentSeason: JsonElement? = null,
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

@Serializable
data class PlayerMatchesEnvelopeDto(
    val matches: List<MatchDto>? = null,
    val page: JsonElement? = null,
    @SerialName("total_page") val totalPage: JsonElement? = null,
    val limit: JsonElement? = null,
)

@Serializable
data class PlayerHeatMapDto(
    val heatmap: List<PlayerHeatPointDto>? = null,
    val direction: String? = null,
)

@Serializable
data class PlayerHeatPointDto(
    val x: JsonElement? = null,
    val y: JsonElement? = null,
)

@Serializable
data class PlayerShotMapDto(
    val type: String? = null,
    val shots: List<PlayerShotDto>? = null,
)

@Serializable
data class PlayerShotDto(
    val minute: JsonElement? = null,
    @SerialName("minute_extra") val minuteExtra: JsonElement? = null,
    @SerialName("start_x") val startX: JsonElement? = null,
    @SerialName("start_y") val startY: JsonElement? = null,
    @SerialName("outcome_text") val outcomeText: String? = null,
    @SerialName("situation_text") val situationText: String? = null,
    @SerialName("shot_type_text") val shotTypeText: String? = null,
    val xg: JsonElement? = null,
    val total: JsonElement? = null,
    val goals: JsonElement? = null,
    @SerialName("on_target") val onTarget: JsonElement? = null,
    @SerialName("off_target") val offTarget: JsonElement? = null,
)
