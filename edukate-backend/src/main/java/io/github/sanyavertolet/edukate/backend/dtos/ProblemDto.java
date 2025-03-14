package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@RequiredArgsConstructor
public class ProblemDto {
    private String id;
    private Boolean isHard;
    private List<String> tags;
    private String text;
    private List<Problem.Subtask> subtasks = new ArrayList<>();
    private List<String> images;
    private Boolean hasResult;
}
