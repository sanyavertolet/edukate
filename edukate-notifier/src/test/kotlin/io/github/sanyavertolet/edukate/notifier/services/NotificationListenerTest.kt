@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class NotificationListenerTest {

    private val notificationService: NotificationService = mockk()
    private lateinit var listener: NotificationListener

    @BeforeEach
    fun setUp() {
        listener = NotificationListener(notificationService)
    }

    @Test
    fun `scheduleCheck delegates SimpleNotificationCreateRequest to saveIfAbsent`() {
        val request = NotificationFixtures.simpleCreateRequest()
        val saved = NotificationFixtures.simpleNotification(uuid = request.uuid)
        every { notificationService.saveIfAbsent(request) } returns Mono.just(saved)

        listener.scheduleCheck(request)

        verify { notificationService.saveIfAbsent(request) }
    }

    @Test
    fun `scheduleCheck delegates InviteNotificationCreateRequest to saveIfAbsent`() {
        val request = NotificationFixtures.inviteCreateRequest()
        val saved = NotificationFixtures.inviteNotification(uuid = request.uuid)
        every { notificationService.saveIfAbsent(request) } returns Mono.just(saved)

        listener.scheduleCheck(request)

        verify { notificationService.saveIfAbsent(request) }
    }

    @Test
    fun `scheduleCheck delegates CheckedNotificationCreateRequest to saveIfAbsent`() {
        val request = NotificationFixtures.checkedCreateRequest()
        val saved = NotificationFixtures.checkedNotification(uuid = request.uuid)
        every { notificationService.saveIfAbsent(request) } returns Mono.just(saved)

        listener.scheduleCheck(request)

        verify { notificationService.saveIfAbsent(request) }
    }

    @Test
    fun `scheduleCheck silently swallows saveIfAbsent errors`() {
        // Known issue: the bare .subscribe() in NotificationListener.scheduleCheck has no onError
        // callback. This means that if the repository is unavailable or throws, the error is
        // silently discarded — no logs at this level, no dead-letter delivery, no RabbitMQ nack.
        // Messages can be permanently lost with zero observability.
        // See TODO in NotificationListener.scheduleCheck.
        val request = NotificationFixtures.simpleCreateRequest()
        every { notificationService.saveIfAbsent(request) } returns Mono.error(RuntimeException("DB unavailable"))

        // Should not throw — the bare subscribe() swallows the error
        listener.scheduleCheck(request)
    }
}
