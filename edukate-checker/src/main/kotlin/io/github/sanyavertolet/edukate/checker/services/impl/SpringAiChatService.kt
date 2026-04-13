package io.github.sanyavertolet.edukate.checker.services.impl

import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.checker.services.ChatService
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
@Profile("!silent")
class SpringAiChatService(
    chatModel: ChatModel,
    @param:Value($$"${checker.openai.system-prompt}") private val systemPromptTemplate: String,
) : ChatService {
    private val chatClient = ChatClient.builder(chatModel).defaultAdvisors(SimpleLoggerAdvisor()).build()

    override fun makeRequest(ctx: RequestContext): Mono<ModelResponse> =
        Mono.fromCallable { callModel(ctx) }
            .doOnSuccess { log.debug("Successfully retrieved AI response") }
            .subscribeOn(Schedulers.boundedElastic())

    private fun callModel(ctx: RequestContext): ModelResponse {
        val allMedia = (ctx.problemImages + ctx.submissionImages).toTypedArray()
        return requireNotNull(
            chatClient
                .prompt()
                .system { s -> s.text(systemPromptTemplate).param("problemText", ctx.problemText) }
                .user { u -> u.text("Here are the images for you to check as well as the problem image.").media(*allMedia) }
                .call()
                .entity(ModelResponse::class.java)
        ) {
            "AI returned null response"
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SpringAiChatService::class.java)
    }
}
