package io.github.sanyavertolet.edukate.gateway.controllers;

import io.github.sanyavertolet.edukate.auth.services.AuthCookieService;
import io.github.sanyavertolet.edukate.gateway.dtos.SignInRequest;
import io.github.sanyavertolet.edukate.gateway.dtos.SignUpRequest;
import io.github.sanyavertolet.edukate.gateway.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user authentication operations")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieService authCookieService;

    @PostMapping("/sign-in")
    @Operation(
            operationId = "sign-in",
            summary = "Sign user in",
            description = "Authenticates a user with provided credentials and returns a JWT token in a cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully authenticated", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authentication failed", content = @Content)
    })
    public Mono<ResponseEntity<Void>> signIn(@RequestBody SignInRequest signInRequest) {
        return authService.signIn(signInRequest).flatMap(authCookieService::respondWithToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
    }

    @PostMapping("/sign-up")
    @Operation(
            operationId = "sign-up",
            summary = "Register new user",
            description = "Registers a new user with provided information and returns a JWT token in a cookie"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully registered", content = @Content),
            @ApiResponse(responseCode = "403", description = "Registration failed", content = @Content),
            @ApiResponse(responseCode = "409", description = "User with this name already exists", content = @Content),
    })
    public Mono<ResponseEntity<Void>> signUp(@RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest).flatMap(authCookieService::respondWithToken)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
    }

    @PostMapping("/sign-out")
    @Operation(
            operationId = "sign-out",
            summary = "Sign user out",
            description = "Signs out the current user by expiring their authentication token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully signed out", content = @Content)
    })
    public Mono<ResponseEntity<Void>> signOut() {
        return authCookieService.respondWithExpiredToken();
    }
}
