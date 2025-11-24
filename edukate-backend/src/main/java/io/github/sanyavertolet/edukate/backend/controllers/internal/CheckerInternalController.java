package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Hidden
@RequiredArgsConstructor
@RequestMapping("/internal/checker")
@RestController
public class CheckerInternalController {
    private final SubmissionService submissionService;
    private final CheckerSchedulerService checkerSchedulerService;

    @PostMapping("/ai")
    public Mono<Void> checkSubmission(@RequestParam("id") String submissionId) {
        return submissionService.findById(submissionId).flatMap(checkerSchedulerService::scheduleCheck);
    }
}
