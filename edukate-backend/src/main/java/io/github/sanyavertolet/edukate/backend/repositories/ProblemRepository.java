package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface ProblemRepository extends ReactiveMongoRepository<Problem, String> {
    @Query("{}")
    Flux<Problem> findAll(Pageable pageable);

    Flux<Problem> findProblemsByIdIn(Collection<String> ids);

    Flux<Problem> findProblemsByIdStartingWith(String prefix, Pageable pageable);

    @Query("{ $or: [" +
            "{ majorId: { $exists: false } }," +
            "{ minorId: { $exists: false } }," +
            "{ patchId: { $exists: false } }] }")
    Flux<Problem> findProblemsWithMissingIndices();
}
