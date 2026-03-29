@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.checker.services.impl.SpringAiChatService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.function.Consumer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.content.Media
import reactor.test.StepVerifier

class SpringAiChatServiceTest {
    private val chatClient = mockk<ChatClient>(relaxed = true)
    private val promptSpec = mockk<ChatClient.ChatClientRequestSpec>(relaxed = true)
    private val callSpec = mockk<ChatClient.CallResponseSpec>(relaxed = true)
    private lateinit var service: SpringAiChatService

    private val problemMedia = listOf(CheckerFixtures.mockMedia())
    private val submissionMedia = listOf(CheckerFixtures.mockMedia(), CheckerFixtures.mockMedia())
    private val ctx = RequestContext("Solve x^2 = 4", problemMedia, submissionMedia)
    private val expectedResponse = CheckerFixtures.modelResponse()

    @BeforeEach
    fun setUp() {
        service = SpringAiChatService(chatClient)
        every { chatClient.prompt() } returns promptSpec
        every { promptSpec.system(any<Consumer<ChatClient.PromptSystemSpec>>()) } returns promptSpec
        every { promptSpec.user(any<Consumer<ChatClient.PromptUserSpec>>()) } returns promptSpec
        every { promptSpec.call() } returns callSpec
        every { callSpec.entity(ModelResponse::class.java) } returns expectedResponse
    }

    @Test
    fun `makeRequest emits the entity returned by ChatClient`() {
        StepVerifier.create(service.makeRequest(ctx))
            .assertNext { response ->
                assertThat(response).isEqualTo(expectedResponse)
            }
            .verifyComplete()
    }

    @Test
    fun `makeRequest completes without error on happy path`() {
        StepVerifier.create(service.makeRequest(ctx))
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `problem text is set as system param`() {
        val systemConsumerSlot = slot<Consumer<ChatClient.PromptSystemSpec>>()
        every { promptSpec.system(capture(systemConsumerSlot)) } returns promptSpec

        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()

        val systemSpec = mockk<ChatClient.PromptSystemSpec>(relaxed = true)
        systemConsumerSlot.captured.accept(systemSpec)
        verify { systemSpec.param("problemText", ctx.problemText) }
    }

    @Test
    fun `problem images are passed to user spec`() {
        val userConsumerSlot = slot<Consumer<ChatClient.PromptUserSpec>>()
        every { promptSpec.user(capture(userConsumerSlot)) } returns promptSpec

        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()

        val userSpec = mockk<ChatClient.PromptUserSpec>(relaxed = true)
        every { userSpec.text(any<String>()) } returns userSpec
        every { userSpec.media(*anyVararg<Media>()) } returns userSpec
        userConsumerSlot.captured.accept(userSpec)

        verify { userSpec.media(*problemMedia.toTypedArray()) }
    }

    @Test
    fun `submission images are passed to user spec`() {
        val userConsumerSlot = slot<Consumer<ChatClient.PromptUserSpec>>()
        every { promptSpec.user(capture(userConsumerSlot)) } returns promptSpec

        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()

        val userSpec = mockk<ChatClient.PromptUserSpec>(relaxed = true)
        every { userSpec.text(any<String>()) } returns userSpec
        every { userSpec.media(*anyVararg<Media>()) } returns userSpec
        userConsumerSlot.captured.accept(userSpec)

        verify { userSpec.media(*submissionMedia.toTypedArray()) }
    }

    @Test
    fun `null entity response throws`() {
        every { callSpec.entity(ModelResponse::class.java) } returns null

        StepVerifier.create(service.makeRequest(ctx))
            .expectError()
            .verify()
    }
}
