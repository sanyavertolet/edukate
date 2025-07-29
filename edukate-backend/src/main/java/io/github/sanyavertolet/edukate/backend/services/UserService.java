package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.repositories.UserRepository;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Mono<User> findUserByAuthentication(Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMap(this::findUserById);
    }

    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> findUserById(String name) {
        return userRepository.findById(name);
    }

    public Mono<User> findUserByName(String name) {
        return userRepository.findByName(name);
    }

    public Mono<Boolean> deleteUserById(String id) {
        return userRepository.deleteById(id).thenReturn(true).onErrorReturn(false);
    }

    public Mono<Boolean> doesUserExist(String username) {
        return userRepository.existsByName(username);
    }

    public Mono<Boolean> hasUserPermissionToSubmit(User user) {
        return Mono.just(user).filter(usr -> usr.getStatus().equals(UserStatus.ACTIVE)).hasElement();
    }
}
