package io.github.sanyavertolet.edukate.auth

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus

object AuthFixtures {
    const val SECRET = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // 32 chars = 256-bit min for HMAC-SHA
    const val EXPIRATION_SECONDS = 3600L
    const val HOSTNAME = "localhost"

    fun userDetails(
        id: Long = 1L,
        name: String = "Test User",
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
        token: String = "",
    ) = EdukateUserDetails(id, name, roles, status, token)
}
