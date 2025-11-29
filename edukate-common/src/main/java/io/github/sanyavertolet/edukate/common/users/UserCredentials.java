package io.github.sanyavertolet.edukate.common.users;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserCredentials {
    private final String id;
    private final String username;
    private final String encodedPassword;
    private final String email;
    private final Set<UserRole> roles;
    private final UserStatus status;

    public static UserCredentials newUser(String username, String encodedPassword, String email) {
        return UserCredentials.builder()
                .id(null)
                .username(username)
                .encodedPassword(encodedPassword)
                .email(email)
                .roles(Set.of(UserRole.USER))
                .status(UserStatus.PENDING)
                .build();
    }
}
