package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class CreateBundleRequest {
    private String name;
    private String description;
    private Boolean isPublic;
    private List<String> problemIds;
}
