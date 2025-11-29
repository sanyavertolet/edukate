package io.github.sanyavertolet.edukate.gateway.services;

import io.github.sanyavertolet.edukate.common.users.EdukateUserDetails;
import io.github.sanyavertolet.edukate.common.users.UserCredentials;
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

    public Mono<EdukateUserDetails> findById(String id) {
        return backendService.getUserById(id).map(EdukateUserDetails::new);
    }

    public Mono<Boolean> isNotUserPresent(String username) {
        return findByUsername(username).hasElement().map(it -> !it);
    }

    public Mono<EdukateUserDetails> create(String username, String email, String encodedPassword) {
        UserCredentials userCredentials = UserCredentials.newUser(username, encodedPassword, email);
        return backendService.saveUser(userCredentials).map(EdukateUserDetails::new)
                .doOnNext(EdukateUserDetails::eraseCredentials);
    }
}
