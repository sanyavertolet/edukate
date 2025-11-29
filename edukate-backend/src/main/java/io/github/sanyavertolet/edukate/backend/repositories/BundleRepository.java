package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Bundle;
import io.github.sanyavertolet.edukate.common.users.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface BundleRepository extends ReactiveMongoRepository<Bundle, String> {
    Flux<Bundle> findBundlesByIsPublic(boolean isPublic, Pageable pageable);

    Mono<Bundle> findBundleByShareCode(String shareCode);

    @Query("{ 'userIdRoleMap.?0': { $in: ?1 } }")
    Flux<Bundle> findBundlesByUserRoleIn(String userId, Collection<UserRole> roles, Pageable pageable);
}
