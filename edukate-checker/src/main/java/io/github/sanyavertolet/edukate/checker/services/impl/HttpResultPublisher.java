package io.github.sanyavertolet.edukate.checker.services.impl;

import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import io.github.sanyavertolet.edukate.checker.services.ResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(ResultPublisher.class)
public class HttpResultPublisher implements ResultPublisher {
    private final WebClient backendClient;

    @Override
    public Mono<Void> publish(CheckResult result) {
        return backendClient.post()
                .uri("/internal/checker/report")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(ex -> log.error("Backend publish error", ex));
    }
}
