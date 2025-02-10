package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class ProblemDto {
    private String id;
    private String text;
    private List<String> images;
}
