package io.github.sanyavertolet.edukate.gateway.services;

import io.github.sanyavertolet.edukate.auth.services.JwtTokenService;
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest;
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public Mono<String> signIn(SignInRequest signInRequest) {
        return userDetailsService.findEdukateUserDetailsByUsername(signInRequest.getUsername())
                .filter(userDetails -> passwordEncoder.matches(signInRequest.getPassword(), userDetails.getPassword()))
                .map(jwtTokenService::generateToken);
    }

    public Mono<String> signUp(SignUpRequest signUpRequest) {
        return Mono.just(signUpRequest).filterWhen(this::isNotUserPresent)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.CONFLICT)))
                .flatMap(request ->
                        userDetailsService.create(request.getUsername(), passwordEncoder.encode(request.getPassword()))
                )
                .map(jwtTokenService::generateToken);
    }

    private Mono<Boolean> isNotUserPresent(SignUpRequest signUpRequest) {
        return userDetailsService.findByUsername(signUpRequest.getUsername()).hasElement().map(it -> !it);
    }
}
