package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.common.SubmissionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(value = "submissions")
public class Submission {
    @Id
    private String id;
    private String problemId;
    private String userId;
    private SubmissionStatus status;
    private List<String> fileObjectIds;

    @CreatedDate
    private Instant createdAt;

    public Submission(String problemId, String userId) {
        this.problemId = problemId;
        this.userId = userId;
        this.status = SubmissionStatus.PENDING;
        this.fileObjectIds = new ArrayList<>();
    }

    public static Submission of(String problemId, String userId) {
        return new Submission(problemId, userId);
    }
}
