package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.UserDto;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/whoami")
    public Mono<UserDto> whoami(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .map(Principal::getName)
                .flatMap(userService::getUserByName)
                .map(UserDto::of)
                .switchIfEmpty(Mono.empty());
    }
}
