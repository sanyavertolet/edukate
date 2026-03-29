package io.github.sanyavertolet.edukate.checker.services.impl

import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.checker.services.ChatService
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.content.Media
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
@Profile("!silent")
class SpringAiChatService(private val chatClient: ChatClient) : ChatService {
    override fun makeRequest(ctx: RequestContext): Mono<ModelResponse> =
        Mono.fromCallable { callModel(ctx) }
            .doOnSuccess { log.debug("Successfully retrieved AI response") }
            .subscribeOn(Schedulers.boundedElastic())

    private fun callModel(ctx: RequestContext): ModelResponse =
        structuredCallModel(ctx.problemText, ctx.problemImages, ctx.submissionImages)

    @Suppress("JavaStyleCallReplaceableByKotlinExtension")
    private fun structuredCallModel(
        problemText: String,
        problemMedia: List<Media>,
        submissionMedia: List<Media>,
    ): ModelResponse =
        requireNotNull(
            chatClient
                .prompt()
                .system { s -> s.param("problemText", problemText) }
                .user { u ->
                    u.text("Here are the images for you to check as well as the problem image.")
                        .media(*problemMedia.toTypedArray())
                        .media(*submissionMedia.toTypedArray())
                }
                .call()
                .entity(ModelResponse::class.java)
        ) {
            "AI returned null response"
        }

    companion object {
        private val log = LoggerFactory.getLogger(SpringAiChatService::class.java)
    }
}
