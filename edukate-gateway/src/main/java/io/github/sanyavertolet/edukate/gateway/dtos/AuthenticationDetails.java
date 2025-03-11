package io.github.sanyavertolet.edukate.gateway.dtos;

import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthenticationDetails {
    private String username;
    private String token;
    private List<String> roles;
    private String status;

    public AuthenticationDetails(EdukateUserDetails userDetails, String jwtToken) {
        this(
                userDetails.getUsername(),
                jwtToken,
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                userDetails.getStatus().name()
        );
    }
}
