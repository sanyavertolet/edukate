package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator;
import io.github.sanyavertolet.edukate.backend.services.CheckResultService;
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

//@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/checker")
public class CheckerController {
    private final CheckResultService checkResultService;
    private final CheckerSchedulerService checkerSchedulerService;
    private final SubmissionPermissionEvaluator submissionPermissionEvaluator;

    @PostMapping("/ai")
    public Mono<ResponseEntity<Void>> aiCheck(@RequestParam String submissionId, Authentication authentication) {
        return submissionPermissionEvaluator.getSubmissionIfOwns(submissionId, AuthUtils.id(authentication))
                .flatMap(checkerSchedulerService::scheduleCheck)
                .thenReturn(ResponseEntity.accepted().build());
    }

    @PostMapping("/self")
    public Mono<ResponseEntity<Void>> selfCheck(@RequestParam String submissionId, Authentication authentication) {
        return Mono.just(submissionId)
                .filterWhen(id -> submissionPermissionEvaluator.isOwner(id, AuthUtils.id(authentication)))
                .flatMap(checkResultService::saveSelfCheckResult)
                .map(_ -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    @SuppressWarnings("unused")
    @PostMapping("/supervisor")
    public Mono<ResponseEntity<Void>> supervisorCheck(@RequestParam String submissionId, Authentication authentication) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }
}
