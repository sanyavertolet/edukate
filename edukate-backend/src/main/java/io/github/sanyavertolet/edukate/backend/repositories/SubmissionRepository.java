package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface SubmissionRepository extends ReactiveMongoRepository<Submission, String> {
    Flux<Submission> findAllByProblemIdAndUserId(String problemId, String userId, Pageable pageable);

    Flux<Submission> findAllByStatusIn(Collection<Submission.Status> statuses, Pageable pageable);

    Flux<Submission> findAllByUserId(String userId, Pageable pageable);
}
