package io.github.sanyavertolet.edukate.checker.services.impl

import io.github.sanyavertolet.edukate.checker.services.ResultPublisher
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class RabbitResultPublisher(private val template: RabbitTemplate) : ResultPublisher {
    override fun publish(result: CheckResultMessage): Mono<Void> =
        Mono.fromRunnable<Void> { template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.RESULT, result) }
            .retry(PUBLISH_RETRY_COUNT)
            .cast(Void::class.java)
            .subscribeOn(Schedulers.boundedElastic())

    companion object {
        private const val PUBLISH_RETRY_COUNT = 3L
    }
}
