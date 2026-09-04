package io.github.chos1n11111.dongqiudipure.core.model

/** 球队引用。列表与比分头只需要这些字段，完整资料由 Repository 按 ID 加载。 */
data class TeamRef(
    val id: TeamId,
    val name: String,
    val crestUrl: String?,
)

data class CompetitionRef(
    val id: CompetitionId,
    val name: String,
    /** 如「第 4 轮」。服务端未提供时为 null。 */
    val roundLabel: String?,
    val logoUrl: String? = null,
    /** 懂球帝 data_menu 中用于 data_ranking deep link 的目录 ID。 */
    val catalogId: String? = null,
)

data class MatchListEvent(
    val label: String,
    val code: String?,
)

data class MatchSummary(
    val id: MatchId,
    val competition: CompetitionRef,
    val home: TeamRef,
    val away: TeamRef,
    /** 比分。null 表示无比分可用 —— 未开始、延期或服务端未提供，三者由 [status] 区分。 */
    val homeScore: Int?,
    val awayScore: Int?,
    val status: MatchStatus,
    val homeHalfScore: Int? = null,
    val awayHalfScore: Int? = null,
    val homePenaltyScore: Int? = null,
    val awayPenaltyScore: Int? = null,
    val homeAggregateScore: Int? = null,
    val awayAggregateScore: Int? = null,
    val homeRank: String? = null,
    val awayRank: String? = null,
    val homeRedCards: Int? = null,
    val awayRedCards: Int? = null,
    val homeYellowCards: Int? = null,
    val awayYellowCards: Int? = null,
    val homeCorners: Int? = null,
    val awayCorners: Int? = null,
    val liveLabel: String? = null,
    val tipsCount: Int? = null,
    val kickoffLabel: String? = null,
    val dateLabel: String? = null,
    val matchInfoLabel: String? = null,
    val homeEvents: List<MatchListEvent> = emptyList(),
    val awayEvents: List<MatchListEvent> = emptyList(),
)

/**
 * 比赛事件。
 *
 * [MatchEventKind.Unknown] 保留服务端原值，新增事件类型时不丢事件也不崩溃
 * （ARCHITECTURE.md §5.2、PLAN.md M5）。
 */
data class MatchEvent(
    val minuteLabel: String,
    val kind: MatchEventKind,
    val primaryName: String,
    /** 助攻者、被换下球员等。 */
    val secondaryName: String?,
    /** 该事件后的比分，如 "2-1"。仅进球事件有值。 */
    val scoreAfter: String?,
    val isHome: Boolean,
)

sealed interface MatchEventKind {
    data object Goal : MatchEventKind
    data object OwnGoal : MatchEventKind
    data object PenaltyGoal : MatchEventKind
    data object YellowCard : MatchEventKind
    data object RedCard : MatchEventKind
    data object SecondYellow : MatchEventKind
    data object Substitution : MatchEventKind
    data object VarReview : MatchEventKind

    /** 当前版本不认识的事件类型。降级为中性图标 + 原始文案，不丢弃。 */
    data class Unknown(val rawValue: String) : MatchEventKind
}

/**
 * 一项技术统计。
 *
 * 指标集合由服务端驱动：新增指标自动出现，不需要改客户端；
 * 缺失指标降级为 null，不写死一组固定统计项（PLAN.md M5、ARCHITECTURE.md §5.2）。
 */
data class StatItem(
    val id: String,
    val name: String,
    /** 已格式化的展示值，如 "61%"、"14"。null = 该赛事未提供此项，不等于 0。 */
    val homeValue: String?,
    val awayValue: String?,
    /** 对比条的占比。任一侧缺失时为 null，此时不绘制实心条。 */
    val homeFraction: Float?,
    val awayFraction: Float?,
    val displayOrder: Int,
)

data class MatchInfo(
    val venue: String?,
    val referee: String?,
    val weather: String?,
    val temperature: String?,
    val attendance: String?,
    val altitude: String? = null,
)

data class MatchArticle(
    val articleId: ArticleId,
    val title: String,
    val thumbnailUrl: String?,
    val commentCount: Int?,
    val minuteLabel: String? = null,
    val scoreLabel: String? = null,
)

data class MatchMomentumPoint(
    val minute: Int,
    val value: Float,
)

data class MatchRating(
    val player: PlayerRef,
    val team: TeamRef,
    val ratingLabel: String,
    val isMvp: Boolean,
)

data class MatchOverview(
    val events: List<MatchEvent>,
    val statistics: List<StatItem>,
    val report: MatchArticle?,
    val highlights: List<MatchArticle>,
    val relatedNews: List<MatchArticle>,
    val momentum: List<MatchMomentumPoint>,
    val topRatings: List<MatchRating>,
)

data class AnalysisMatch(
    val matchId: MatchId?,
    val dateLabel: String?,
    val competitionName: String?,
    val homeTeamId: TeamId?,
    val homeName: String,
    val homeLogoUrl: String?,
    val awayTeamId: TeamId?,
    val awayName: String,
    val awayLogoUrl: String?,
    val scoreLabel: String?,
)

data class MatchAnalysis(
    val headToHeadTitle: String?,
    val headToHead: List<AnalysisMatch>,
    val recentTitle: String?,
    val homeRecent: List<AnalysisMatch>,
    val awayRecent: List<AnalysisMatch>,
    val futureTitle: String?,
    val homeFuture: List<AnalysisMatch>,
    val awayFuture: List<AnalysisMatch>,
    val homeAbsentees: List<Absentee> = emptyList(),
    val awayAbsentees: List<Absentee> = emptyList(),
)

/** 近期战绩的单场结果。 */
enum class FormResult { Win, Draw, Loss }

data class TeamProfile(
    val id: TeamId,
    val name: String,
    val crestUrl: String?,
    val englishName: String? = null,
    val country: String? = null,
    val city: String? = null,
    val competitionName: String?,
    val venue: String?,
    val venueCapacity: String? = null,
    val foundedLabel: String?,
    val rankLabel: String? = null,
    val marketValueLabel: String? = null,
    val leagueRankLabel: String? = null,
    val leagueRecordLabel: String? = null,
    val type: TeamType = TeamType.Unknown,
    val recentForm: List<FormResult>,
    val facts: List<TeamFact> = emptyList(),
    val honors: List<TeamHonor> = emptyList(),
    val historicalCoaches: List<HistoricalCoach> = emptyList(),
    val rankHistory: List<TeamRankHistoryPoint> = emptyList(),
    val topScorers: List<TeamRecordEntry> = emptyList(),
    val appearanceLeaders: List<TeamRecordEntry> = emptyList(),
    val description: String? = null,
    /** Public fan-circle identifier advertised by the server-provided team tab list. */
    val circleId: String? = null,
)

enum class TeamType { Club, National, Unknown }
