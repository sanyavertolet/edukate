package io.github.sanyavertolet.edukate.checker.services

import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.utils.error
import io.github.sanyavertolet.edukate.checker.utils.success
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CheckerService(private val chatService: ChatService, private val mediaContentResolver: MediaContentResolver) {

    fun runCheck(context: SubmissionContext): Mono<CheckResultMessage> =
        buildRequestContext(context)
            .doOnNext { ctx ->
                log.debug(
                    "Got {} problem and {} submission images processed",
                    ctx.problemImages.size,
                    ctx.submissionImages.size,
                )
            }
            .flatMap(chatService::makeRequest)
            .switchIfEmpty(Mono.error(IllegalStateException("No AI response received")))
            .map { modelResponse -> success(modelResponse, context) }
            .doOnSuccess { log.debug("Successfully checked submission {}", context.submissionId) }
            .doOnError { ex -> log.error("Failed to check submission {}", context.submissionId, ex) }
            .onErrorReturn(error(context))

    private fun buildRequestContext(submissionContext: SubmissionContext): Mono<RequestContext> =
        Mono.zip(
            mediaContentResolver.resolveMedia(submissionContext.problemImageRawKeys).collectList(),
            mediaContentResolver.resolveMedia(submissionContext.submissionImageRawKeys).collectList(),
        ) { problemMedia, submissionMedia ->
            RequestContext(submissionContext.problemText, problemMedia, submissionMedia)
        }

    companion object {
        private val log = LoggerFactory.getLogger(CheckerService::class.java)
    }
}
