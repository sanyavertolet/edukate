package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.checker.domain.RequestContext;
import io.github.sanyavertolet.edukate.checker.utils.CheckResultMessageUtils;
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage;
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckerService {
    private final ChatService chatService;
    private final MediaContentResolver mediaContentResolver;

    public Mono<CheckResultMessage> runCheck(SubmissionContext context) {
        return buildRequestContext(context)
                .doOnNext(ctx -> log.debug("Got {} problem and {} submission images processed",
                        ctx.problemImages().size(), ctx.submissionImages().size()))
                .flatMap(chatService::makeRequest)
                .switchIfEmpty(Mono.error(new IllegalStateException("No AI response received")))
                .map(modelResponse -> CheckResultMessageUtils.success(modelResponse, context))
                .doOnSuccess(_ -> log.debug("Successfully checked submission {}", context.getSubmissionId()))
                .doOnError(ex -> log.error("Failed to check submission {}", context.getSubmissionId(), ex))
                .onErrorReturn(CheckResultMessageUtils.error(context));
    }

    private Mono<RequestContext> buildRequestContext(SubmissionContext submissionContext) {
        return Mono.zip(
                mediaContentResolver.resolveMedia(submissionContext.getProblemImageRawKeys()).collectList(),
                mediaContentResolver.resolveMedia(submissionContext.getSubmissionImageRawKeys()).collectList(),
                (problemMedia, submissionMedia) ->
                        new RequestContext(submissionContext.getProblemText(), problemMedia, submissionMedia)
                );
    }
}
