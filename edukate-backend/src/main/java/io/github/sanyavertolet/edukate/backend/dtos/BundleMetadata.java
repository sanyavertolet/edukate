package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BundleMetadata {
    private String name;
    private String description;
    private String authorName;
    private Boolean isPublic;
    private Long size;
}
