package io.github.sanyavertolet.edukate.notifier.entities

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.github.sanyavertolet.edukate.notifier.dtos.CheckedNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class BaseNotificationTest {

    // region fromCreationRequest

    @Test
    fun `fromCreationRequest creates SimpleNotification from SimpleNotificationCreateRequest`() {
        val request = NotificationFixtures.simpleCreateRequest(userId = "user-42", uuid = "uuid-1")

        val result = BaseNotification.fromCreationRequest(request)

        assertThat(result).isInstanceOf(SimpleNotification::class.java)
        result as SimpleNotification
        assertThat(result.uuid).isEqualTo("uuid-1")
        assertThat(result.targetUserId).isEqualTo("user-42")
        assertThat(result.title).isEqualTo("Test Title")
        assertThat(result.message).isEqualTo("Test Message")
        assertThat(result.source).isEqualTo("test-source")
        assertThat(result.isRead).isFalse()
        assertThat(result.createdAt).isNull()
    }

    @Test
    fun `fromCreationRequest creates InviteNotification from InviteNotificationCreateRequest`() {
        val request = NotificationFixtures.inviteCreateRequest(userId = "user-7", uuid = "uuid-2")

        val result = BaseNotification.fromCreationRequest(request)

        assertThat(result).isInstanceOf(InviteNotification::class.java)
        result as InviteNotification
        assertThat(result.uuid).isEqualTo("uuid-2")
        assertThat(result.targetUserId).isEqualTo("user-7")
        assertThat(result.inviterName).isEqualTo("inviter")
        assertThat(result.bundleName).isEqualTo("My Bundle")
        assertThat(result.bundleShareCode).isEqualTo("SHARE123")
        assertThat(result.isRead).isFalse()
        assertThat(result.createdAt).isNull()
    }

    @Test
    fun `fromCreationRequest creates CheckedNotification with SUCCESS status`() {
        val request =
            NotificationFixtures.checkedCreateRequest(userId = "user-3", uuid = "uuid-3", status = CheckStatus.SUCCESS)

        val result = BaseNotification.fromCreationRequest(request)

        assertThat(result).isInstanceOf(CheckedNotification::class.java)
        result as CheckedNotification
        assertThat(result.uuid).isEqualTo("uuid-3")
        assertThat(result.targetUserId).isEqualTo("user-3")
        assertThat(result.submissionId).isEqualTo("submission-1")
        assertThat(result.problemId).isEqualTo("problem-1")
        assertThat(result.status).isEqualTo(CheckStatus.SUCCESS)
    }

    @Test
    fun `fromCreationRequest creates CheckedNotification with MISTAKE status`() {
        val request = NotificationFixtures.checkedCreateRequest(status = CheckStatus.MISTAKE)
        val result = BaseNotification.fromCreationRequest(request) as CheckedNotification
        assertThat(result.status).isEqualTo(CheckStatus.MISTAKE)
    }

    @Test
    fun `fromCreationRequest creates CheckedNotification with INTERNAL_ERROR status`() {
        val request = NotificationFixtures.checkedCreateRequest(status = CheckStatus.INTERNAL_ERROR)
        val result = BaseNotification.fromCreationRequest(request) as CheckedNotification
        assertThat(result.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
    }

    // endregion

    // region markAsRead

    @Test
    fun `markAsRead returns a new SimpleNotification copy with isRead true`() {
        val original = NotificationFixtures.simpleNotification(isRead = false)

        val marked = original.markAsRead()

        assertThat(marked).isInstanceOf(SimpleNotification::class.java)
        assertThat(marked.isRead).isTrue()
        assertThat(original.isRead).isFalse()
    }

    @Test
    fun `markAsRead returns a new InviteNotification copy with isRead true`() {
        val original = NotificationFixtures.inviteNotification(isRead = false)

        val marked = original.markAsRead()

        assertThat(marked).isInstanceOf(InviteNotification::class.java)
        assertThat(marked.isRead).isTrue()
        assertThat(original.isRead).isFalse()
    }

    @Test
    fun `markAsRead returns a new CheckedNotification copy with isRead true`() {
        val original = NotificationFixtures.checkedNotification(isRead = false)

        val marked = original.markAsRead()

        assertThat(marked).isInstanceOf(CheckedNotification::class.java)
        assertThat(marked.isRead).isTrue()
        assertThat(original.isRead).isFalse()
    }

    @Test
    fun `markAsRead preserves all other fields on SimpleNotification`() {
        val original = NotificationFixtures.simpleNotification(userId = "user-x", uuid = "u-x", isRead = false)

        val marked = original.markAsRead() as SimpleNotification

        assertThat(marked.uuid).isEqualTo("u-x")
        assertThat(marked.targetUserId).isEqualTo("user-x")
        assertThat(marked.title).isEqualTo(original.title)
        assertThat(marked.message).isEqualTo(original.message)
    }

    @Test
    fun `markAsRead on already-read notification returns isRead true`() {
        val notification = NotificationFixtures.simpleNotification(isRead = true)
        assertThat(notification.markAsRead().isRead).isTrue()
    }

    // endregion

    // region toDto

    @Test
    fun `SimpleNotification toDto returns SimpleNotificationDto with correct fields`() {
        val now = Instant.now()
        val notification = NotificationFixtures.simpleNotification(uuid = "uuid-x", userId = "u1", createdAt = now)

        val dto = notification.toDto()

        assertThat(dto).isInstanceOf(SimpleNotificationDto::class.java)
        assertThat(dto.uuid).isEqualTo("uuid-x")
        assertThat(dto.isRead).isFalse()
        assertThat(dto.createdAt).isEqualTo(now)
        assertThat(dto.title).isEqualTo("Test Title")
        assertThat(dto.message).isEqualTo("Test Message")
        assertThat(dto.source).isEqualTo("test-source")
    }

    @Test
    fun `InviteNotification toDto returns InviteNotificationDto with correct fields`() {
        val now = Instant.now()
        val notification = NotificationFixtures.inviteNotification(uuid = "uuid-y", createdAt = now)

        val dto = notification.toDto()

        assertThat(dto).isInstanceOf(InviteNotificationDto::class.java)
        assertThat(dto.uuid).isEqualTo("uuid-y")
        assertThat(dto.createdAt).isEqualTo(now)
        assertThat(dto.inviterName).isEqualTo("inviter")
        assertThat(dto.bundleName).isEqualTo("My Bundle")
        assertThat(dto.bundleShareCode).isEqualTo("SHARE123")
    }

    @Test
    fun `CheckedNotification toDto returns CheckedNotificationDto with correct fields`() {
        val now = Instant.now()
        val notification =
            NotificationFixtures.checkedNotification(uuid = "uuid-z", createdAt = now, status = CheckStatus.INTERNAL_ERROR)

        val dto = notification.toDto()

        assertThat(dto).isInstanceOf(CheckedNotificationDto::class.java)
        assertThat(dto.uuid).isEqualTo("uuid-z")
        assertThat(dto.createdAt).isEqualTo(now)
        assertThat(dto.submissionId).isEqualTo("submission-1")
        assertThat(dto.problemId).isEqualTo("problem-1")
        assertThat(dto.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
    }

    @Test
    fun `toDto throws IllegalArgumentException when createdAt is null`() {
        val notification = NotificationFixtures.simpleNotification(createdAt = null)
        assertThatThrownBy { notification.toDto() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("createdAt is null")
    }

    // endregion
}
