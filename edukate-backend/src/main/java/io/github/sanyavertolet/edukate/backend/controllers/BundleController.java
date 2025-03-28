package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.dtos.BundleMetadata;
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
        return Mono.justOrEmpty(authentication)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMapMany(auth -> bundleService.getOwnedBundles(auth, PageRequest.of(page, size)))
                .map(Bundle::toBundleMetadata);
    }

    @GetMapping("/public")
    public Flux<BundleMetadata> getPublicBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bundleService.getPublicBundles(PageRequest.of(page, size)).map(Bundle::toBundleMetadata);
    }

    @GetMapping("/joined")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Flux<BundleMetadata> getJoinedBundles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return Mono.justOrEmpty(authentication)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMapMany(auth -> bundleService.getJoinedBundles(auth, PageRequest.of(page, size)))
                .map(Bundle::toBundleMetadata);
    }

    @GetMapping("/join/{shareCode}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleMetadata> joinBundle(@PathVariable String shareCode) {
        // todo: finish this
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet"));
    }

    // todo: should be fixed as bundleName is something human-readable, while shareCode might be more suitable here
    @GetMapping("/{ownerName}/{bundleName}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> getBundleByOwnerAndName(
            @PathVariable String ownerName,
            @PathVariable String bundleName,
            Authentication authentication) {
        return bundleService.findBundleByOwnerIdAndName(ownerName, bundleName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bundle " + bundleName + " not found"
                )))
                .filterWhen(bundle -> Mono.fromCallable(() ->
                        bundle.isUserInBundle(authentication.getName()) || bundle.isOwner(authentication.getName())
                ))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permission")))
                .map(Bundle::toDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public Mono<BundleDto> createBundle(@RequestBody BundleDto bundleDto, Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .map(auth -> bundleDto.withOwnerName(auth.getName()))
                .filterWhen(dto -> bundleService.existsBundle(dto.getOwnerName(), dto.getName())
                        .map(Boolean.FALSE::equals))
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bundle with name " + bundleDto.getName() + " already exists"
                )))
                .flatMap(bundleService::createBundle)
                .map(Bundle::toDto);
    }
}
