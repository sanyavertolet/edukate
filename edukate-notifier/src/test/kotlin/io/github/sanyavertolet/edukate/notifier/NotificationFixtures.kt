package io.github.sanyavertolet.edukate.notifier

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.github.sanyavertolet.edukate.notifier.entities.CheckedNotification
import io.github.sanyavertolet.edukate.notifier.entities.InviteNotification
import io.github.sanyavertolet.edukate.notifier.entities.SimpleNotification
import java.time.Instant
import java.util.UUID
import org.springframework.security.core.Authentication

object NotificationFixtures {
    fun simpleCreateRequest(userId: Long = 1L, uuid: String = UUID.randomUUID().toString()) =
        SimpleNotificationCreateRequest(
            uuid = uuid,
            targetUserId = userId,
            title = "Test Title",
            message = "Test Message",
            source = "test-source",
        )

    fun inviteCreateRequest(userId: Long = 1L, uuid: String = UUID.randomUUID().toString()) =
        InviteNotificationCreateRequest(
            uuid = uuid,
            targetUserId = userId,
            inviterName = "inviter",
            problemSetName = "My Bundle",
            problemSetShareCode = "SHARE123",
        )

    fun checkedCreateRequest(
        userId: Long = 1L,
        uuid: String = UUID.randomUUID().toString(),
        status: CheckStatus = CheckStatus.SUCCESS,
    ) =
        CheckedNotificationCreateRequest(
            uuid = uuid,
            targetUserId = userId,
            submissionId = 1L,
            problemKey = "problem-1",
            status = status,
        )

    fun simpleNotification(
        userId: Long = 1L,
        uuid: String = UUID.randomUUID().toString(),
        isRead: Boolean = false,
        createdAt: Instant? = Instant.now(),
    ) =
        SimpleNotification(
            uuid = uuid,
            targetUserId = userId,
            isRead = isRead,
            createdAt = createdAt,
            title = "Test Title",
            message = "Test Message",
            source = "test-source",
        )

    fun inviteNotification(
        userId: Long = 1L,
        uuid: String = UUID.randomUUID().toString(),
        isRead: Boolean = false,
        createdAt: Instant? = Instant.now(),
    ) =
        InviteNotification(
            uuid = uuid,
            targetUserId = userId,
            isRead = isRead,
            createdAt = createdAt,
            inviterName = "inviter",
            problemSetName = "My Bundle",
            problemSetShareCode = "SHARE123",
        )

    fun checkedNotification(
        userId: Long = 1L,
        uuid: String = UUID.randomUUID().toString(),
        isRead: Boolean = false,
        createdAt: Instant? = Instant.now(),
        status: CheckStatus = CheckStatus.SUCCESS,
    ) =
        CheckedNotification(
            uuid = uuid,
            targetUserId = userId,
            isRead = isRead,
            createdAt = createdAt,
            submissionId = 1L,
            problemKey = "problem-1",
            status = status,
        )

    fun mockAuthentication(userId: Long = 1L): Authentication {
        val userDetails = EdukateUserDetails(userId, "testuser", emptySet(), UserStatus.ACTIVE, "token")
        return userDetails.toPreAuthenticatedAuthenticationToken()
    }
}
