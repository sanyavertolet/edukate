package io.github.sanyavertolet.edukate.common.services;

import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Profile("notifier")
public class RabbitNotifier implements Notifier {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Mono<String> notify(BaseNotificationCreateRequest notificationCreationRequest) {
        rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.RK_NOTIFY, notificationCreationRequest);
        return Mono.just(notificationCreationRequest.getUuid());
    }
}
