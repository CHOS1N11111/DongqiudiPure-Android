package io.github.chos1n11111.dongqiudipure.core.network

import io.github.chos1n11111.dongqiudipure.core.model.AccountSummary

/** A mobile authorization value. Its string representation is always redacted. */
class AuthorizationToken(val value: String) {
    init {
        require(value.isNotBlank())
        require(value.length <= MAX_AUTHORIZATION_LENGTH)
        require(value.none(Char::isISOControl))
    }

    override fun toString(): String = "AuthorizationToken(REDACTED)"

    private companion object {
        const val MAX_AUTHORIZATION_LENGTH = 16_384
    }
}

interface AuthRemoteDataSource {
    suspend fun login(
        identifier: String,
        password: String,
        deviceId: String,
    ): ApiResult<AuthorizationToken>

    suspend fun validateSession(
        authorization: AuthorizationToken,
        deviceId: String,
    ): ApiResult<AccountSummary>
}

data class DqdClientProfile(
    val userAgent: String,
)
