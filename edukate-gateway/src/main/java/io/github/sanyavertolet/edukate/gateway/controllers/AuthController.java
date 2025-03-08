package io.github.sanyavertolet.edukate.gateway.controllers;

import io.github.sanyavertolet.edukate.gateway.dtos.AuthenticationDetails;
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest;
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest;
import io.github.sanyavertolet.edukate.gateway.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @RequestMapping("/sign-in")
    public Mono<ResponseEntity<AuthenticationDetails>> signIn(@RequestBody SignInRequest signInRequest) {
        return authService.signIn(signInRequest).map(ResponseEntity::ok);
    }

    @RequestMapping("/sign-up")
    public Mono<ResponseEntity<AuthenticationDetails>> signUp(@RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest).map(ResponseEntity::ok);
    }
}
