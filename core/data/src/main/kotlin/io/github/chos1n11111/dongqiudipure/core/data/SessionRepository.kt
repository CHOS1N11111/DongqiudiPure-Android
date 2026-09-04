package io.github.chos1n11111.dongqiudipure.core.data

import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary
import io.github.chos1n11111.dongqiudipure.core.model.AppError
import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    data object Restoring : SessionState
    data class Anonymous(val error: AppError? = null) : SessionState
    data object SubmittingCredentials : SessionState
    data object ValidatingSession : SessionState
    data class Authenticated(val account: AccountSummary) : SessionState
    data object Expired : SessionState
}

interface SessionRepository {
    val state: StateFlow<SessionState>

    suspend fun restore()

    suspend fun login(identifier: String, password: String)

    suspend fun logout()
}
