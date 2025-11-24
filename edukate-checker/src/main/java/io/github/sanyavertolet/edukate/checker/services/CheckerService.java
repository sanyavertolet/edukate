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
                .flatMap(chatService::makeRequest)
                .map(modelResponse -> CheckResultMessageUtils.success(modelResponse, context))
                .onErrorResume(ex -> Mono.just(CheckResultMessageUtils.error(context))
                        .doOnNext(_ -> log.error("Failed to check submission", ex))
                );
    }

    private Mono<RequestContext> buildRequestContext(SubmissionContext submissionContext) {
        return Mono.zip(
                mediaContentResolver.resolveMedia(submissionContext.getProblemImageUrls()).collectList(),
                mediaContentResolver.resolveMedia(submissionContext.getSubmissionImageUrls()).collectList(),
                (problemMedia, submissionMedia) ->
                        new RequestContext(submissionContext.getProblemText(), problemMedia, submissionMedia)
                );
    }
}
