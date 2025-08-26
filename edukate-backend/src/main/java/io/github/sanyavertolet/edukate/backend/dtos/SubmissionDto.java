package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class SubmissionDto {
    private String id;
    private String problemId;
    @With
    private String userName;
    private Submission.Status status;
    private Instant createdAt;
    @With
    private List<String> fileUrls;
}
