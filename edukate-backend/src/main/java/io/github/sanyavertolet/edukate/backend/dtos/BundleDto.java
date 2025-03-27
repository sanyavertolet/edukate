package io.github.sanyavertolet.edukate.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BundleDto {
    private String name;
    private String description;
    private String ownerName;
    private Boolean isPublic;
    private List<String> problemIds;
    private String shareCode;

    public BundleDto withOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public BundleDto withShareCode(String shareCode) {
        this.shareCode = shareCode;
        return this;
    }
}
