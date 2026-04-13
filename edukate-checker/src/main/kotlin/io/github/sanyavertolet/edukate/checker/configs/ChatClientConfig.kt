package io.github.sanyavertolet.edukate.checker.configs

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Flux

@Configuration
class ChatClientConfig {
    /**
     * Wraps the auto-configured [OpenAiChatModel] to fix a Spring AI 2.0.0-SNAPSHOT regression: [OpenAiChatOptions] does not
     * override [ChatOptions.mutate], which [org.springframework.ai.chat.client.DefaultChatClientUtils] unconditionally
     * invokes on every [org.springframework.ai.chat.client.ChatClient] call. Returns [ChatOptions.builder]-based
     * [org.springframework.ai.chat.prompt.DefaultChatOptions] that properly implements [ChatOptions.mutate].
     *
     * Remove this workaround once Spring AI ships a fixed [OpenAiChatOptions.mutate].
     */
    @Bean
    @Primary
    fun chatModel(openAiChatModel: OpenAiChatModel): ChatModel =
        object : ChatModel {
            override fun call(prompt: Prompt): ChatResponse = openAiChatModel.call(prompt)

            override fun stream(prompt: Prompt): Flux<ChatResponse> = openAiChatModel.stream(prompt)

            override fun getDefaultOptions(): ChatOptions {
                val opts = openAiChatModel.defaultOptions
                return ChatOptions.builder().model((opts as? OpenAiChatOptions)?.model).temperature(opts.temperature).build()
            }
        }
}
