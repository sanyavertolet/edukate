@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.github.sanyavertolet.edukate.notifier.dtos.NotificationStatistics
import io.github.sanyavertolet.edukate.notifier.repositories.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class NotificationServiceTest {

    private val repository: NotificationRepository = mockk()
    private lateinit var service: NotificationService

    @BeforeEach
    fun setUp() {
        service = NotificationService(repository)
    }

    // region saveIfAbsent(createRequest)

    @Test
    fun `saveIfAbsent saves new notification when UUID is not present`() {
        val request = NotificationFixtures.simpleCreateRequest(uuid = "new-uuid")
        val saved = NotificationFixtures.simpleNotification(uuid = "new-uuid")
        every { repository.findNotificationByUuid("new-uuid") } returns Mono.empty()
        every { repository.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.saveIfAbsent(request)).expectNext(saved).verifyComplete()

        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `saveIfAbsent returns existing notification when UUID already exists`() {
        val existing = NotificationFixtures.simpleNotification(uuid = "existing-uuid")
        val request = NotificationFixtures.simpleCreateRequest(uuid = "existing-uuid")
        every { repository.findNotificationByUuid("existing-uuid") } returns Mono.just(existing)

        StepVerifier.create(service.saveIfAbsent(request)).expectNext(existing).verifyComplete()

        verify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `saveIfAbsent works for InviteNotificationCreateRequest`() {
        val request = NotificationFixtures.inviteCreateRequest(uuid = "invite-uuid")
        val saved = NotificationFixtures.inviteNotification(uuid = "invite-uuid")
        every { repository.findNotificationByUuid("invite-uuid") } returns Mono.empty()
        every { repository.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.saveIfAbsent(request)).expectNext(saved).verifyComplete()
    }

    @Test
    fun `saveIfAbsent works for CheckedNotificationCreateRequest`() {
        val request = NotificationFixtures.checkedCreateRequest(uuid = "checked-uuid")
        val saved = NotificationFixtures.checkedNotification(uuid = "checked-uuid")
        every { repository.findNotificationByUuid("checked-uuid") } returns Mono.empty()
        every { repository.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.saveIfAbsent(request)).expectNext(saved).verifyComplete()
    }

    // endregion

    // region getUserNotifications

    @Test
    fun `getUserNotifications returns empty Flux for unauthenticated user`() {
        StepVerifier.create(service.getUserNotifications(null, 10, 0, null)).verifyComplete()
    }

    @Test
    fun `getUserNotifications filters by isRead true`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val notification = NotificationFixtures.simpleNotification(userId = 1L, isRead = true)
        val pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
        every { repository.findAllByTargetUserIdAndIsRead(1L, true, pageRequest) } returns Flux.just(notification)

        StepVerifier.create(service.getUserNotifications(true, 10, 0, auth)).expectNext(notification).verifyComplete()
    }

    @Test
    fun `getUserNotifications filters by isRead false`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val notification = NotificationFixtures.simpleNotification(userId = 1L, isRead = false)
        val pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
        every { repository.findAllByTargetUserIdAndIsRead(1L, false, pageRequest) } returns Flux.just(notification)

        StepVerifier.create(service.getUserNotifications(false, 10, 0, auth)).expectNext(notification).verifyComplete()
    }

    @Test
    fun `getUserNotifications returns all notifications when isRead is null`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val n1 = NotificationFixtures.simpleNotification(userId = 1L, isRead = true)
        val n2 = NotificationFixtures.inviteNotification(userId = 1L, isRead = false)
        val pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
        every { repository.findAllByTargetUserId(1L, pageRequest) } returns Flux.just(n1, n2)

        StepVerifier.create(service.getUserNotifications(null, 10, 0, auth)).expectNext(n1, n2).verifyComplete()
    }

    @Test
    fun `getUserNotifications passes correct page and size to repository`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val pageRequest = PageRequest.of(2, 25, Sort.Direction.DESC, "createdAt")
        every { repository.findAllByTargetUserId(1L, pageRequest) } returns Flux.empty()

        StepVerifier.create(service.getUserNotifications(null, 25, 2, auth)).verifyComplete()

        verify { repository.findAllByTargetUserId(1L, pageRequest) }
    }

    @Test
    fun `getUserNotifications does not call isRead-filtered query when isRead is null`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")
        every { repository.findAllByTargetUserId(1L, pageRequest) } returns Flux.empty()

        StepVerifier.create(service.getUserNotifications(null, 10, 0, auth)).verifyComplete()

        verify(exactly = 0) { repository.findAllByTargetUserIdAndIsRead(any(), any(), any()) }
    }

    // endregion

    // region markAsRead

    @Test
    fun `markAsRead marks specified notifications as read and returns count`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val n1 = NotificationFixtures.simpleNotification(userId = 1L, uuid = "u1", isRead = false)
        val n2 = NotificationFixtures.inviteNotification(userId = 1L, uuid = "u2", isRead = false)
        every { repository.findByTargetUserIdAndUuidIn(1L, listOf("u1", "u2")) } returns Flux.just(n1, n2)
        every { repository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.markAsRead(listOf("u1", "u2"), auth)).expectNext(2L).verifyComplete()

        verify(exactly = 2) { repository.save(match { it.isRead }) }
    }

    @Test
    fun `markAsRead returns zero when no matching notifications found`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        every { repository.findByTargetUserIdAndUuidIn(1L, listOf("nonexistent")) } returns Flux.empty()

        StepVerifier.create(service.markAsRead(listOf("nonexistent"), auth)).expectNext(0L).verifyComplete()
    }

    @Test
    fun `markAsRead returns empty Mono for unauthenticated user`() {
        StepVerifier.create(service.markAsRead(listOf("u1"), null)).verifyComplete()
    }

    // endregion

    // region markAllAsRead

    @Test
    fun `markAllAsRead marks all unread notifications and returns count`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val n1 = NotificationFixtures.simpleNotification(userId = 1L, isRead = false)
        val n2 = NotificationFixtures.checkedNotification(userId = 1L, isRead = false)
        every { repository.findAllByTargetUserIdAndIsRead(1L, false, Pageable.unpaged()) } returns Flux.just(n1, n2)
        every { repository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.markAllAsRead(auth)).expectNext(2L).verifyComplete()

        verify(exactly = 2) { repository.save(match { it.isRead }) }
    }

    @Test
    fun `markAllAsRead returns zero when there are no unread notifications`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        every { repository.findAllByTargetUserIdAndIsRead(1L, false, Pageable.unpaged()) } returns Flux.empty()

        StepVerifier.create(service.markAllAsRead(auth)).expectNext(0L).verifyComplete()
    }

    @Test
    fun `markAllAsRead returns empty Mono for unauthenticated user`() {
        StepVerifier.create(service.markAllAsRead(null)).verifyComplete()
    }

    // endregion

    // region gatherUserStatistics

    @Test
    fun `gatherUserStatistics returns statistics from repository`() {
        val auth = NotificationFixtures.mockAuthentication(1L)
        val stats = NotificationStatistics(unread = 3, total = 10)
        every { repository.gatherStatistics(1L) } returns Mono.just(stats)

        StepVerifier.create(service.gatherUserStatistics(auth)).expectNext(stats).verifyComplete()
    }

    @Test
    fun `gatherUserStatistics returns zero statistics for unauthenticated user`() {
        StepVerifier.create(service.gatherUserStatistics(null)).expectNext(NotificationStatistics()).verifyComplete()
    }

    // endregion
}
