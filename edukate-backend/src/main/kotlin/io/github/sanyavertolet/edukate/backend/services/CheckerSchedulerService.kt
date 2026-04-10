package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class CheckerSchedulerService(private val rabbitTemplate: RabbitTemplate, private val submissionService: SubmissionService) {
    fun scheduleCheck(submission: Submission): Mono<Void> =
        submissionService.prepareContext(submission).publishOn(Schedulers.boundedElastic()).flatMap { ctx ->
            Mono.fromRunnable { rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.SCHEDULE, ctx) }
        }
}
