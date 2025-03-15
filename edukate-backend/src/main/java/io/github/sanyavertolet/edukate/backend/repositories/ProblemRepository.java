package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface ProblemRepository extends ReactiveMongoRepository<Problem, String> {
    Flux<Problem> findAllByIsHardIn(Collection<Boolean> isHard, Pageable pageable);
}
