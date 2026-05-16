package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.github.sanyavertolet.edukate.notifier.configs.EmailProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import java.util.Properties
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import reactor.test.StepVerifier

class EmailServiceTest {

    private val mailSender: JavaMailSender = mockk()
    private val emailProperties = EmailProperties(from = "noreply@edukate.example.com")
    private val gatewayUrl = "https://gateway.example.com"
    private lateinit var service: EmailService

    @BeforeEach
    fun setUp() {
        service = EmailService(mailSender, emailProperties, gatewayUrl)
        every { mailSender.createMimeMessage() } returns MimeMessage(Session.getDefaultInstance(Properties()))
        every { mailSender.send(any<MimeMessage>()) } returns Unit
    }

    // region sendVerificationEmail

    @Test
    fun `sendVerificationEmail completes without error`() {
        val message = EmailVerificationMessage(toEmail = "alice@example.com", token = UUID.randomUUID(), username = "alice")

        StepVerifier.create(service.sendVerificationEmail(message)).verifyComplete()

        verify(exactly = 1) { mailSender.send(any<MimeMessage>()) }
    }

    @Test
    fun `sendVerificationEmail link contains verify-email path and token`() {
        val token = UUID.randomUUID()
        val message = EmailVerificationMessage(toEmail = "alice@example.com", token = token, username = "alice")
        val capturedMsg = slot<MimeMessage>()
        every { mailSender.send(capture(capturedMsg)) } returns Unit

        StepVerifier.create(service.sendVerificationEmail(message)).verifyComplete()

        val body = capturedMsg.captured.content as String
        assertThat(body).contains("$gatewayUrl/api/v1/auth/verify-email?token=$token")
    }

    @Test
    fun `sendVerificationEmail does not use reset-password path`() {
        val token = UUID.randomUUID()
        val message = EmailVerificationMessage(toEmail = "alice@example.com", token = token, username = "alice")
        val capturedMsg = slot<MimeMessage>()
        every { mailSender.send(capture(capturedMsg)) } returns Unit

        StepVerifier.create(service.sendVerificationEmail(message)).verifyComplete()

        val body = capturedMsg.captured.content as String
        assertThat(body).doesNotContain("/reset-password")
    }

    @Test
    fun `sendVerificationEmail sends to the correct recipient`() {
        val message = EmailVerificationMessage(toEmail = "alice@example.com", token = UUID.randomUUID(), username = "alice")
        val capturedMsg = slot<MimeMessage>()
        every { mailSender.send(capture(capturedMsg)) } returns Unit

        StepVerifier.create(service.sendVerificationEmail(message)).verifyComplete()

        assertThat(capturedMsg.captured.allRecipients).hasSize(1)
        assertThat(capturedMsg.captured.allRecipients[0].toString()).isEqualTo("alice@example.com")
    }

    // endregion

    // region sendPasswordResetEmail

    @Test
    fun `sendPasswordResetEmail completes without error`() {
        val message = PasswordResetMessage(toEmail = "bob@example.com", token = UUID.randomUUID(), username = "bob")

        StepVerifier.create(service.sendPasswordResetEmail(message)).verifyComplete()

        verify(exactly = 1) { mailSender.send(any<MimeMessage>()) }
    }

    @Test
    fun `sendPasswordResetEmail link uses frontend reset-password path not API path`() {
        val token = UUID.randomUUID()
        val message = PasswordResetMessage(toEmail = "bob@example.com", token = token, username = "bob")
        val capturedMsg = slot<MimeMessage>()
        every { mailSender.send(capture(capturedMsg)) } returns Unit

        StepVerifier.create(service.sendPasswordResetEmail(message)).verifyComplete()

        val body = capturedMsg.captured.content as String
        assertThat(body).contains("$gatewayUrl/reset-password?token=$token")
        assertThat(body).doesNotContain("/api/v1/auth/reset-password")
    }

    @Test
    fun `sendPasswordResetEmail sends to the correct recipient`() {
        val message = PasswordResetMessage(toEmail = "bob@example.com", token = UUID.randomUUID(), username = "bob")
        val capturedMsg = slot<MimeMessage>()
        every { mailSender.send(capture(capturedMsg)) } returns Unit

        StepVerifier.create(service.sendPasswordResetEmail(message)).verifyComplete()

        assertThat(capturedMsg.captured.allRecipients).hasSize(1)
        assertThat(capturedMsg.captured.allRecipients[0].toString()).isEqualTo("bob@example.com")
    }

    // endregion
}
