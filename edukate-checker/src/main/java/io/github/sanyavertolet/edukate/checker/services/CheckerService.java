package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.checker.domain.RequestContext;
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CheckerService {
    private final SubmissionFetcher submissionFetcher;
    private final AiService aiService;
    private final MediaContentResolver mediaContentResolver;
    private final ResultPublisher resultPublisher;

    public Mono<Void> runCheck(SubmissionContext context) {
        return buildRequestContext(context)
                .flatMap(aiService::evaluate)
                .map(modelResponse -> modelResponse.toCheckResult(context.getSubmissionId()))
                // todo: create a stub of CheckResult and report an error occurred
                .flatMap(resultPublisher::publish)
                // todo: notify the user about the result
                .then();
    }

    public Mono<Void> runCheck(String submissionId) {
        return submissionFetcher.fetch(submissionId).flatMap(this::runCheck);
    }

    private Mono<RequestContext> buildRequestContext(SubmissionContext submissionContext) {
        return Mono.just(submissionContext)
                .flatMap(ctx -> Mono.zip(
                        mediaContentResolver.resolveMedia(ctx.getProblemImageUrls()).collectList(),
                        mediaContentResolver.resolveMedia(ctx.getSubmissionImageUrls()).collectList()
                ))
                .map(tuple -> new RequestContext(submissionContext.getProblemText(), tuple.getT1(), tuple.getT2()));
    }
}
