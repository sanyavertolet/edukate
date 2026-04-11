package io.github.sanyavertolet.edukate.gateway.services

import io.github.sanyavertolet.edukate.gateway.GatewayFixtures
import io.github.sanyavertolet.edukate.gateway.configs.GatewayProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class BackendServiceTest {
    private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val exchangeFunction: ExchangeFunction = mockk()
    private val webClientBuilder = WebClient.builder().exchangeFunction(exchangeFunction)
    private val gatewayProperties = GatewayProperties(GatewayProperties.Backend("http://test-backend"))
    private val backendService = BackendService(gatewayProperties, webClientBuilder)

    private fun mockJsonResponse(status: HttpStatus, body: Any): ClientResponse =
        ClientResponse.create(status)
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(objectMapper.writeValueAsString(body))
            .build()

    private fun mockErrorResponse(status: HttpStatus): ClientResponse =
        ClientResponse.create(status).header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body("{}").build()

    // ── saveUser ──────────────────────────────────────────────────────────────

    @Test
    fun `saveUser sends POST to internal users endpoint and returns UserCredentials`() {
        val credentials = GatewayFixtures.userCredentials()
        val requestSlot = slot<ClientRequest>()
        every { exchangeFunction.exchange(capture(requestSlot)) } returns
            Mono.just(mockJsonResponse(HttpStatus.OK, credentials))

        StepVerifier.create(backendService.saveUser(credentials))
            .expectNextMatches { it.username == GatewayFixtures.USER_NAME }
            .verifyComplete()

        assertThat(requestSlot.captured.method().name()).isEqualTo("POST")
        assertThat(requestSlot.captured.url().path).isEqualTo("/internal/users")
    }

    @Test
    fun `saveUser propagates WebClientResponseException on 5xx`() {
        val credentials = GatewayFixtures.userCredentials()
        every { exchangeFunction.exchange(any()) } returns Mono.just(mockErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR))

        StepVerifier.create(backendService.saveUser(credentials)).expectError().verify()
    }

    // ── getUserByName ─────────────────────────────────────────────────────────

    @Test
    fun `getUserByName sends GET to by-name endpoint with correct path variable`() {
        val credentials = GatewayFixtures.userCredentials()
        val requestSlot = slot<ClientRequest>()
        every { exchangeFunction.exchange(capture(requestSlot)) } returns
            Mono.just(mockJsonResponse(HttpStatus.OK, credentials))

        StepVerifier.create(backendService.getUserByName(GatewayFixtures.USER_NAME))
            .expectNextMatches { it.username == GatewayFixtures.USER_NAME }
            .verifyComplete()

        assertThat(requestSlot.captured.url().path).isEqualTo("/internal/users/by-name/${GatewayFixtures.USER_NAME}")
    }

    @Test
    fun `getUserByName returns empty Mono when backend returns 404`() {
        every { exchangeFunction.exchange(any()) } returns Mono.just(mockErrorResponse(HttpStatus.NOT_FOUND))

        StepVerifier.create(backendService.getUserByName(GatewayFixtures.USER_NAME)).verifyComplete()
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    fun `getUserById sends GET to by-id endpoint with correct path variable`() {
        val credentials = GatewayFixtures.userCredentials()
        val requestSlot = slot<ClientRequest>()
        every { exchangeFunction.exchange(capture(requestSlot)) } returns
            Mono.just(mockJsonResponse(HttpStatus.OK, credentials))

        StepVerifier.create(backendService.getUserById(GatewayFixtures.USER_ID))
            .expectNextMatches { it.id == GatewayFixtures.USER_ID }
            .verifyComplete()

        assertThat(requestSlot.captured.url().path).isEqualTo("/internal/users/by-id/${GatewayFixtures.USER_ID}")
    }

    @Test
    fun `getUserById returns empty Mono when backend returns 404`() {
        every { exchangeFunction.exchange(any()) } returns Mono.just(mockErrorResponse(HttpStatus.NOT_FOUND))

        StepVerifier.create(backendService.getUserById(GatewayFixtures.USER_ID)).verifyComplete()
    }
}
