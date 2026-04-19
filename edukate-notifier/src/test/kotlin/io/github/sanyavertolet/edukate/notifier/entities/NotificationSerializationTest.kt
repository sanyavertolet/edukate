package io.github.sanyavertolet.edukate.notifier.entities

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.github.sanyavertolet.edukate.notifier.dtos.BaseNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.CheckedNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.InviteNotificationDto
import io.github.sanyavertolet.edukate.notifier.dtos.SimpleNotificationDto
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class NotificationSerializationTest {

    private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    // region Entity serialization — _type field

    @Test
    fun `SimpleNotification serializes with _type simple`() {
        val notification = NotificationFixtures.simpleNotification()
        val json = objectMapper.writeValueAsString(notification)
        assertThat(json).contains("\"_type\":\"simple\"")
    }

    @Test
    fun `InviteNotification serializes with _type invite`() {
        val notification = NotificationFixtures.inviteNotification()
        val json = objectMapper.writeValueAsString(notification)
        assertThat(json).contains("\"_type\":\"invite\"")
    }

    @Test
    fun `CheckedNotification serializes with _type checked`() {
        val notification = NotificationFixtures.checkedNotification()
        val json = objectMapper.writeValueAsString(notification)
        assertThat(json).contains("\"_type\":\"checked\"")
    }

    // endregion

    // region Entity polymorphic round-trips

    @Test
    fun `SimpleNotification survives polymorphic round-trip via BaseNotification`() {
        val original =
            NotificationFixtures.simpleNotification(
                uuid = "rt-simple",
                userId = 1L,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotification::class.java)

        assertThat(deserialized).isInstanceOf(SimpleNotification::class.java)
        deserialized as SimpleNotification
        assertThat(deserialized.uuid).isEqualTo("rt-simple")
        assertThat(deserialized.targetUserId).isEqualTo(1L)
        assertThat(deserialized.title).isEqualTo("Test Title")
        assertThat(deserialized.message).isEqualTo("Test Message")
        assertThat(deserialized.source).isEqualTo("test-source")
        assertThat(deserialized.isRead).isFalse()
    }

    @Test
    fun `InviteNotification survives polymorphic round-trip via BaseNotification`() {
        val original =
            NotificationFixtures.inviteNotification(uuid = "rt-invite", createdAt = Instant.parse("2025-06-01T00:00:00Z"))

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotification::class.java)

        assertThat(deserialized).isInstanceOf(InviteNotification::class.java)
        deserialized as InviteNotification
        assertThat(deserialized.uuid).isEqualTo("rt-invite")
        assertThat(deserialized.inviterName).isEqualTo("inviter")
        assertThat(deserialized.problemSetName).isEqualTo("My Bundle")
        assertThat(deserialized.problemSetShareCode).isEqualTo("SHARE123")
    }

    @Test
    fun `CheckedNotification survives polymorphic round-trip via BaseNotification`() {
        val original =
            NotificationFixtures.checkedNotification(
                uuid = "rt-checked",
                status = CheckStatus.MISTAKE,
                createdAt = Instant.parse("2025-03-01T00:00:00Z"),
            )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotification::class.java)

        assertThat(deserialized).isInstanceOf(CheckedNotification::class.java)
        deserialized as CheckedNotification
        assertThat(deserialized.uuid).isEqualTo("rt-checked")
        assertThat(deserialized.status).isEqualTo(CheckStatus.MISTAKE)
        assertThat(deserialized.submissionId).isEqualTo(1L)
        assertThat(deserialized.problemKey).isEqualTo("problem-1")
    }

    // endregion

    // region CreateRequest deserialization

    @Test
    fun `SimpleNotificationCreateRequest deserializes from JSON with _type discriminator`() {
        val json = """{"_type":"simple","uuid":"req-uuid","targetUserId":1,"title":"T","message":"M","source":"S"}"""

        val result = objectMapper.readValue(json, BaseNotificationCreateRequest::class.java)

        assertThat(result).isInstanceOf(SimpleNotificationCreateRequest::class.java)
        result as SimpleNotificationCreateRequest
        assertThat(result.uuid).isEqualTo("req-uuid")
        assertThat(result.targetUserId).isEqualTo(1L)
        assertThat(result.title).isEqualTo("T")
        assertThat(result.message).isEqualTo("M")
        assertThat(result.source).isEqualTo("S")
    }

    @Test
    fun `InviteNotificationCreateRequest deserializes from JSON with _type discriminator`() {
        val json =
            """
            |{"_type":"invite","uuid":"req-invite","targetUserId":2,
            |"inviterName":"Alice","problemSetName":"B","problemSetShareCode":"CODE"}
            """
                .trimMargin()

        val result = objectMapper.readValue(json, BaseNotificationCreateRequest::class.java)

        assertThat(result).isInstanceOf(InviteNotificationCreateRequest::class.java)
        result as InviteNotificationCreateRequest
        assertThat(result.uuid).isEqualTo("req-invite")
        assertThat(result.inviterName).isEqualTo("Alice")
        assertThat(result.problemSetName).isEqualTo("B")
        assertThat(result.problemSetShareCode).isEqualTo("CODE")
    }

    @Test
    fun `CheckedNotificationCreateRequest deserializes from JSON with _type discriminator`() {
        val json =
            """
            |{"_type":"checked","uuid":"req-checked","targetUserId":3,
            |"submissionId":1,"problemKey":"p1","status":"SUCCESS"}
            """
                .trimMargin()

        val result = objectMapper.readValue(json, BaseNotificationCreateRequest::class.java)

        assertThat(result).isInstanceOf(CheckedNotificationCreateRequest::class.java)
        result as CheckedNotificationCreateRequest
        assertThat(result.uuid).isEqualTo("req-checked")
        assertThat(result.submissionId).isEqualTo(1L)
        assertThat(result.problemKey).isEqualTo("p1")
        assertThat(result.status).isEqualTo(CheckStatus.SUCCESS)
    }

    // endregion

    // region DTO round-trips

    @Test
    fun `SimpleNotificationDto survives polymorphic round-trip via BaseNotificationDto`() {
        val now = Instant.parse("2025-01-01T12:00:00Z")
        val original =
            SimpleNotificationDto(
                uuid = "dto-simple",
                isRead = false,
                createdAt = now,
                title = "Title",
                message = "Msg",
                source = "src",
            )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotificationDto::class.java)

        assertThat(deserialized).isInstanceOf(SimpleNotificationDto::class.java)
        deserialized as SimpleNotificationDto
        assertThat(deserialized.uuid).isEqualTo("dto-simple")
        assertThat(deserialized.title).isEqualTo("Title")
        assertThat(deserialized.createdAt).isEqualTo(now)
    }

    @Test
    fun `InviteNotificationDto survives polymorphic round-trip via BaseNotificationDto`() {
        val now = Instant.parse("2025-02-01T00:00:00Z")
        val original =
            InviteNotificationDto(
                uuid = "dto-invite",
                isRead = true,
                createdAt = now,
                inviterName = "Bob",
                problemSetName = "Bundle",
                problemSetShareCode = "XYZ",
            )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotificationDto::class.java)

        assertThat(deserialized).isInstanceOf(InviteNotificationDto::class.java)
        deserialized as InviteNotificationDto
        assertThat(deserialized.inviterName).isEqualTo("Bob")
        assertThat(deserialized.problemSetShareCode).isEqualTo("XYZ")
    }

    @Test
    fun `CheckedNotificationDto survives polymorphic round-trip via BaseNotificationDto`() {
        val now = Instant.parse("2025-03-01T00:00:00Z")
        val original =
            CheckedNotificationDto(
                uuid = "dto-checked",
                isRead = false,
                createdAt = now,
                submissionId = 1L,
                problemKey = "prob-1",
                status = CheckStatus.INTERNAL_ERROR,
            )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, BaseNotificationDto::class.java)

        assertThat(deserialized).isInstanceOf(CheckedNotificationDto::class.java)
        deserialized as CheckedNotificationDto
        assertThat(deserialized.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
        assertThat(deserialized.submissionId).isEqualTo(1L)
    }

    // endregion
}
