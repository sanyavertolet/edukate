package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.CheckResultService;
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Hidden
@RequiredArgsConstructor
@RequestMapping("/internal/checker")
@RestController
public class CheckerInternalController {
    private final SubmissionService submissionService;
    private final CheckResultService checkResultService;
    private final CheckerSchedulerService checkerSchedulerService;

    @PostMapping("/report")
    public Mono<Void> reportCheckerResult(@RequestBody CheckResult checkResult) {
        return checkResultService.save(checkResult).then();
    }

    @GetMapping("/context")
    public Mono<SubmissionContext> getSubmissionContextById(@RequestParam(name = "id") String submissionId) {
        return submissionService.findSubmissionById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .flatMap(submissionService::prepareContext);
    }

    @PostMapping("/ai")
    public Mono<Void> checkSubmission(@RequestParam("id") String submissionId) {
        return submissionService.findSubmissionById(submissionId)
                .flatMap(checkerSchedulerService::scheduleCheck);
    }
}
