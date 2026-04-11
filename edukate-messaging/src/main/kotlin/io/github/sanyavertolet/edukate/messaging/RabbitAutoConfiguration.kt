package io.github.sanyavertolet.edukate.messaging

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(AmqpAdmin::class)
class RabbitAutoConfiguration {
    @Bean @ConditionalOnMissingBean fun edukateExchange() = TopicExchange(RabbitTopology.EXCHANGE, true, false)

    @Bean @ConditionalOnMissingBean fun jacksonJsonMessageConverter() = JacksonJsonMessageConverter()
}
