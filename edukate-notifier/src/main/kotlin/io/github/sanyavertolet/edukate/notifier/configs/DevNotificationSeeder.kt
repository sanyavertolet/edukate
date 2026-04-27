package io.github.sanyavertolet.edukate.notifier.configs

import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.InviteNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.notifier.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Seeds sample notifications for all dev users on startup.
 *
 * Idempotency: each notification has a deterministic UUID built from the prefix "dev-seed-" + userId + type index.
 * [NotificationService.saveIfAbsent] skips any notification whose UUID already exists in MongoDB, so this bean is safe to
 * run on every restart.
 *
 * Activated only under the `dev` Spring profile.
 */
@Component
@Profile("dev")
class DevNotificationSeeder(private val notificationService: NotificationService) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val requests = DEV_USER_IDS.flatMap { userId -> buildNotificationsForUser(userId) }

        Flux.fromIterable(requests)
            .flatMap { notificationService.saveIfAbsent(it) }
            .doOnComplete { log.info("Dev notification seeding complete ({} notifications)", requests.size) }
            .doOnError { log.error("Dev notification seeding failed", it) }
            .subscribe()
    }

    companion object {
        private val log = LoggerFactory.getLogger(DevNotificationSeeder::class.java)

        /** User IDs matching the dev seed data in V22042026__dev_data.sql. */
        private val DEV_USER_IDS = listOf(1L, 2L, 3L)

        private const val DEV_PROBLEM_KEY = "savchenko/1.1.4"

        // Submission IDs matching V22042026__dev_data.sql (problem 1.1.4, user admin)
        private const val SUBMISSION_ID_PENDING = 1L
        private const val SUBMISSION_ID_MISTAKE = 2L
        private const val SUBMISSION_ID_SUCCESS = 3L
        private const val SUBMISSION_ID_ERROR = 4L

        // UUID index offsets per notification group
        private const val CHECKED_INDEX_OFFSET = 3
        private const val INVITE_INDEX_OFFSET_1 = 7
        private const val INVITE_INDEX_OFFSET_2 = 8

        private fun devUuid(userId: Long, index: Int): String = "dev-seed-$userId-$index"

        private fun buildNotificationsForUser(userId: Long): List<BaseNotificationCreateRequest> =
            buildSimpleNotifications(userId) + buildCheckedNotifications(userId) + buildInviteNotifications(userId)

        private fun buildSimpleNotifications(userId: Long): List<SimpleNotificationCreateRequest> =
            listOf(
                SimpleNotificationCreateRequest(
                    uuid = devUuid(userId, 1),
                    targetUserId = userId,
                    title = "Welcome to Edukate!",
                    message = "Start solving physics problems and track your progress.",
                    source = "edukate",
                ),
                SimpleNotificationCreateRequest(
                    uuid = devUuid(userId, 2),
                    targetUserId = userId,
                    title = "New problems available",
                    message = "14 chapters of Savchenko problems are now ready to solve.",
                    source = "edukate",
                ),
            )

        private fun buildCheckedNotifications(userId: Long): List<CheckedNotificationCreateRequest> {
            val statusToSubmissionId =
                listOf(
                    CheckStatus.SUCCESS to SUBMISSION_ID_SUCCESS,
                    CheckStatus.MISTAKE to SUBMISSION_ID_MISTAKE,
                    CheckStatus.PENDING to SUBMISSION_ID_PENDING,
                    CheckStatus.INTERNAL_ERROR to SUBMISSION_ID_ERROR,
                )
            return statusToSubmissionId.mapIndexed { i, (status, submissionId) ->
                CheckedNotificationCreateRequest(
                    uuid = devUuid(userId, i + CHECKED_INDEX_OFFSET),
                    targetUserId = userId,
                    submissionId = submissionId,
                    problemKey = DEV_PROBLEM_KEY,
                    status = status,
                )
            }
        }

        private fun buildInviteNotifications(userId: Long): List<InviteNotificationCreateRequest> =
            listOf(
                InviteNotificationCreateRequest(
                    uuid = devUuid(userId, INVITE_INDEX_OFFSET_1),
                    targetUserId = userId,
                    inviterName = "moderator",
                    problemSetName = "Classical Mechanics",
                    problemSetShareCode = "MECH-PRIV-2026",
                ),
                InviteNotificationCreateRequest(
                    uuid = devUuid(userId, INVITE_INDEX_OFFSET_2),
                    targetUserId = userId,
                    inviterName = "admin",
                    problemSetName = "Introduction to Physics",
                    problemSetShareCode = "INTRO-PUB-2026",
                ),
            )
    }
}
