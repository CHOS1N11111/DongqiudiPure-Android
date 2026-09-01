package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 稳定实体 ID。
 *
 * 使用 value class 而不是裸 String，避免在导航参数和 Repository 调用之间
 * 把 TeamId 传成 PlayerId —— 这类错误在裸字符串下只有运行时才暴露。
 *
 * 导航只传这些 ID，大对象由 Repository 重新加载（ARCHITECTURE.md §5.1）。
 */
@JvmInline
value class ArticleId(val raw: String)

@JvmInline
value class MatchId(val raw: String)

@JvmInline
value class TeamId(val raw: String)

@JvmInline
value class PlayerId(val raw: String)

@JvmInline
value class CompetitionId(val raw: String)

@JvmInline
value class SeasonId(val raw: String)

/** endpoint 标识。只用于错误诊断，不含 host、path 或 query。 */
@JvmInline
value class EndpointId(val raw: String)
