package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.github.sanyavertolet.edukate.notifier.configs.EmailProperties
import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val emailProperties: EmailProperties,
    @param:Value("\${gateway.url}") private val gatewayUrl: String,
) {
    fun sendVerificationEmail(message: EmailVerificationMessage): Mono<Void> =
        Mono.fromRunnable<Void> {
                val link = "$gatewayUrl/api/v1/auth/verify-email?token=${message.token}"
                send(
                    to = message.toEmail,
                    subject = "Verify your edukate account",
                    body =
                        "Hello ${message.username},\n\nPlease verify your account:\n$link\n\nThis link expires in 24 hours.",
                )
            }
            .subscribeOn(Schedulers.boundedElastic())

    fun sendPasswordResetEmail(message: PasswordResetMessage): Mono<Void> =
        Mono.fromRunnable<Void> {
                val link = "$gatewayUrl/reset-password?token=${message.token}"
                send(
                    to = message.toEmail,
                    subject = "Reset your edukate password",
                    body = "Hello ${message.username},\n\nReset your password:\n$link\n\nThis link expires in 1 hour.",
                )
            }
            .subscribeOn(Schedulers.boundedElastic())

    private fun send(to: String, subject: String, body: String) {
        val msg: MimeMessage = mailSender.createMimeMessage()
        MimeMessageHelper(msg, false, "UTF-8").apply {
            setFrom(emailProperties.from)
            setTo(to)
            setSubject(subject)
            setText(body, false)
        }
        mailSender.send(msg)
    }
}
