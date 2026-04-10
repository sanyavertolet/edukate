package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.gateway.GatewayFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserDetailsServiceTest {
    private val backendService: BackendService = mockk()
    private val userDetailsService = UserDetailsService(backendService)

    private val credentials = GatewayFixtures.userCredentials()

    @Test
    fun `findByUsername delegates to BackendService and returns UserDetails`() {
        every { backendService.getUserByName(GatewayFixtures.USER_NAME) } returns Mono.just(credentials)

        StepVerifier.create(userDetailsService.findByUsername(GatewayFixtures.USER_NAME))
            .expectNextMatches { it.username == GatewayFixtures.USER_NAME }
            .verifyComplete()
    }

    @Test
    fun `findEdukateUserDetailsByUsername returns EdukateUserDetails with all fields mapped`() {
        every { backendService.getUserByName(GatewayFixtures.USER_NAME) } returns Mono.just(credentials)

        StepVerifier.create(userDetailsService.findEdukateUserDetailsByUsername(GatewayFixtures.USER_NAME))
            .expectNextMatches { details ->
                details.id == GatewayFixtures.USER_ID &&
                    details.username == GatewayFixtures.USER_NAME &&
                    details.status == credentials.status &&
                    details.roles == credentials.roles
            }
            .verifyComplete()
    }

    @Test
    fun `findById maps UserCredentials to EdukateUserDetails`() {
        every { backendService.getUserById(GatewayFixtures.USER_ID) } returns Mono.just(credentials)

        StepVerifier.create(userDetailsService.findById(GatewayFixtures.USER_ID))
            .expectNextMatches { details ->
                details.id == GatewayFixtures.USER_ID && details.username == GatewayFixtures.USER_NAME
            }
            .verifyComplete()
    }

    @Test
    fun `isNotUserPresent returns true when getUserByName emits empty`() {
        every { backendService.getUserByName(GatewayFixtures.USER_NAME) } returns Mono.empty()

        StepVerifier.create(userDetailsService.isNotUserPresent(GatewayFixtures.USER_NAME)).expectNext(true).verifyComplete()
    }

    @Test
    fun `isNotUserPresent returns false when user exists`() {
        every { backendService.getUserByName(GatewayFixtures.USER_NAME) } returns Mono.just(credentials)

        StepVerifier.create(userDetailsService.isNotUserPresent(GatewayFixtures.USER_NAME))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `create calls BackendService saveUser with new user credentials`() {
        every { backendService.saveUser(any()) } returns Mono.just(credentials)

        StepVerifier.create(
                userDetailsService.create(GatewayFixtures.USER_NAME, GatewayFixtures.EMAIL, GatewayFixtures.ENCODED_PASSWORD)
            )
            .expectNextMatches { details ->
                assertThat(details.id).isEqualTo(GatewayFixtures.USER_ID)
                true
            }
            .verifyComplete()

        verify(exactly = 1) { backendService.saveUser(any()) }
    }
}
