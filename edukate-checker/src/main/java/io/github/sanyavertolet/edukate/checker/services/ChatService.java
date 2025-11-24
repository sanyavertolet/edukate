package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.checker.domain.RequestContext;
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse;
import lombok.NonNull;
import reactor.core.publisher.Mono;

public interface ChatService {
    Mono<ModelResponse> makeRequest(@NonNull RequestContext ctx);
}
