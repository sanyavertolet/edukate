package io.github.sanyavertolet.edukate.checker.configs;

import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Declarables scheduleTopology(TopicExchange edukateExchange) {
        Queue scheduleQueue = QueueBuilder.durable(RabbitTopology.Q.SCHEDULE_CHECKER).build();
        Binding binding = BindingBuilder.bind(scheduleQueue).to(edukateExchange).with(RabbitTopology.Rk.SCHEDULE);
        return new Declarables(scheduleQueue, binding);
    }
}
