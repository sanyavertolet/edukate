package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Hidden
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {
    private final UserService userService;

    @PostMapping
    public Mono<User> saveUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping("/by-name/{name}")
    public Mono<User> getUserByName(@PathVariable String name) {
        return userService.findUserByName(name);
    }

    @GetMapping("/by-id/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return userService.findUserById(id);
    }

    @DeleteMapping("/by-id/{id}")
    public Mono<Boolean> deleteUserById(@PathVariable String id) {
        return userService.deleteUserById(id);
    }
}
