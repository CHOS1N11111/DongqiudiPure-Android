package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 比赛状态。
 *
 * [Unknown] 是刻意保留的分支：服务端新增状态时，parser 不崩溃、UI 也不会把
 * 未知状态误显示为「完场」或零比分（ARCHITECTURE.md §5.2、PLAN.md M4 退出条件）。
 */
sealed interface MatchStatus {

    /** 未开始。[kickoffLabel] 为已按用户时区格式化的开球时间。 */
    data class NotStarted(val kickoffLabel: String) : MatchStatus

    /** 进行中。[minuteLabel] 如 "67'"、"45+2'"；服务端未提供时为 null。 */
    data class Live(val minuteLabel: String?) : MatchStatus

    data object HalfTime : MatchStatus

    data object Finished : MatchStatus

    data object Postponed : MatchStatus

    data object Cancelled : MatchStatus

    /** 服务端返回了当前版本不认识的状态。原样保留，供 UI 降级显示。 */
    data class Unknown(val rawValue: String) : MatchStatus
}

/** 该状态下是否应展示比分。未开始与延期没有比分可展示。 */
val MatchStatus.hasScore: Boolean
    get() = when (this) {
        is MatchStatus.Live, MatchStatus.HalfTime, MatchStatus.Finished -> true
        is MatchStatus.NotStarted, MatchStatus.Postponed,
        MatchStatus.Cancelled, is MatchStatus.Unknown,
        -> false
    }

/** 是否需要实时刷新。终场后必须返回 false，用于停止轮询（PLAN.md M4）。 */
val MatchStatus.needsLiveRefresh: Boolean
    get() = this is MatchStatus.Live || this == MatchStatus.HalfTime
