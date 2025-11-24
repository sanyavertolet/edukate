package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class SubmissionContextListener {
    private final CheckerService checkerService;
    private final ResultPublisher resultPublisher;

    @RabbitListener(queues = RabbitTopology.Q_SCHEDULE_CHECKER)
    public void onSubmissionContext(SubmissionContext context) {
        Mono.just(context)
                .doOnNext(submissionContext -> log.debug("received submissionContext={}", submissionContext))
                .flatMap(checkerService::runCheck)
                .flatMap(resultPublisher::publish)
                .doOnSuccess(_ ->
                        log.debug("Successfully published result for submission {}", context.getSubmissionId())
                )
                .doOnError(ex ->
                        log.error("Failed to process or publish result for submission {}", context.getSubmissionId(), ex)
                )
                .block();
    }
}
