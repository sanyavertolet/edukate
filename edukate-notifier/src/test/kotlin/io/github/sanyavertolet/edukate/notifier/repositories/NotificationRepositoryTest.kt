package io.github.sanyavertolet.edukate.notifier.repositories

import io.github.sanyavertolet.edukate.notifier.NotificationFixtures
import io.github.sanyavertolet.edukate.notifier.configs.MongoConfig
import io.github.sanyavertolet.edukate.notifier.entities.CheckedNotification
import io.github.sanyavertolet.edukate.notifier.entities.InviteNotification
import io.github.sanyavertolet.edukate.notifier.entities.SimpleNotification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import reactor.test.StepVerifier
import java.time.temporal.ChronoUnit

@DataMongoTest
@Import(MongoConfig::class)
class NotificationRepositoryTest {

    @Autowired private lateinit var repository: NotificationRepository

    @BeforeEach
    fun cleanUp() {
        repository.deleteAll().block()
    }

    // region findNotificationByUuid

    @Test
    fun `findNotificationByUuid returns notification for matching UUID`() {
        repository.save(NotificationFixtures.simpleNotification(uuid = "find-uuid")).block()

        StepVerifier.create(repository.findNotificationByUuid("find-uuid"))
            .assertNext { assertThat(it.uuid).isEqualTo("find-uuid") }
            .verifyComplete()
    }

    @Test
    fun `findNotificationByUuid returns empty Mono when UUID not found`() {
        StepVerifier.create(repository.findNotificationByUuid("nonexistent-uuid")).verifyComplete()
    }

    @Test
    fun `findNotificationByUuid does not return notifications with a different UUID`() {
        repository.save(NotificationFixtures.simpleNotification(uuid = "uuid-a")).block()

        StepVerifier.create(repository.findNotificationByUuid("uuid-b")).verifyComplete()
    }

    // endregion

    // region findAllByTargetUserIdAndIsRead

    @Test
    fun `findAllByTargetUserIdAndIsRead returns only read notifications for user`() {
        val user = "user-read-filter"
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = true)).block()
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = false)).block()

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserIdAndIsRead(user, true, pageable))
            .assertNext { assertThat(it.isRead).isTrue() }
            .verifyComplete()
    }

    @Test
    fun `findAllByTargetUserIdAndIsRead returns only unread notifications for user`() {
        val user = "user-unread-filter"
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = true)).block()
        repository.save(NotificationFixtures.inviteNotification(userId = user, isRead = false)).block()

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserIdAndIsRead(user, false, pageable))
            .assertNext { assertThat(it.isRead).isFalse() }
            .verifyComplete()
    }

    @Test
    fun `findAllByTargetUserIdAndIsRead does not return notifications from other users`() {
        repository.save(NotificationFixtures.simpleNotification(userId = "other-user", isRead = true)).block()

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserIdAndIsRead("target-user", true, pageable)).verifyComplete()
    }

    // endregion

    // region findAllByTargetUserId

    @Test
    fun `findAllByTargetUserId returns all notifications for user regardless of read status`() {
        val user = "user-all"
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = true)).block()
        repository.save(NotificationFixtures.inviteNotification(userId = user, isRead = false)).block()
        repository.save(NotificationFixtures.checkedNotification(userId = user, isRead = false)).block()

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserId(user, pageable)).expectNextCount(3).verifyComplete()
    }

    @Test
    fun `findAllByTargetUserId does not return notifications from other users`() {
        repository.save(NotificationFixtures.simpleNotification(userId = "other-user")).block()

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserId("target-user", pageable)).verifyComplete()
    }

    @Test
    fun `findAllByTargetUserId respects page size`() {
        val user = "user-paged"
        repeat(5) { repository.save(NotificationFixtures.simpleNotification(userId = user)).block() }

        val pageable = PageRequest.of(0, 3, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserId(user, pageable)).expectNextCount(3).verifyComplete()
    }

    @Test
    fun `findAllByTargetUserId returns second page correctly`() {
        val user = "user-page2"
        repeat(5) { repository.save(NotificationFixtures.simpleNotification(userId = user)).block() }

        val pageable = PageRequest.of(1, 3, Sort.Direction.DESC, "createdAt")

        StepVerifier.create(repository.findAllByTargetUserId(user, pageable)).expectNextCount(2).verifyComplete()
    }

    @Test
    fun `findAllByTargetUserId sorts by createdAt descending`() {
        val user = "user-sorted"
        // @CreatedDate overrides any provided createdAt for new entities (no id).
        // Save in sequence with a small gap to guarantee distinct timestamps.
        val savedFirst = repository.save(NotificationFixtures.simpleNotification(userId = user, createdAt = null)).block()!!
        Thread.sleep(10)
        val savedSecond = repository.save(NotificationFixtures.simpleNotification(userId = user, createdAt = null)).block()!!

        val pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createdAt")

        // MongoDB stores timestamps with millisecond precision; truncate before comparing.
        val firstMs = savedFirst.createdAt!!.truncatedTo(ChronoUnit.MILLIS)
        val secondMs = savedSecond.createdAt!!.truncatedTo(ChronoUnit.MILLIS)

        StepVerifier.create(repository.findAllByTargetUserId(user, pageable))
            .assertNext { assertThat(it?.createdAt).isEqualTo(secondMs) }
            .assertNext { assertThat(it?.createdAt).isEqualTo(firstMs) }
            .verifyComplete()
    }

    // endregion

    // region findByTargetUserIdAndUuidIn

    @Test
    @Suppress("UnusedPrivateProperty")
    fun `findByTargetUserIdAndUuidIn returns matching notifications for user`() {
        val user = "user-uuid-in"

        repository.save(NotificationFixtures.simpleNotification(userId = user, uuid = "uuid-in-1")).block()!!
        repository.save(NotificationFixtures.inviteNotification(userId = user, uuid = "uuid-in-2")).block()!!
        repository.save(NotificationFixtures.simpleNotification(userId = user, uuid = "uuid-in-3")).block()

        StepVerifier.create(repository.findByTargetUserIdAndUuidIn(user, listOf("uuid-in-1", "uuid-in-2")))
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `findByTargetUserIdAndUuidIn does not return notifications from other users even if UUID matches`() {
        repository.save(NotificationFixtures.simpleNotification(userId = "other-user", uuid = "shared-uuid")).block()

        StepVerifier.create(repository.findByTargetUserIdAndUuidIn("target-user", listOf("shared-uuid"))).verifyComplete()
    }

    @Test
    fun `findByTargetUserIdAndUuidIn returns empty flux when UUID list has no matches`() {
        val user = "user-no-match"
        repository.save(NotificationFixtures.simpleNotification(userId = user, uuid = "existing-uuid")).block()

        StepVerifier.create(repository.findByTargetUserIdAndUuidIn(user, listOf("nonexistent-uuid"))).verifyComplete()
    }

    // endregion

    // region gatherStatistics

    @Test
    fun `gatherStatistics returns correct unread and total counts`() {
        val user = "user-stats"
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = false)).block()
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = false)).block()
        repository.save(NotificationFixtures.inviteNotification(userId = user, isRead = true)).block()

        StepVerifier.create(repository.gatherStatistics(user))
            .assertNext { stats ->
                assertThat(stats.unread).isEqualTo(2)
                assertThat(stats.total).isEqualTo(3)
            }
            .verifyComplete()
    }

    @Test
    fun `gatherStatistics returns zeros when user has no notifications`() {
        StepVerifier.create(repository.gatherStatistics("user-no-notifications"))
            .assertNext { stats ->
                assertThat(stats.unread).isEqualTo(0)
                assertThat(stats.total).isEqualTo(0)
            }
            .verifyComplete()
    }

    @Test
    fun `gatherStatistics does not count notifications from other users`() {
        repository.save(NotificationFixtures.simpleNotification(userId = "other-user", isRead = false)).block()

        StepVerifier.create(repository.gatherStatistics("target-user"))
            .assertNext { stats ->
                assertThat(stats.unread).isEqualTo(0)
                assertThat(stats.total).isEqualTo(0)
            }
            .verifyComplete()
    }

    @Test
    fun `gatherStatistics returns zero unread when all notifications are read`() {
        val user = "user-all-read"
        repository.save(NotificationFixtures.simpleNotification(userId = user, isRead = true)).block()
        repository.save(NotificationFixtures.inviteNotification(userId = user, isRead = true)).block()

        StepVerifier.create(repository.gatherStatistics(user))
            .assertNext { stats ->
                assertThat(stats.unread).isEqualTo(0)
                assertThat(stats.total).isEqualTo(2)
            }
            .verifyComplete()
    }

    // endregion

    // region Polymorphic persistence

    @Test
    fun `SimpleNotification is saved and retrieved as the correct type`() {
        val notification = NotificationFixtures.simpleNotification(uuid = "poly-simple")
        repository.save(notification).block()

        StepVerifier.create(repository.findNotificationByUuid("poly-simple"))
            .assertNext { retrieved ->
                assertThat(retrieved).isInstanceOf(SimpleNotification::class.java)
                retrieved as SimpleNotification
                assertThat(retrieved.title).isEqualTo("Test Title")
                assertThat(retrieved.message).isEqualTo("Test Message")
                assertThat(retrieved.source).isEqualTo("test-source")
            }
            .verifyComplete()
    }

    @Test
    fun `InviteNotification is saved and retrieved as the correct type`() {
        val notification = NotificationFixtures.inviteNotification(uuid = "poly-invite")
        repository.save(notification).block()

        StepVerifier.create(repository.findNotificationByUuid("poly-invite"))
            .assertNext { retrieved ->
                assertThat(retrieved).isInstanceOf(InviteNotification::class.java)
                retrieved as InviteNotification
                assertThat(retrieved.inviterName).isEqualTo("inviter")
                assertThat(retrieved.bundleShareCode).isEqualTo("SHARE123")
            }
            .verifyComplete()
    }

    @Test
    fun `CheckedNotification is saved and retrieved as the correct type`() {
        val notification = NotificationFixtures.checkedNotification(uuid = "poly-checked")
        repository.save(notification).block()

        StepVerifier.create(repository.findNotificationByUuid("poly-checked"))
            .assertNext { retrieved ->
                assertThat(retrieved).isInstanceOf(CheckedNotification::class.java)
                retrieved as CheckedNotification
                assertThat(retrieved.submissionId).isEqualTo("submission-1")
                assertThat(retrieved.problemId).isEqualTo("problem-1")
            }
            .verifyComplete()
    }

    // endregion

    // region Auditing

    @Test
    fun `createdAt is automatically populated on save`() {
        // Notifications created without a createdAt get it injected by @EnableReactiveMongoAuditing
        val notification = NotificationFixtures.simpleNotification(uuid = "audit-uuid", createdAt = null)

        StepVerifier.create(repository.save(notification))
            .assertNext { saved -> assertThat(saved.createdAt).isNotNull() }
            .verifyComplete()
    }

    // endregion
}
