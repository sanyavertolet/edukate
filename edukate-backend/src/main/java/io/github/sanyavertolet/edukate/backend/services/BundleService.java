package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository;
import io.github.sanyavertolet.edukate.common.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BundleService {
    private final BundleRepository bundleRepository;
    private final ShareCodeGenerator shareCodeGenerator;

    public Mono<Bundle> findBundleByShareCode(String shareCode) {
        return bundleRepository.findBundleByShareCode(shareCode);
    }

    public Mono<Bundle> joinUser(String userId, String shareCode) {
        return bundleRepository.findBundleByShareCode(shareCode)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle [" + shareCode + "] not found")))
                .filter(bundle -> !bundle.isUserInBundle(userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already joined the bundle")))
                .map(bundle -> {
                    bundle.addUser(userId, Role.USER);
                    return bundle;
                })
                .flatMap(bundleRepository::save);
    }

    public Flux<Bundle> getOwnedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication).flatMapMany(auth ->
                bundleRepository.findBundlesByUserRoleIn(auth.getName(), List.of(Role.ADMIN), pageable)
        );
    }

    public Flux<Bundle> getJoinedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication).flatMapMany(auth ->
                bundleRepository.findBundlesByUserRoleIn(auth.getName(), Role.anyRole(), pageable)
        );
    }

    public Flux<Bundle> getPublicBundles(PageRequest pageable) {
        return bundleRepository.findBundlesByIsPublic(true, pageable);
    }

    public Mono<Bundle> createBundle(BundleDto bundleDto) {
        return Mono.just(bundleDto)
                .map(Bundle::fromDto)
                .map(bundle -> bundle.updateShareCode(shareCodeGenerator.generateShareCode()))
                .flatMap(bundleRepository::save);
    }
}
