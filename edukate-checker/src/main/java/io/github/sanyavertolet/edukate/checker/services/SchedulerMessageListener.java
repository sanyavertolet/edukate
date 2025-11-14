package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import io.github.sanyavertolet.edukate.messaging.RabbitTopology;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class SchedulerMessageListener {
    private final SchedulerService schedulerService;
    private final CheckerService checkerService;

    @RabbitListener(queues = RabbitTopology.Q_SCHEDULE_CHECKER)
    public void onScheduleMessage(SubmissionContext context) {
        log.info("[checker] schedule received submissionId={}", context.getSubmissionId());
        schedulerService.schedule(() -> checkerService.runCheck(context)).subscribe();
    }
}
