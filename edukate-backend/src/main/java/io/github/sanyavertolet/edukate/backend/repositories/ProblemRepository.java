package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends ReactiveMongoRepository<Problem, String> {
}