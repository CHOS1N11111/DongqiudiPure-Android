package io.github.chos1n11111.dongqiudipure.core.model

/**
 * Minimal account data returned by the session validation endpoint.
 *
 * Every field is optional because authentication validity must not be inferred from profile
 * completeness. In particular, the login identifier is never used as a fallback display name.
 */
data class AccountSummary(
    val id: String? = null,
    val displayName: String? = null,
)
