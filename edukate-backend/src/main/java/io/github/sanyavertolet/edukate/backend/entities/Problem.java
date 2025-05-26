package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@RequiredArgsConstructor
@Document(value = "problems")
public class Problem {
    @Id
    private String id;
    private Boolean isHard;
    private List<String> tags;

    private String text;
    private List<Subtask> subtasks;
    private List<String> images;

    @With
    private Result result;

    @Data
    @AllArgsConstructor
    public static class Subtask {
        private String id;
        private String text;
    }

    public enum Status {
        SOLVED, SOLVING, FAILED, NOT_SOLVED
    }

    public void addImageIfNotPresent(String imageName) {
        if (images == null) {
            images = new ArrayList<>();
            images.add(imageName);
            return;
        }
        boolean contains = images.contains(imageName);
        if (!contains) {
            images.add(imageName);
        }
    }

    public ProblemMetadata toProblemMetadata() {
        return new ProblemMetadata(id, isHard, tags, null);
    }

    public ProblemDto toProblemDto() {
        return new ProblemDto(id, isHard, tags, text, subtasks, images, null, result != null);
    }
}
