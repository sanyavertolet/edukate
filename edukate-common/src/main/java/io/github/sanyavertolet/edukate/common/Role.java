package io.github.sanyavertolet.edukate.common;

public enum Role {
    USER,
    MODERATOR,
    ADMIN,
    ;

    public String asSpringSecurityRole() {
        return "ROLE_" + name();
    }
}
