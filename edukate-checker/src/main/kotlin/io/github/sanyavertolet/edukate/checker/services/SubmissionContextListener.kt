package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.common.checks.SubmissionContext
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SubmissionContextListener(private val checkerService: CheckerService, private val resultPublisher: ResultPublisher) {
    @RabbitListener(queues = [RabbitTopology.Q.SCHEDULE_CHECKER])
    fun onSubmissionContext(context: SubmissionContext) {
        Mono.just(context)
            .doOnNext { submissionContext -> log.debug("received submissionContext={}", submissionContext) }
            .flatMap(checkerService::runCheck)
            .flatMap(resultPublisher::publish)
            .doOnSuccess { log.debug("Successfully published result for submission {}", context.submissionId) }
            .doOnError { ex -> log.error("Failed to process or publish result for submission {}", context.submissionId, ex) }
            // RabbitMQ listener is synchronous; .block() bridges the reactive chain to the blocking
            // listener thread
            .block()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SubmissionContextListener::class.java)
    }
}
