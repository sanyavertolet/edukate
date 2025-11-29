package io.github.sanyavertolet.edukate.backend.entities;

import io.github.sanyavertolet.edukate.common.users.UserRole;
import io.github.sanyavertolet.edukate.common.users.UserStatus;
import io.github.sanyavertolet.edukate.common.users.UserCredentials;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@Document(collection = "users")
public class User {
    @Id
    private String id;

    // todo: put NonNull annotation back
    @Indexed(unique = true)
    private final String name;

    private final String email;

    @NonNull
    @ToString.Exclude
    private final String token;

    private final Set<UserRole> roles;

    private final UserStatus status;

    public static User newFromCredentials(UserCredentials credentials) {
        return new User(
                null,
                credentials.getUsername(),
                credentials.getEmail(),
                credentials.getEncodedPassword(),
                Set.of(UserRole.USER),
                UserStatus.PENDING
        );
    }

    public UserCredentials toCredentials() {
        return UserCredentials.builder()
                .username(name)
                .email(email)
                .encodedPassword(token)
                .build();
    }
}
