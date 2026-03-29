package io.github.sanyavertolet.edukate.checker.configs

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {
    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        @Value("\${checker.openai.system-prompt}") defaultSystemPrompt: String,
    ): ChatClient = builder.defaultSystem(defaultSystemPrompt).build()
}
