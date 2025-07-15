package io.github.sanyavertolet.edukate.auth;

import io.github.sanyavertolet.edukate.common.Role;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.github.sanyavertolet.edukate.common.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.sanyavertolet.edukate.auth.utils.AuthHeaders.*;

@AllArgsConstructor
public class EdukateUserDetails implements UserDetails, CredentialsContainer {
    @Getter
    private final String id;
    private final String name;
    @Getter
    private final Set<Role> roles;
    @Getter
    private final UserStatus status;
    private String token;

    public EdukateUserDetails(User user) {
        this(
                Objects.requireNonNull(user.getId(), "User id must not be null"),
                Objects.requireNonNull(user.getName(), "User name must not be null"),
                Objects.requireNonNull(user.getRoles(), "User role must not be null"),
                Objects.requireNonNull(user.getStatus(), "User status must not be null"),
                user.getToken()
        );
    }

    public PreAuthenticatedAuthenticationToken toPreAuthenticatedAuthenticationToken() {
        return new PreAuthenticatedAuthenticationToken(this, null, getAuthorities());
    }

    public void populateHeaders(HttpHeaders httpHeaders) {
        Objects.requireNonNull(httpHeaders, "HttpHeaders must not be null");
        httpHeaders.set(AUTHORIZATION_ID.headerName, id);
        httpHeaders.set(AUTHORIZATION_NAME.headerName, name);
        httpHeaders.set(AUTHORIZATION_STATUS.headerName, status.name());
        String rolesString = Role.toString(roles);
        httpHeaders.set(AUTHORIZATION_ROLES.headerName, rolesString);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(Role::asGrantedAuthority).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public String getPassword() {
        return token;
    }

    @Override
    public String toString() {
        return "EdukateUserDetails{" +
                "name='" + name + '\'' +
                ", roles='" + roles + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public void eraseCredentials() {
        token = null;
    }
}
