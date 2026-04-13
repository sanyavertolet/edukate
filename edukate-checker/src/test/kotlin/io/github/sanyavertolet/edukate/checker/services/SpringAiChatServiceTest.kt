@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.services.impl.SpringAiChatService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import reactor.test.StepVerifier

class SpringAiChatServiceTest {
    private val chatModel = mockk<ChatModel>()
    private val systemPromptTemplate = "Check the solution for: {problemText}"
    private lateinit var service: SpringAiChatService

    private val problemMedia = listOf(CheckerFixtures.mockMedia())
    private val submissionMedia = listOf(CheckerFixtures.mockMedia(), CheckerFixtures.mockMedia())
    private val ctx = RequestContext("Solve x^2 = 4", problemMedia, submissionMedia)
    private val expectedResponse = CheckerFixtures.modelResponse()

    @BeforeEach
    fun setUp() {
        service = SpringAiChatService(chatModel, systemPromptTemplate)
        every { chatModel.getDefaultOptions() } returns ChatOptions.builder().build()
        val responseJson = """{"status":"SUCCESS","trustLevel":0.9,"errorType":"NONE","explanation":"Correct."}"""
        every { chatModel.call(any<Prompt>()) } returns ChatResponse(listOf(Generation(AssistantMessage(responseJson))))
    }

    @Test
    fun `makeRequest emits the entity returned by ChatModel`() {
        StepVerifier.create(service.makeRequest(ctx))
            .assertNext { response ->
                assertThat(response.status).isEqualTo(expectedResponse.status)
                assertThat(response.explanation).isEqualTo(expectedResponse.explanation)
            }
            .verifyComplete()
    }

    @Test
    fun `makeRequest completes without error on happy path`() {
        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()
    }

    @Test
    fun `problem text is substituted into system message`() {
        val promptSlot = slot<Prompt>()
        every { chatModel.call(capture(promptSlot)) } returns
            ChatResponse(
                listOf(
                    Generation(
                        AssistantMessage("""{"status":"SUCCESS","trustLevel":0.9,"errorType":"NONE","explanation":"ok"}""")
                    )
                )
            )

        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()

        val systemMessages = promptSlot.captured.instructions.filter { it.messageType.value == "system" }
        assertThat(systemMessages).isNotEmpty
        assertThat(systemMessages.first().getText()).contains(ctx.problemText)
    }

    @Test
    fun `all problem and submission images are included in the prompt`() {
        val promptSlot = slot<Prompt>()
        every { chatModel.call(capture(promptSlot)) } returns
            ChatResponse(
                listOf(
                    Generation(
                        AssistantMessage("""{"status":"SUCCESS","trustLevel":0.9,"errorType":"NONE","explanation":"ok"}""")
                    )
                )
            )

        StepVerifier.create(service.makeRequest(ctx)).expectNextCount(1).verifyComplete()

        val allExpectedMedia = problemMedia + submissionMedia
        val userMessages = promptSlot.captured.instructions.filter { it.messageType.value == "user" }
        assertThat(userMessages).isNotEmpty
        val actualMedia =
            userMessages.flatMap { msg -> (msg as? org.springframework.ai.chat.messages.UserMessage)?.media ?: emptyList() }
        assertThat(actualMedia).hasSize(allExpectedMedia.size)
    }

    @Test
    fun `null text response from model throws`() {
        every { chatModel.call(any<Prompt>()) } returns ChatResponse(listOf(Generation(AssistantMessage(null))))

        StepVerifier.create(service.makeRequest(ctx)).expectError(IllegalArgumentException::class.java).verify()
    }
}
