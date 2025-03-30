package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BundleMetadata {
    private String name;
    private String description;
    private List<String> admins;
    private String shareCode;
    private Boolean isPublic;
    private Long size;
}
