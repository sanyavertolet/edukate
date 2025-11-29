package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.common.users.UserStatus;
import io.github.sanyavertolet.edukate.backend.entities.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByName(String name);

    Flux<User> findAllByStatus(UserStatus status);
}
