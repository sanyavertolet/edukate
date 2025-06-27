package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.CreateBundleRequest;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.services.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bundles")
public class BundleController {
    private final BundleService bundleService;

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

    @PostMapping("/join/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleMetadata> joinBundle(@PathVariable String shareCode, Authentication authentication) {
        return bundleService.joinUser(authentication.getName(), shareCode).map(Bundle::toBundleMetadata);
    }

    @PostMapping("/leave/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<String> leaveBundle(@PathVariable String shareCode, Authentication authentication) {
        return bundleService.removeUser(authentication.getName(), shareCode).map(Bundle::getShareCode);
    }

    @PostMapping("/invite/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<String> inviteToBundle(
            @PathVariable String shareCode, @RequestParam String inviteeId, Authentication authentication
    ) {
        return bundleService.inviteUser(authentication.getName(), inviteeId, shareCode)
                .thenReturn("User " + inviteeId + " has been invited to bundle " + shareCode);
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

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> createBundle(@RequestBody CreateBundleRequest request, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .flatMap(auth -> bundleService.createBundle(request, auth))
                .flatMap(bundle -> bundleService.prepareDto(bundle, authentication));
    }
}
