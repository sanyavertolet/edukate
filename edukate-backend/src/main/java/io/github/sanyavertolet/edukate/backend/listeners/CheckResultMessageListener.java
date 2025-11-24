package io.github.sanyavertolet.edukate.backend.listeners;

import io.github.sanyavertolet.edukate.backend.entities.CheckResult;
import io.github.sanyavertolet.edukate.backend.services.CheckResultService;
import io.github.sanyavertolet.edukate.common.checks.CheckResultMessage;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class CheckResultMessageListener {
    private final CheckResultService checkResultService;

    @RabbitListener(queues = RabbitTopology.Q_RESULT_BACKEND)
    public void onCheckResultMessage(@NonNull CheckResultMessage checkResultMessage) {
        log.info("Received result for submission {}", checkResultMessage.getSubmissionId());

        buildCheckResult(checkResultMessage)
                .flatMap(checkResultService::saveAndUpdateSubmission)
                .doOnError(ex -> log.error("Failed to persist result {}", checkResultMessage.getSubmissionId(), ex))
                // todo: notify user that the result is available
                .block();
    }

    private Mono<CheckResult> buildCheckResult(CheckResultMessage checkResultMessage) {
        return Mono.fromCallable(() -> CheckResult.builder()
                .submissionId(checkResultMessage.getSubmissionId())
                .status(checkResultMessage.getStatus())
                .trustLevel(checkResultMessage.getTrustLevel())
                .errorType(checkResultMessage.getErrorType())
                .explanation(checkResultMessage.getExplanation())
                .build()
        );
    }
}
