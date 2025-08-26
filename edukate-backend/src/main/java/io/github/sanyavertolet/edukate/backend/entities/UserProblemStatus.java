package io.github.sanyavertolet.edukate.backend.entities;

import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document("problem_status")
@CompoundIndex(name = "uniq_user_problem", def = "{ 'userId': 1, 'problemId': 1 }", unique = true)
public class UserProblemStatus {
    private String userId;

    private String problemId;

    private Submission.Status latestStatus;

    private Instant latestTime;

    private String latestSubmissionId;

    private Submission.Status bestStatus;

    private Instant bestTime;

    private String bestSubmissionId;
}
