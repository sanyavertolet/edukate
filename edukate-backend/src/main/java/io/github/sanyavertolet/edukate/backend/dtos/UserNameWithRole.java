package io.github.sanyavertolet.edukate.backend.dtos;

import io.github.sanyavertolet.edukate.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserNameWithRole {
    private String name;
    private Role role;
}
