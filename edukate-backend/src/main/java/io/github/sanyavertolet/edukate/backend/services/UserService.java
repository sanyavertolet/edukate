package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.repositories.UserRepository;
import io.github.sanyavertolet.edukate.common.UserStatus;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest;
import io.github.sanyavertolet.edukate.common.services.Notifier;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final Notifier notifier;

    public Mono<User> findUserByAuthentication(Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMap(this::findUserById);
    }

    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> findUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Flux<User> findUsersByIds(Collection<String> userIds) {
        return userRepository.findAllById(userIds);
    }

    public Mono<User> findUserByName(String name) {
        return userRepository.findByName(name);
    }

    public Mono<Void> deleteUserById(String id) {
        return userRepository.deleteById(id);
    }

    public Mono<Boolean> hasUserPermissionToSubmit(User user) {
        return Mono.just(UserStatus.ACTIVE.equals(user.getStatus()));
    }

    public Mono<Long> notifyAllUsersWithStatus(String title, String message, UserStatus status) {
        return userRepository.findAllByStatus(status)
                .map(user -> new SimpleNotificationCreateRequest(
                        UUID.randomUUID().toString(),
                        user.getId(),
                        title != null ? title : "edukate-talks",
                        message,
                        "edukate team"
                ))
                .flatMap(req -> notifier.notify(req)
                        .onErrorResume(ex -> {
                            log.warn("Could not notify user {}", req.getTargetUserId(), ex);
                            return Mono.empty();
                        })
                )
                .count();
    }
}
