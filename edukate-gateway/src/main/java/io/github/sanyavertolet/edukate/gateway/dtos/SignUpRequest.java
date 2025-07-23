package io.github.sanyavertolet.edukate.gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    @ToString.Exclude
    private String password;
    private String email;
}
