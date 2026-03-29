package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import reactor.core.publisher.Mono

fun interface ChatService {
    fun makeRequest(ctx: RequestContext): Mono<ModelResponse>
}
