package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.users.UserStatus;
import io.github.sanyavertolet.edukate.backend.entities.User;
import io.github.sanyavertolet.edukate.common.users.UserCredentials;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {
    private final UserService userService;

    @PostMapping
    public Mono<UserCredentials> saveUser(@RequestBody UserCredentials userCredentials) {
        return userService.saveUser(User.newFromCredentials(userCredentials)).map(User::toCredentials);
    }

    @GetMapping("/by-name/{name}")
    public Mono<UserCredentials> getUserByName(@PathVariable String name) {
        return userService.findUserByName(name).map(User::toCredentials);
    }

    @GetMapping("/by-id/{id}")
    public Mono<UserCredentials> getUserById(@PathVariable String id) {
        return userService.findUserById(id).map(User::toCredentials);
    }

    @DeleteMapping("/by-id/{id}")
    public Mono<String> deleteUserById(@PathVariable String id) {
        return userService.deleteUserById(id).thenReturn(id);
    }

    @PostMapping("/notify-all")
    public Mono<Long> notifyAllUsers(
            @RequestBody Map<String, String> body,
            @RequestParam(required = false, defaultValue = "ACTIVE") UserStatus status
    ) {
        return userService.notifyAllUsersWithStatus(body.get("title"), body.get("message"), status);
    }
}
