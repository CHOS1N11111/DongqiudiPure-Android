package io.github.chos1n11111.dongqiudipure.core.model

/**
 * 单个页面 section 的状态。
 *
 * 「资料页和比赛详情由独立 section state 组成，单项缺失或失败不能清空整个页面」
 * （ARCHITECTURE.md §5.2）。把状态建模在 section 粒度而不是页面粒度，
 * 是让失败隔离成为默认行为的前提 —— 页面持有多个 [SectionState]，
 * 一个失败不影响其余。
 *
 * 使用 sealed interface 而不是若干 Boolean，避免出现
 * `isLoading && hasError` 这类互相矛盾的组合（ARCHITECTURE.md §5.1）。
 */
sealed interface SectionState<out T> {

    data object Loading : SectionState<Nothing>

    data class Content<out T>(val value: T) : SectionState<T>

    /** 请求成功，但该 section 确实没有内容。与 [Failed] 语义不同，不应显示「重试」。 */
    data object Empty : SectionState<Nothing>

    data class Failed(val error: AppError) : SectionState<Nothing>
}

/** 已有内容时返回内容，否则 null。用于「刷新失败但保留旧内容」的场景。 */
fun <T> SectionState<T>.contentOrNull(): T? = (this as? SectionState.Content)?.value
