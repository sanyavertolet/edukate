package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class ProblemMetadata {
    private String name;
    private Boolean isHard;
    private List<String> tags;
    private Problem.Status status;
}
