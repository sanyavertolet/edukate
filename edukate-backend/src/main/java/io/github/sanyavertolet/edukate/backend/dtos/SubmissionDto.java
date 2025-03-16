package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.Instant;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class SubmissionDto {
    private String id;
    private String problemId;
    private String userId;
    private Submission.Status status;
    private Instant createdAt;
}
