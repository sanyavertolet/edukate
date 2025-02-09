package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class ProblemMetadata {
    private String name;
}
