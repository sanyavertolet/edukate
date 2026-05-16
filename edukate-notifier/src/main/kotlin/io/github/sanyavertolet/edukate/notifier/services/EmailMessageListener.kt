package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.BaseEmailMessage
import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.github.sanyavertolet.edukate.messaging.RabbitTopology
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!spec-gen")
class EmailMessageListener(private val emailService: EmailService) {
    @RabbitListener(queues = [RabbitTopology.Q.EMAIL])
    fun handleEmailMessage(message: BaseEmailMessage) {
        log.debug("received email message type={} to={}", message::class.simpleName, message.toEmail)
        when (message) {
            is EmailVerificationMessage -> emailService.sendVerificationEmail(message).subscribe()
            is PasswordResetMessage -> emailService.sendPasswordResetEmail(message).subscribe()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(EmailMessageListener::class.java)
    }
}
