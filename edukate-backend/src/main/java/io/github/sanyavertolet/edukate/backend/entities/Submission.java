package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@Document(value = "submissions")
public class Submission {
    @Id
    private String id;
    private String problemId;
    private String userId;
    private Status status;
    private List<String> fileKeys;
    private Instant createdAt;

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }

    public SubmissionDto toDto() {
        return new SubmissionDto(id, problemId, userId, status, createdAt, fileKeys);
    }

    public static Submission of(String problemId, String userId, List<String> fileKeys) {
        return new Submission(null, problemId, userId, Status.PENDING, fileKeys, Instant.now(Clock.systemUTC()));
    }

    public Submission markAs(Status status) {
        this.status = status;
        return this;
    }
}
