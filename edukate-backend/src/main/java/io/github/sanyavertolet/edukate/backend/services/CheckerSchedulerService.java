package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@AllArgsConstructor
public class CheckerSchedulerService {
    private final RabbitTemplate rabbitTemplate;
    private final SubmissionService submissionService;

    public Mono<Void> scheduleCheck(@NonNull Submission submission) {
        return submissionService.prepareContext(submission)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(ctx -> Mono.fromRunnable(() ->
                        rabbitTemplate.convertAndSend(RabbitTopology.EXCHANGE, RabbitTopology.Rk.SCHEDULE, ctx)
                ));
    }
}
