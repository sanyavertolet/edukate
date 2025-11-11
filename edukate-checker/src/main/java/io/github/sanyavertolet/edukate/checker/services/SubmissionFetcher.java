package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubmissionFetcher {
    private final WebClient backendClient;

    public Mono<SubmissionContext> fetch(String submissionId) {
        return backendClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/checker/context")
                        .queryParam("id", submissionId)
                        .build()
                )
                .retrieve()
                .bodyToMono(SubmissionContext.class);
    }
}
