package io.github.chos1n11111.dongqiudipure.core.model

sealed interface DataResult<out T> {
    data class Success<out T>(val value: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>
}

class AppErrorException(
    val error: AppError,
) : Exception()

fun Throwable.toAppError(): AppError =
    (this as? AppErrorException)?.error ?: AppError.Network(NetworkKind.Unknown)
