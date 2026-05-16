@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.notifier.services

import io.github.sanyavertolet.edukate.common.notifications.EmailVerificationMessage
import io.github.sanyavertolet.edukate.common.notifications.PasswordResetMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class EmailMessageListenerTest {

    private val emailService: EmailService = mockk()
    private lateinit var listener: EmailMessageListener

    @BeforeEach
    fun setUp() {
        listener = EmailMessageListener(emailService)
    }

    @Test
    fun `handleEmailMessage dispatches EmailVerificationMessage to sendVerificationEmail`() {
        val message = EmailVerificationMessage(toEmail = "alice@example.com", token = UUID.randomUUID(), username = "alice")
        every { emailService.sendVerificationEmail(message) } returns Mono.empty()

        listener.handleEmailMessage(message)

        verify(exactly = 1) { emailService.sendVerificationEmail(message) }
        verify(exactly = 0) { emailService.sendPasswordResetEmail(any()) }
    }

    @Test
    fun `handleEmailMessage dispatches PasswordResetMessage to sendPasswordResetEmail`() {
        val message = PasswordResetMessage(toEmail = "bob@example.com", token = UUID.randomUUID(), username = "bob")
        every { emailService.sendPasswordResetEmail(message) } returns Mono.empty()

        listener.handleEmailMessage(message)

        verify(exactly = 1) { emailService.sendPasswordResetEmail(message) }
        verify(exactly = 0) { emailService.sendVerificationEmail(any()) }
    }

    @Test
    fun `handleEmailMessage silently swallows errors from sendVerificationEmail`() {
        val message = EmailVerificationMessage(toEmail = "err@example.com", token = UUID.randomUUID(), username = "erruser")
        every { emailService.sendVerificationEmail(message) } returns Mono.error(RuntimeException("SMTP down"))

        listener.handleEmailMessage(message)
    }

    @Test
    fun `handleEmailMessage silently swallows errors from sendPasswordResetEmail`() {
        val message = PasswordResetMessage(toEmail = "err@example.com", token = UUID.randomUUID(), username = "erruser")
        every { emailService.sendPasswordResetEmail(message) } returns Mono.error(RuntimeException("SMTP down"))

        listener.handleEmailMessage(message)
    }
}
