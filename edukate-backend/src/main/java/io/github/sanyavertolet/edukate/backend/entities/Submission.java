package io.github.sanyavertolet.edukate.backend.entities;

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
    private List<String> fileObjectIds;
    private Instant createdAt;

    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }

    public static Submission of(String problemId, String userId) {
        return new Submission(null, problemId, userId, Status.PENDING, List.of(), Instant.now(Clock.systemUTC()));
    }

    public Submission markAs(Status status) {
        this.status = status;
        return this;
    }
}
