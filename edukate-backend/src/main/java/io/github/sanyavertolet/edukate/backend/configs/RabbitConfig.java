package io.github.sanyavertolet.edukate.backend.configs;

import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Declarables resultTopology(TopicExchange edukateExchange) {
        Queue resultQueue = QueueBuilder.durable(RabbitTopology.Q_RESULT_BACKEND).build();
        Binding binding = BindingBuilder.bind(resultQueue).to(edukateExchange).with(RabbitTopology.RK_RESULT);
        return new Declarables(resultQueue, binding);
    }
}
