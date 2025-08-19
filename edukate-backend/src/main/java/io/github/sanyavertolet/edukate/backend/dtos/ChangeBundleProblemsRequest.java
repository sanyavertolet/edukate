package io.github.sanyavertolet.edukate.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChangeBundleProblemsRequest {
    @NotEmpty
    private List<@NotBlank String> problemIds;
}
