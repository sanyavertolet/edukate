package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@Document(value = "problems")
public class Problem {
    @Id
    private String id;
    private String text;
    private List<String> images;
    private String result;
    private List<String> resultImages;

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
        return new ProblemMetadata(id);
    }

    public ProblemDto toProblemDto() {
        return new ProblemDto(id, text, images);
    }
}
