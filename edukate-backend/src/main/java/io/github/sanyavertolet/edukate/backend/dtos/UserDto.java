package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.common.users.UserRole;
import io.github.sanyavertolet.edukate.backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;

import java.util.List;

@Data
@AllArgsConstructor(onConstructor = @__(@PersistenceCreator))
public class UserDto {
    private String name;
    private List<String> roles;
    private String status;

    public static UserDto of(User user) {
        List<String> userRoles = user.getRoles().stream().map(UserRole::name).toList();
        String userStatus = user.getStatus().name();
        return new UserDto(user.getName(), userRoles, userStatus);
    }
}
