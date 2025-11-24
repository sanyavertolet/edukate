package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.common.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class SubmissionDto {
    private String id;
    private String problemId;
    @With
    private String userName;
    private SubmissionStatus status;
    private Instant createdAt;
    @With
    private List<String> fileUrls;
}
