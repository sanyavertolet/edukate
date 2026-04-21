@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.notifier.controllers

import com.ninjasquad.springmockk.MockkBean
import io.github.sanyavertolet.edukate.common.security.NoopWebSecurityConfig
import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics
import io.github.sanyavertolet.edukate.notifier.services.NotificationService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest(NotificationController::class)
@Import(NoopWebSecurityConfig::class)
class NotificationControllerTest {

    @Autowired private lateinit var webTestClient: WebTestClient

    @MockkBean private lateinit var notificationService: NotificationService

    // region GET /api/v1/notifications

    @Test
    fun `getNotifications returns 200 with notification DTOs and statistics`() {
        val simple = NotificationFixtures.simpleNotification(userId = 1L)
        val invite = NotificationFixtures.inviteNotification(userId = 1L)
        val stats = NotificationStatistics(unread = 1, total = 2)
        every { notificationService.getUserNotifications(null, 10, 0, any()) } returns Flux.just(simple, invite)
        every { notificationService.gatherUserStatistics(any()) } returns Mono.just(stats)

        webTestClient
            .get()
            .uri("/api/v1/notifications")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.notifications[0]._type")
            .isEqualTo("simple")
            .jsonPath("$.notifications[1]._type")
            .isEqualTo("invite")
            .jsonPath("$.statistics.unread")
            .isEqualTo(1)
            .jsonPath("$.statistics.total")
            .isEqualTo(2)
    }

    @Test
    fun `getNotifications returns empty notifications list when no notifications exist`() {
        val stats = NotificationStatistics(unread = 0, total = 0)
        every { notificationService.getUserNotifications(null, 10, 0, any()) } returns Flux.empty()
        every { notificationService.gatherUserStatistics(any()) } returns Mono.just(stats)

        webTestClient
            .get()
            .uri("/api/v1/notifications")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.notifications")
            .isArray
            .jsonPath("$.notifications.length()")
            .isEqualTo(0)
    }

    @Test
    fun `getNotifications passes isRead=true filter to service`() {
        val notification = NotificationFixtures.simpleNotification(isRead = true)
        val stats = NotificationStatistics(unread = 0, total = 1)
        every { notificationService.getUserNotifications(true, 10, 0, any()) } returns Flux.just(notification)
        every { notificationService.gatherUserStatistics(any()) } returns Mono.just(stats)

        webTestClient
            .get()
            .uri("/api/v1/notifications?isRead=true")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.notifications[0].isRead")
            .isEqualTo(true)
    }

    @Test
    fun `getNotifications passes isRead=false filter to service`() {
        val notification = NotificationFixtures.simpleNotification(isRead = false)
        val stats = NotificationStatistics(unread = 1, total = 1)
        every { notificationService.getUserNotifications(false, 10, 0, any()) } returns Flux.just(notification)
        every { notificationService.gatherUserStatistics(any()) } returns Mono.just(stats)

        webTestClient.get().uri("/api/v1/notifications?isRead=false").exchange().expectStatus().isOk
    }

    @Test
    fun `getNotifications passes custom page and size to service`() {
        val stats = NotificationStatistics(unread = 0, total = 0)
        every { notificationService.getUserNotifications(null, 20, 3, any()) } returns Flux.empty()
        every { notificationService.gatherUserStatistics(any()) } returns Mono.just(stats)

        webTestClient.get().uri("/api/v1/notifications?page=3&size=20").exchange().expectStatus().isOk
    }

    @Test
    fun `getNotifications returns empty body when unauthenticated`() {
        val stats = NotificationStatistics(unread = 0, total = 0)
        every { notificationService.getUserNotifications(null, 10, 0, null) } returns Flux.empty()
        every { notificationService.gatherUserStatistics(null) } returns Mono.just(stats)

        webTestClient
            .get()
            .uri("/api/v1/notifications")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.notifications.length()")
            .isEqualTo(0)
    }

    // endregion

    // region POST /api/v1/notifications/mark-as-read

    @Test
    fun `markAsRead returns 200 with count of updated notifications`() {
        every { notificationService.markAsRead(listOf("uuid-1", "uuid-2"), any()) } returns Mono.just(2L)

        webTestClient
            .post()
            .uri("/api/v1/notifications/mark-as-read")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""["uuid-1","uuid-2"]""")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(2L)
    }

    @Test
    fun `markAsRead returns 400 when body is an empty list`() {
        webTestClient
            .post()
            .uri("/api/v1/notifications/mark-as-read")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("[]")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `markAsRead returns 400 when body is missing`() {
        webTestClient.post().uri("/api/v1/notifications/mark-as-read").exchange().expectStatus().isBadRequest
    }

    // endregion

    // region POST /api/v1/notifications/mark-all-as-read

    @Test
    fun `markAllAsRead returns 200 with count of updated notifications`() {
        every { notificationService.markAllAsRead(any()) } returns Mono.just(5L)

        webTestClient
            .post()
            .uri("/api/v1/notifications/mark-all-as-read")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(5L)
    }

    @Test
    fun `markAllAsRead returns zero when all notifications are already read`() {
        every { notificationService.markAllAsRead(any()) } returns Mono.just(0L)

        webTestClient
            .post()
            .uri("/api/v1/notifications/mark-all-as-read")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody<Long>()
            .isEqualTo(0L)
    }

    // endregion
}
