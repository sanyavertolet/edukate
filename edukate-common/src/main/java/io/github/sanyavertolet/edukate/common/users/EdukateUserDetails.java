package io.github.sanyavertolet.edukate.common.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@AllArgsConstructor
public class EdukateUserDetails implements UserDetails, CredentialsContainer {
    @Getter
    private final String id;
    private final String name;
    @Getter
    private final Set<UserRole> roles;
    @Getter
    private final UserStatus status;

    @ToString.Exclude
    private String token;

    public EdukateUserDetails(UserCredentials userCredentials) {
        this(
                Objects.requireNonNull(userCredentials.getId(), "User id must not be null"),
                Objects.requireNonNull(userCredentials.getUsername(), "User name must not be null"),
                Objects.requireNonNull(userCredentials.getRoles(), "User role must not be null"),
                Objects.requireNonNull(userCredentials.getStatus(), "User status must not be null"),
                userCredentials.getEncodedPassword()
        );
    }

    public PreAuthenticatedAuthenticationToken toPreAuthenticatedAuthenticationToken() {
        return new PreAuthenticatedAuthenticationToken(this, null, getAuthorities());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(UserRole::asGrantedAuthority).collect(Collectors.toSet());
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
    public void eraseCredentials() {
        token = null;
    }
}
