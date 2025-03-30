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
    private List<String> problemIds;
    @With
    private String shareCode;

    public BundleDto withAdmin(String adminName) {
        return withAdmins(List.of(adminName));
    }
}
