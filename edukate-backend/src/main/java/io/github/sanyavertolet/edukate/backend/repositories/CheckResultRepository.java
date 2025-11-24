package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.CheckResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CheckResultRepository extends ReactiveMongoRepository<CheckResult, String> {
    Flux<CheckResult> findBySubmissionId(String submissionId);
}
