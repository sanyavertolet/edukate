package io.github.sanyavertolet.edukate.common.entities;

import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
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

    private final Set<Role> roles;

    private final UserStatus status;
}
