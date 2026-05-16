package io.github.sanyavertolet.edukate.common

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus

object CommonFixtures {
    const val USER_ID = 1L
    const val USER_NAME = "testuser"
    const val ENCODED_PASSWORD = "encoded-pw"
    const val EMAIL = "test@example.com"

    fun userCredentials(
        id: Long? = USER_ID,
        username: String = USER_NAME,
        encodedPassword: String = ENCODED_PASSWORD,
        email: String = EMAIL,
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
    ) = UserCredentials(id, username, encodedPassword, email, roles, status)

    fun userDetails(
        id: Long = USER_ID,
        name: String = USER_NAME,
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
        token: String = "token",
        email: String = EMAIL,
    ) = EdukateUserDetails(id, name, roles, status, token, email)
}
