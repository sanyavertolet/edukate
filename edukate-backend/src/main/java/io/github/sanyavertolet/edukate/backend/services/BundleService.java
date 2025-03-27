package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.BundleDto;
import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.backend.repositories.BundleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BundleService {
    private final BundleRepository bundleRepository;

    public Mono<Bundle> findBundleByOwnerIdAndName(String ownerId, String bundleName) {
        return bundleRepository.findBundleByOwnerIdAndName(ownerId, bundleName);
    }

    public Flux<Bundle> getOwnedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication)
                .flatMapMany(auth -> bundleRepository.findBundlesByOwnerId(auth.getName(), pageable));
    }

    public Flux<Bundle> getJoinedBundles(Authentication authentication, PageRequest pageable) {
        return Mono.justOrEmpty(authentication)
                .flatMapMany(auth ->
                        bundleRepository.findBundlesByUserIdsContains(auth.getName(), pageable)
                );
    }

    public Flux<Bundle> getPublicBundles(PageRequest pageable) {
        return bundleRepository.findBundlesByIsPublic(true, pageable);
    }

    public Mono<Bundle> createBundle(BundleDto bundleDto) {
        return Mono.just(bundleDto)
                .map(Bundle::fromDto)
                // todo: generate shareCode here
                .flatMap(bundleRepository::save);
    }

    public Mono<Boolean> existsBundle(String ownerId, String bundleName) {
        return bundleRepository.existsByOwnerIdAndName(ownerId, bundleName);
    }
}
