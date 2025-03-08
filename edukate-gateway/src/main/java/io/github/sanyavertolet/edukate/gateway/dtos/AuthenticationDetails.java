package io.github.sanyavertolet.edukate.gateway.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationDetails {
    private String username;
    private String token;
}
