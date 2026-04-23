package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.backend.mappers.SubmissionMapper
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class CheckerSchedulerService(
    private val rabbitTemplate: RabbitTemplate,
    private val submissionMapper: SubmissionMapper,
    private val checkResultRepository: CheckResultRepository,
) {
    fun scheduleCheck(submission: Submission): Mono<Void> {
        val submissionId = requireNotNull(submission.id) { "Submission ID must not be null" }
        return checkResultRepository.save(CheckResult.stub(submissionId)).flatMap { stub ->
            val checkResultId = requireNotNull(stub.id) { "CheckResult ID must not be null after save" }
            submissionMapper.prepareContext(submission, checkResultId).publishOn(Schedulers.boundedElastic()).flatMap { ctx
                ->
                Mono.fromRunnable { rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.SCHEDULE, ctx) }
            }
        }
    }
}
