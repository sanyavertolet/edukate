package io.github.sanyavertolet.edukate.common

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
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
    ) = EdukateUserDetails(id, name, roles, status, token)

    fun simpleCreateRequest(uuid: String = "uuid-simple", targetUserId: Long = USER_ID) =
        SimpleNotificationCreateRequest(
            uuid = uuid,
            targetUserId = targetUserId,
            title = "Title",
            message = "Message",
            source = "source",
        )

    fun inviteCreateRequest(uuid: String = "uuid-invite", targetUserId: Long = USER_ID) =
        InviteNotificationCreateRequest(
            uuid = uuid,
            targetUserId = targetUserId,
            inviterName = "inviter",
            problemSetName = "Bundle",
            problemSetShareCode = "CODE123",
        )

    fun checkedCreateRequest(
        uuid: String = "uuid-checked",
        targetUserId: Long = USER_ID,
        status: CheckStatus = CheckStatus.SUCCESS,
    ) =
        CheckedNotificationCreateRequest(
            uuid = uuid,
            targetUserId = targetUserId,
            submissionId = 1L,
            problemKey = "prob-1",
            status = status,
        )
}
