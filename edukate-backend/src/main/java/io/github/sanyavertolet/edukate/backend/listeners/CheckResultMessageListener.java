package io.github.sanyavertolet.edukate.backend.listeners;

import io.github.sanyavertolet.edukate.backend.entities.CheckResult;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.services.CheckResultService;
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage;
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.services.Notifier;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@AllArgsConstructor
public class CheckResultMessageListener {
    private final CheckResultService checkResultService;
    private final Notifier notifier;

    @RabbitListener(queues = RabbitTopology.Q_RESULT_BACKEND)
    public void onCheckResultMessage(@NonNull CheckResultMessage checkResultMessage) {
        log.debug("Received result for submission {}", checkResultMessage.getSubmissionId());

        buildCheckResult(checkResultMessage)
                .flatMap(checkResultService::saveAndUpdateSubmission)
                 .map(tuple -> prepareNotification(tuple.getT1(), tuple.getT2()))
                .doOnSuccess(notifier::notify)
                .doOnError(ex -> log.error("Failed to persist result {}", checkResultMessage.getSubmissionId(), ex))
                .block();
    }

    private CheckedNotificationCreateRequest prepareNotification(
            CheckResult checkResult, Submission submission
    ) {
        String submissionId = Objects.requireNonNull(submission.getId(), "Submission ID must not be null");
        return CheckedNotificationCreateRequest.from(
                submission.getUserId(),
                submissionId,
                submission.getProblemId(),
                checkResult.getStatus()
        );
    }

    private Mono<CheckResult> buildCheckResult(CheckResultMessage checkResultMessage) {
        return Mono.fromCallable(() -> CheckResult.fromCheckResultMessage(checkResultMessage));
    }
}
