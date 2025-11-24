package io.github.sanyavertolet.edukate.backend.permissions;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SubmissionPermissionEvaluator {
    public Boolean isOwner(Submission submission, String userId) {
        return submission.getUserId().equals(userId);
    }
}
