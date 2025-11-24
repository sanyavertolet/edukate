package io.github.sanyavertolet.edukate.checker.services.impl;

import io.github.sanyavertolet.edukate.checker.domain.RequestContext;
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse;
import io.github.sanyavertolet.edukate.checker.services.ChatService;
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType;
import io.github.sanyavertolet.edukate.common.checks.CheckStatus;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Profile("silent")
@Service
public class NoopChatService implements ChatService {

    @Override
    public Mono<ModelResponse> makeRequest(@NonNull RequestContext ctx) {
        return Mono.fromCallable(() -> new ModelResponse(CheckStatus.SUCCESS, 0.01f, CheckErrorType.NONE, "stub"))
                .doOnNext(stub -> log.warn("Chat service is disabled, returning stub response: {}.", stub));
    }

    @PostConstruct
    public void warningPostConstruct() {
        log.warn("Chat service is disabled, all requests will be ignored.");
    }
}
