package io.github.sanyavertolet.edukate.gateway

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserCredentials
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest
import io.github.sanyavertolet.edukate.gateway.entities.GatewayUser

object GatewayFixtures {
    const val USER_ID = 1L
    const val USER_NAME = "testuser"
    const val ENCODED_PASSWORD = "encoded-pw"
    const val RAW_PASSWORD = "raw-password"
    const val EMAIL = "test@example.com"

    fun signInRequest(username: String = USER_NAME, password: String = RAW_PASSWORD) = SignInRequest(username, password)

    fun signUpRequest(username: String = USER_NAME, password: String = RAW_PASSWORD, email: String = EMAIL) =
        SignUpRequest(username, password, email)

    fun userCredentials(
        id: Long? = USER_ID,
        username: String = USER_NAME,
        encodedPassword: String = ENCODED_PASSWORD,
        email: String = EMAIL,
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
    ) = UserCredentials(id, username, encodedPassword, email, roles, status)

    fun edukateUserDetails(
        id: Long = USER_ID,
        username: String = USER_NAME,
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
        token: String = "test-token",
    ) = EdukateUserDetails(id, username, roles, status, token)

    fun gatewayUser(
        id: Long? = USER_ID,
        name: String = USER_NAME,
        token: String = ENCODED_PASSWORD,
        email: String? = EMAIL,
        roles: Set<UserRole> = setOf(UserRole.USER),
        status: UserStatus = UserStatus.ACTIVE,
    ) = GatewayUser(id, name, email, token, roles, status)

    fun mockAuthentication(id: Long = USER_ID, username: String = USER_NAME) =
        edukateUserDetails(id = id, username = username).toPreAuthenticatedAuthenticationToken()
}
