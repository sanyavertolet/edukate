package io.github.sanyavertolet.edukate.backend.entities.files;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("submission")
@JsonTypeName("submission")
public class SubmissionFileKey extends FileKey {
    @Getter
    private final String userId;
    @Getter
    private final String problemId;
    @Getter
    private final String submissionId;

    private SubmissionFileKey(String userId, String problemId, String submissionId, String fileName) {
        super(fileName);
        this.userId = userId;
        this.problemId = problemId;
        this.submissionId = submissionId;
    }

    public static SubmissionFileKey of(String userId, String problemId, String submissionId, String fileName) {
        return new SubmissionFileKey(userId, problemId, submissionId, fileName);
    }

    private static String constructKey(String userId, String problemId, String submissionId, String fileName) {
        return String.format("users/%s/submissions/%s/%s/%s", userId, problemId, submissionId, fileName);
    }

    public static String prefix(String userId, String problemId, String submissionId) {
        return constructKey(userId, problemId, submissionId, "");
    }

    @Override
    public String toString() {
        return constructKey(userId, problemId, submissionId, fileName);
    }
}
