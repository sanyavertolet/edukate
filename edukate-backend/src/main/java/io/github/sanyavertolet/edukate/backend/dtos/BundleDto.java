package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

import java.util.List;

@Data
@AllArgsConstructor
public class BundleDto {
    private String name;
    private String description;
    @With
    private List<String> admins;
    private Boolean isPublic;
    @With
    private List<ProblemMetadata> problems;
    private String shareCode;
}
