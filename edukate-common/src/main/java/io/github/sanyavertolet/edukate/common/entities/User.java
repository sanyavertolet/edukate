package io.github.sanyavertolet.edukate.common.entities;

import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
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
    @NonNull
    @Indexed(unique = true)
    private final String name;

    @NonNull
    private final String token;

    private final Set<Role> roles;

    private final UserStatus status;
}
