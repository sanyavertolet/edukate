package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.utils.StatusCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface SubmissionRepository extends ReactiveMongoRepository<Submission, String> {
    Flux<Submission> findAllByProblemIdAndUserId(String problemId, String userId, Pageable pageable);

    Flux<Submission> findAllByStatusIn(Collection<Submission.Status> statuses, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $match: { 'problemId': ?0, 'userId': ?1 } }",
            "{ $group: { _id: '$status', count: { $sum: 1 } } }",
            "{ $project: { _id: 0, status: '$_id', count: 1 } }"
    })
    Flux<StatusCount> countAllForUserByStatus(String problemId, String userId);
}
