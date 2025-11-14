package io.github.sanyavertolet.edukate.backend.permissions;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class SubmissionPermissionEvaluator {
    private final SubmissionService submissionService;

    public Mono<Boolean> isOwner(String submissionId, String userId) {
        return submissionService.findSubmissionById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .map(submission -> submission.getUserId().equals(userId));
    }

    public Mono<Submission> getSubmissionIfOwns(String submissionId, String userId) {
        return submissionService.findSubmissionById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submission.getUserId().equals(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")));
    }
}
