package io.github.sanyavertolet.edukate.checker.services.impl

import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.checker.services.ChatService
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Profile("silent")
@Service
class NoopChatService : ChatService {
    override fun makeRequest(ctx: RequestContext): Mono<ModelResponse> =
        Mono.fromCallable { ModelResponse(CheckStatus.SUCCESS, STUB_TRUST_LEVEL, CheckErrorType.NONE, "stub") }
            .doOnNext { stub -> log.warn("Chat service is disabled, returning stub response: {}.", stub) }

    @PostConstruct
    fun warningPostConstruct() {
        log.warn("Chat service is disabled, all requests will be ignored.")
    }

    companion object {
        private val log = LoggerFactory.getLogger(NoopChatService::class.java)
        private const val STUB_TRUST_LEVEL = 0.01f
    }
}
