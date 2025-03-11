package io.github.sanyavertolet.edukate.gateway.services;

import io.github.sanyavertolet.edukate.auth.EdukateUserDetails;
import io.github.sanyavertolet.edukate.auth.utils.RoleUtils;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.github.sanyavertolet.edukate.common.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements ReactiveUserDetailsService {
    private final BackendService backendService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return findEdukateUserDetailsByUsername(username).cast(UserDetails.class);
    }

    public Mono<EdukateUserDetails> findEdukateUserDetailsByUsername(String username) {
        return backendService.getUserByName(username).map(EdukateUserDetails::new);
    }

    public Mono<EdukateUserDetails> create(String username, String encodedPassword) {
        User user = new User(username, encodedPassword, RoleUtils.getDefaultRole(), UserStatus.PENDING);
        return backendService.saveUser(user).map(EdukateUserDetails::new)
                .doOnNext(EdukateUserDetails::eraseCredentials);
    }
}
