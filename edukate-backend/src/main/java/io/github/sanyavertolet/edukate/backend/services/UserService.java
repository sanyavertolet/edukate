package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.repositories.UserRepository;
import io.github.sanyavertolet.edukate.common.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> getUserById(String name) {
        return userRepository.findById(name);
    }

    public Mono<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public Mono<Boolean> deleteUserById(String id) {
        return userRepository.deleteById(id).thenReturn(true).onErrorReturn(false);
    }
}
