package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.checker.domain.RequestContext;
import io.github.sanyavertolet.edukate.common.checks.ModelResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class AiService {
    private final ChatClient chatClient;

    public Mono<ModelResponse> evaluate(RequestContext ctx) {
        return Mono.just(ctx)
                .map(this::callModel)
                .doOnSuccess(_ -> log.debug("Successfully retrieved AI response."))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private ModelResponse callModel(RequestContext ctx) {
        return structuredCallModel(ctx.problemText(), ctx.problemImages(), ctx.submissionImages());
    }

    private ModelResponse structuredCallModel(String problemText, List<Media> problemMedia, List<Media> submissionMedia) {
        return chatClient.prompt()
                .system(s -> s.param("problemText", problemText))
                .user(u -> u
                        .text("Here are the images for you to check as well as the problem image.")
                        .media(problemMedia.toArray(new Media[0]))
                        .media(submissionMedia.toArray(new Media[0]))
                )
                .call()
                .entity(ModelResponse.class);
    }
}
