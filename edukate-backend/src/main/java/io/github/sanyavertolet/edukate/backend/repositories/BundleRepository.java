package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BundleRepository extends ReactiveMongoRepository<Bundle, String> {
    Flux<Bundle> findBundlesByOwnerId(String ownerId, Pageable pageable);

    Flux<Bundle> findBundlesByUserIdsContains(String userId, Pageable pageable);

    Flux<Bundle> findBundlesByIsPublic(boolean isPublic, Pageable pageable);

    Mono<Boolean> existsByOwnerIdAndName(String ownerId, String name);

    Mono<Bundle> findBundleByShareCode(String shareCode);
}
