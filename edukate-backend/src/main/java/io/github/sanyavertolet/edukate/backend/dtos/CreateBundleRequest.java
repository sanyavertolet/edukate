package io.github.sanyavertolet.edukate.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateBundleRequest {
    @NotBlank
    @Size(max = 50)
    private String name;
    @NotBlank
    @Size(max = 255)
    private String description;
    @NotNull
    private Boolean isPublic;
    @NotEmpty
    private List<@NotBlank String> problemIds;
}
