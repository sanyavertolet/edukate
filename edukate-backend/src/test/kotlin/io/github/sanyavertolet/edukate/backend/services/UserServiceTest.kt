@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.repositories.UserRepository
import io.github.sanyavertolet.edukate.common.notifications.BaseNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.users.UserStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val notifier: Notifier = mockk()
    private lateinit var service: UserService

    @BeforeEach
    fun setUp() {
        service = UserService(userRepository, notifier)
    }

    // region findUserByName

    @Test
    fun `findUserByName returns User`() {
        val user = BackendFixtures.user(name = "alice")
        every { userRepository.findByName("alice") } returns Mono.just(user)

        StepVerifier.create(service.findUserByName("alice")).expectNext(user).verifyComplete()
    }

    @Test
    fun `findUserByName empty propagates empty`() {
        every { userRepository.findByName("ghost") } returns Mono.empty()

        StepVerifier.create(service.findUserByName("ghost")).verifyComplete()
    }

    // endregion

    // region hasUserPermissionToSubmit

    @Test
    fun `hasUserPermissionToSubmit returns true for active`() {
        val user = BackendFixtures.user(status = UserStatus.ACTIVE)

        StepVerifier.create(service.hasUserPermissionToSubmit(user)).expectNext(true).verifyComplete()
    }

    @Test
    fun `hasUserPermissionToSubmit returns false for pending`() {
        val user = BackendFixtures.user(status = UserStatus.PENDING)

        StepVerifier.create(service.hasUserPermissionToSubmit(user)).expectNext(false).verifyComplete()
    }

    // endregion

    // region saveUser / deleteUserById

    @Test
    fun `saveUser delegates to repository`() {
        val user = BackendFixtures.user()
        every { userRepository.save(user) } returns Mono.just(user)

        StepVerifier.create(service.saveUser(user)).expectNext(user).verifyComplete()

        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `deleteUserById delegates to repository`() {
        every { userRepository.deleteById(1L) } returns Mono.empty()

        StepVerifier.create(service.deleteUserById(1L)).verifyComplete()

        verify(exactly = 1) { userRepository.deleteById(1L) }
    }

    // endregion

    // region notifyAllUsersWithStatus

    @Test
    fun `notifyAllUsersWithStatus publishes to notifier`() {
        val user1 = BackendFixtures.user(id = 1L, name = "alice")
        val user2 = BackendFixtures.user(id = 2L, name = "bob")
        every { userRepository.findAllByStatus(UserStatus.ACTIVE) } returns Flux.just(user1, user2)
        every { notifier.notify(any<BaseNotificationCreateRequest>()) } returns Mono.just("notif-id")

        StepVerifier.create(service.notifyAllUsersWithStatus("Title", "Hello", UserStatus.ACTIVE))
            .assertNext { count -> assertThat(count).isEqualTo(2L) }
            .verifyComplete()

        verify(exactly = 2) { notifier.notify(any()) }
    }

    // endregion

    // region setAvatarTimestamp

    @Test
    fun `setAvatarTimestamp writes the instant onto the user`() {
        val instant = Instant.ofEpochSecond(1_700_000_000)
        val user = BackendFixtures.user(id = 42L, avatarUpdatedAt = null)
        val saved = user.copy(avatarUpdatedAt = instant)
        every { userRepository.findById(42L) } returns Mono.just(user)
        every { userRepository.save(any()) } returns Mono.just(saved)

        StepVerifier.create(service.setAvatarTimestamp(42L, instant)).expectNext(saved).verifyComplete()

        verify(exactly = 1) { userRepository.save(match { it.avatarUpdatedAt == instant }) }
    }

    @Test
    fun `setAvatarTimestamp clears the instant when given null`() {
        val user = BackendFixtures.user(id = 42L, avatarUpdatedAt = Instant.ofEpochSecond(1_700_000_000))
        val cleared = user.copy(avatarUpdatedAt = null)
        every { userRepository.findById(42L) } returns Mono.just(user)
        every { userRepository.save(any()) } returns Mono.just(cleared)

        StepVerifier.create(service.setAvatarTimestamp(42L, null)).expectNext(cleared).verifyComplete()

        verify(exactly = 1) { userRepository.save(match { it.avatarUpdatedAt == null }) }
    }

    @Test
    fun `setAvatarTimestamp errors NOT_FOUND and never saves when the user is missing`() {
        every { userRepository.findById(404L) } returns Mono.empty()

        StepVerifier.create(service.setAvatarTimestamp(404L, Instant.ofEpochSecond(1_700_000_000)))
            .expectErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.NOT_FOUND }
            .verify()

        verify(exactly = 0) { userRepository.save(any()) }
    }

    // endregion
}
