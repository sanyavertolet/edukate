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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    // region findUserByAuthentication

    @Test
    fun `findUserByAuthentication returns User`() {
        val user = BackendFixtures.user(id = "user-1")
        val auth = BackendFixtures.mockAuthentication(userId = "user-1")
        every { userRepository.findById("user-1") } returns Mono.just(user)

        StepVerifier.create(service.findUserByAuthentication(auth)).expectNext(user).verifyComplete()
    }

    // endregion

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

    // region findUserName

    @Test
    fun `findUserName returns name`() {
        val user = BackendFixtures.user(id = "user-1", name = "alice")
        every { userRepository.findById("user-1") } returns Mono.just(user)

        StepVerifier.create(service.findUserName("user-1")).expectNext("alice").verifyComplete()
    }

    @Test
    fun `findUserName returns unknown for missing User`() {
        every { userRepository.findById("nobody") } returns Mono.empty()

        StepVerifier.create(service.findUserName("nobody")).expectNext("UNKNOWN").verifyComplete()
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
        every { userRepository.deleteById("user-1") } returns Mono.empty()

        StepVerifier.create(service.deleteUserById("user-1")).verifyComplete()

        verify(exactly = 1) { userRepository.deleteById("user-1") }
    }

    // endregion

    // region notifyAllUsersWithStatus

    @Test
    fun `notifyAllUsersWithStatus publishes to notifier`() {
        val user1 = BackendFixtures.user(id = "user-1", name = "alice")
        val user2 = BackendFixtures.user(id = "user-2", name = "bob")
        every { userRepository.findAllByStatus(UserStatus.ACTIVE) } returns Flux.just(user1, user2)
        every { notifier.notify(any<BaseNotificationCreateRequest>()) } returns Mono.just("notif-id")

        StepVerifier.create(service.notifyAllUsersWithStatus("Title", "Hello", UserStatus.ACTIVE))
            .assertNext { count -> assertThat(count).isEqualTo(2L) }
            .verifyComplete()

        verify(exactly = 2) { notifier.notify(any()) }
    }

    // endregion
}
