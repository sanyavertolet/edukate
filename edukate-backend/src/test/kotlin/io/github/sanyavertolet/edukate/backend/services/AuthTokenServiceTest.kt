@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.AuthToken
import io.github.sanyavertolet.edukate.backend.entities.AuthTokenType
import io.github.sanyavertolet.edukate.backend.repositories.AuthTokenRepository
import io.github.sanyavertolet.edukate.common.services.EmailPublisher
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AuthTokenServiceTest {
    private val authTokenRepository: AuthTokenRepository = mockk()
    private val userService: UserService = mockk()
    private val emailPublisher: EmailPublisher = mockk()
    private lateinit var service: AuthTokenService

    @BeforeEach
    fun setUp() {
        service =
            AuthTokenService(
                authTokenRepository,
                userService,
                emailPublisher,
                verificationTtlSeconds = 86400,
                passwordResetTtlSeconds = 3600,
            )
        every { emailPublisher.publish(any()) } returns Mono.empty()
    }

    // region issueVerificationToken

    @Test
    fun `issueVerificationToken stores token and publishes email`() {
        val user = BackendFixtures.user(id = 1L, name = "alice", email = "alice@example.com", status = UserStatus.PENDING)
        every { userService.findUserById(1L) } returns Mono.just(user)
        every { authTokenRepository.deleteAllByUserIdAndType(1L, AuthTokenType.EMAIL_VERIFICATION) } returns Mono.empty()
        every { authTokenRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.issueVerificationToken(1L, "alice@example.com")).verifyComplete()

        verify { authTokenRepository.deleteAllByUserIdAndType(1L, AuthTokenType.EMAIL_VERIFICATION) }
        verify { authTokenRepository.save(match { it.type == AuthTokenType.EMAIL_VERIFICATION && it.userId == 1L }) }
        verify { emailPublisher.publish(any()) }
    }

    // endregion

    // region issuePasswordResetToken

    @Test
    fun `issuePasswordResetToken stores token and publishes email when user exists`() {
        val user = BackendFixtures.user(id = 2L, name = "bob", email = "bob@example.com")
        every { userService.findUserByEmail("bob@example.com") } returns Mono.just(user)
        every { authTokenRepository.deleteAllByUserIdAndType(2L, AuthTokenType.PASSWORD_RESET) } returns Mono.empty()
        every { authTokenRepository.save(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.issuePasswordResetToken("bob@example.com")).verifyComplete()

        verify { emailPublisher.publish(any()) }
    }

    @Test
    fun `issuePasswordResetToken completes silently when user not found`() {
        every { userService.findUserByEmail("unknown@example.com") } returns Mono.empty()

        StepVerifier.create(service.issuePasswordResetToken("unknown@example.com")).verifyComplete()

        verify(exactly = 0) { emailPublisher.publish(any()) }
    }

    // endregion

    // region consumeVerificationToken

    @Test
    fun `consumeVerificationToken activates user and deletes token`() {
        val token = UUID.randomUUID()
        val user = BackendFixtures.user(id = 1L, status = UserStatus.PENDING)
        val authToken = AuthToken(token, 1L, AuthTokenType.EMAIL_VERIFICATION, Instant.now().plusSeconds(3600))

        every { authTokenRepository.findByTokenAndType(token, AuthTokenType.EMAIL_VERIFICATION) } returns
            Mono.just(authToken)
        every { authTokenRepository.deleteById(token) } returns Mono.empty()
        every { userService.findUserById(1L) } returns Mono.just(user)
        every { userService.saveUser(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.consumeVerificationToken(token)).verifyComplete()

        val savedSlot = slot<io.github.sanyavertolet.edukate.backend.entities.User>()
        verify { userService.saveUser(capture(savedSlot)) }
        assert(savedSlot.captured.status == UserStatus.ACTIVE)
    }

    @Test
    fun `consumeVerificationToken returns 404 when token not found`() {
        val token = UUID.randomUUID()
        every { authTokenRepository.findByTokenAndType(token, AuthTokenType.EMAIL_VERIFICATION) } returns Mono.empty()

        StepVerifier.create(service.consumeVerificationToken(token))
            .expectErrorMatches {
                it is org.springframework.web.server.ResponseStatusException && it.statusCode.value() == 404
            }
            .verify()
    }

    @Test
    fun `consumeVerificationToken returns 410 when token expired`() {
        val token = UUID.randomUUID()
        val expiredToken = AuthToken(token, 1L, AuthTokenType.EMAIL_VERIFICATION, Instant.now().minusSeconds(1))

        every { authTokenRepository.findByTokenAndType(token, AuthTokenType.EMAIL_VERIFICATION) } returns
            Mono.just(expiredToken)
        every { authTokenRepository.deleteById(token) } returns Mono.empty()

        StepVerifier.create(service.consumeVerificationToken(token))
            .expectErrorMatches {
                it is org.springframework.web.server.ResponseStatusException && it.statusCode.value() == 410
            }
            .verify()
    }

    // endregion

    // region consumeResetToken

    @Test
    fun `consumeResetToken updates password and deletes token`() {
        val token = UUID.randomUUID()
        val user = BackendFixtures.user(id = 1L)
        val authToken = AuthToken(token, 1L, AuthTokenType.PASSWORD_RESET, Instant.now().plusSeconds(3600))

        every { authTokenRepository.findByTokenAndType(token, AuthTokenType.PASSWORD_RESET) } returns Mono.just(authToken)
        every { authTokenRepository.deleteById(token) } returns Mono.empty()
        every { userService.findUserById(1L) } returns Mono.just(user)
        every { userService.saveUser(any()) } answers { Mono.just(firstArg()) }

        StepVerifier.create(service.consumeResetToken(token, "new-encoded-password")).verifyComplete()

        val savedSlot = slot<io.github.sanyavertolet.edukate.backend.entities.User>()
        verify { userService.saveUser(capture(savedSlot)) }
        assert(savedSlot.captured.token == "new-encoded-password")
    }

    @Test
    fun `consumeResetToken returns 410 when token expired`() {
        val token = UUID.randomUUID()
        val expiredToken = AuthToken(token, 1L, AuthTokenType.PASSWORD_RESET, Instant.now().minusSeconds(1))

        every { authTokenRepository.findByTokenAndType(token, AuthTokenType.PASSWORD_RESET) } returns Mono.just(expiredToken)
        every { authTokenRepository.deleteById(token) } returns Mono.empty()

        StepVerifier.create(service.consumeResetToken(token, "any"))
            .expectErrorMatches {
                it is org.springframework.web.server.ResponseStatusException && it.statusCode.value() == 410
            }
            .verify()
    }

    // endregion
}
