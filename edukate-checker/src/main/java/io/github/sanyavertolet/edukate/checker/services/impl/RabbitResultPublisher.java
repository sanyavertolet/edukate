package io.github.sanyavertolet.edukate.checker.services.impl;

import io.github.sanyavertolet.edukate.checker.services.ResultPublisher;
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@AllArgsConstructor
public class RabbitResultPublisher implements ResultPublisher {
    private final RabbitTemplate template;

    @Override
    public Mono<Void> publish(@NonNull CheckResultMessage checkResultMessage) {
        return Mono.fromRunnable(() ->
                        template.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.RK_RESULT, checkResultMessage)
                )
                .retry(3)
                .cast(Void.class)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
