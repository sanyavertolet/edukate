package io.github.sanyavertolet.edukate.backend.listeners

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.services.CheckResultService
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
@Profile("!spec-gen")
class CheckResultMessageListener(
    private val checkResultService: CheckResultService,
    private val problemRepository: ProblemRepository,
    private val notifier: Notifier,
) {
    @RabbitListener(queues = [RabbitTopology.Q.RESULT_BACKEND])
    fun onCheckResultMessage(checkResultMessage: CheckResultMessage) {
        log.debug("Received result for submission {}", checkResultMessage.submissionId)

        CheckResult.fromCheckResultMessage(checkResultMessage)
            .toMono()
            .flatMap { checkResultService.saveAndUpdateSubmission(it) }
            .flatMap { (checkResult, submission) -> prepareNotification(checkResult, submission) }
            .flatMap { notifier.notify(it) }
            .doOnError { ex -> log.error("Failed to persist result {}", checkResultMessage.submissionId, ex) }
            .block()
    }

    private fun prepareNotification(
        checkResult: CheckResult,
        submission: Submission,
    ): Mono<CheckedNotificationCreateRequest> {
        val submissionId = requireNotNull(submission.id) { "Submission ID must not be null" }
        return problemRepository
            .findById(submission.problemId)
            .orNotFound("Problem not found for submission $submissionId")
            .map { problem ->
                CheckedNotificationCreateRequest.from(submission.userId, submissionId, problem.key, checkResult.status)
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CheckResultMessageListener::class.java)
    }
}
