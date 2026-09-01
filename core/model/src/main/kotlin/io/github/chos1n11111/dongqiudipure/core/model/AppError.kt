package io.github.chos1n11111.dongqiudipure.core.model

import kotlin.time.Duration

/**
 * 统一错误模型（ARCHITECTURE.md §8）。
 *
 * UI 不直接展示 exception message。用户文案描述可采取的动作，
 * 诊断信息只保留脱敏的 endpoint 标识与 HTTP 状态。
 */
sealed interface AppError {

    data class Network(val kind: NetworkKind) : AppError

    data class Http(val status: Int) : AppError

    data class Server(val code: String?, val message: String?) : AppError

    data class Parse(val endpoint: EndpointId) : AppError

    data object AuthenticationRequired : AppError

    data object SessionExpired : AppError

    data class RateLimited(val retryAfter: Duration?) : AppError

    /**
     * 服务端结构与当前版本的 contract 不兼容。
     *
     * 这是非官方客户端特有的失败模式，必须与网络错误区分：
     * 用户重试再多次也不会成功，需要的是更新应用。
     */
    data class UnsupportedContract(val endpoint: EndpointId) : AppError
}

enum class NetworkKind {
    NoConnection,
    Timeout,
    TlsFailure,
    Unknown,
}

/** 重试是否可能改变结果。用于决定错误态要不要显示「重试」按钮。 */
val AppError.isRetryable: Boolean
    get() = when (this) {
        is AppError.Network, is AppError.Http, is AppError.Server, is AppError.RateLimited -> true
        is AppError.Parse, is AppError.UnsupportedContract -> false
        AppError.AuthenticationRequired, AppError.SessionExpired -> false
    }

/** 脱敏诊断串。只含 endpoint 标识与状态码，绝不含 host、query、Header 或凭据。 */
val AppError.diagnostic: String?
    get() = when (this) {
        is AppError.Http -> "HTTP $status"
        is AppError.Parse -> "Parse · ${endpoint.raw}"
        is AppError.UnsupportedContract -> "UnsupportedContract · ${endpoint.raw}"
        is AppError.Server -> code?.let { "Server · $it" }
        is AppError.Network -> "Network · $kind"
        is AppError.RateLimited -> "RateLimited"
        AppError.AuthenticationRequired, AppError.SessionExpired -> null
    }
