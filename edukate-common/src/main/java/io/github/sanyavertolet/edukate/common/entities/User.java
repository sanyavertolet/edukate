package io.github.sanyavertolet.edukate.common.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
@Document(collection = "users")
public class User {
    @Id
    private final String id;
    @Indexed(unique = true)
    private final String name;
    private final String role;
    private final String status;
    private final String token;
}
