package io.github.sanyavertolet.edukate.gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    private String password;
}
