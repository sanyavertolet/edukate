package io.github.sanyavertolet.edukate.checker.configs;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(
            ChatClient.Builder builder,
            @Value("${checker.openai.system-prompt}") String defaultSystemPrompt
    ) {
        return builder.defaultSystem(defaultSystemPrompt).build();
    }
}
