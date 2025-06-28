package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.backend.dtos.UserNameWithRole;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.services.BundleService;
import io.github.sanyavertolet.edukate.common.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bundles")
public class BundleController {
    private final BundleService bundleService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> createBundle(@RequestBody CreateBundleRequest request, Authentication authentication) {
        return bundleService.createBundle(request, authentication)
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @GetMapping("/owned")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Flux<BundleMetadata> getOwnedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return bundleService.getOwnedBundles(authentication, PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/joined")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Flux<BundleMetadata> getJoinedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return bundleService.getJoinedBundles(authentication, PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/public")
    public Flux<BundleMetadata> getPublicBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bundleService.getPublicBundles(PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> getBundleByShareCode(
            @PathVariable String shareCode,
            Authentication authentication) {
        return bundleService.findBundleByShareCode(shareCode)
                .filter(bundle -> bundle.isUserInBundle(authentication.getName()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permission")))
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }

    @PostMapping("/{shareCode}/join")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleMetadata> joinBundle(@PathVariable String shareCode, Authentication authentication) {
        return bundleService.joinUser(authentication.getName(), shareCode).map(Bundle::toBundleMetadata);
    }

    @PostMapping("/{shareCode}/leave")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<String> leaveBundle(@PathVariable String shareCode, Authentication authentication) {
        return bundleService.removeUser(authentication.getName(), shareCode).map(Bundle::getShareCode);
    }

    @PostMapping("/{shareCode}/invite")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<String> inviteToBundle(
            @PathVariable String shareCode, @RequestParam String inviteeId, Authentication authentication
    ) {
        return bundleService.inviteUser(authentication.getName(), inviteeId, shareCode)
                .thenReturn("User " + inviteeId + " has been invited to bundle " + shareCode);
    }

    @GetMapping("/{shareCode}/users")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<List<UserNameWithRole>> getUsersInBundle(@PathVariable String shareCode, Authentication authentication) {
        return bundleService.getBundleUsers(shareCode, authentication).flatMap(bundleService::mapToList);
    }

    @PostMapping("/{shareCode}/change-role")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<Role> changeUserRole(
            @PathVariable String shareCode,
            @RequestParam String username,
            @RequestParam Role requestedRole,
            Authentication authentication
    ) {
        return bundleService.changeUserRole(shareCode, username, requestedRole, authentication);
    }
}
