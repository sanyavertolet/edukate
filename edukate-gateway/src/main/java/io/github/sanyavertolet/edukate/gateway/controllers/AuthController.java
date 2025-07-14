package io.github.sanyavertolet.edukate.gateway.controllers;

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService;
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest;
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest;
import io.github.sanyavertolet.edukate.gateway.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Import(AuthCookieService.class)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/sign-in")
    public Mono<ResponseEntity<Void>> signIn(@RequestBody SignInRequest signInRequest) {
        return authService.signIn(signInRequest).flatMap(authCookieService::respondWithToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
    }

    @PostMapping("/sign-up")
    public Mono<ResponseEntity<Void>> signUp(@RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest).flatMap(authCookieService::respondWithToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
    }

    @PostMapping("/sign-out")
    public Mono<ResponseEntity<Void>> signUp() {
        return authCookieService.respondWithExpiredToken();
    }
}
