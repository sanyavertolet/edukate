package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class CheckerSchedulerService {
    private final RabbitTemplate rabbitTemplate;
    private final CheckResultService checkResultService;
    private final SubmissionService submissionService;

    public Mono<Void> scheduleCheck(@NonNull Submission submission) {
        return submissionService.prepareContext(submission)
                .doOnNext(ctx ->
                        rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.RK_SCHEDULE, ctx)
                )
                .then();
    }

    @RabbitListener(queues = RabbitTopology.Q_RESULT_BACKEND)
    public void onResult(@NonNull CheckResult checkResult) {
        log.info("Received result for submission {}", checkResult.getSubmissionId());
        checkResultService.save(checkResult)
                .doOnError(ex -> log.error("Failed to persist result {}", checkResult.getSubmissionId(), ex))
                .subscribe();
    }
}
