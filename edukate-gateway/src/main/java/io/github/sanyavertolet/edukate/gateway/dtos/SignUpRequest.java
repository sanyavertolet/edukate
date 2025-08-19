package io.github.sanyavertolet.edukate.gateway.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class SignUpRequest {
    @NotBlank
    private String username;

    @ToString.Exclude
    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;
}
