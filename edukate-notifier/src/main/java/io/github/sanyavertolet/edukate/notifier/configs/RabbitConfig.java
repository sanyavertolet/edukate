package io.github.sanyavertolet.edukate.notifier.configs;

import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Declarables notifyTopology(TopicExchange edukateExchange) {
        Queue notifyQueue = QueueBuilder.durable(RabbitTopology.Q_NOTIFY).build();
        Binding binding = BindingBuilder.bind(notifyQueue).to(edukateExchange).with(RabbitTopology.RK_NOTIFY);
        return new Declarables(notifyQueue, binding);
    }
}
