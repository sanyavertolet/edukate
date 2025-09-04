package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ProblemRepository extends ReactiveMongoRepository<Problem, String> {
    @Query("{}")
    Flux<Problem> findAll(Pageable pageable);

    Flux<Problem> findProblemsByIdIn(Collection<String> ids);

    Flux<Problem> findProblemsByIdStartingWith(String prefix, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $lookup: { from: 'problem_status', let: { pid: '$_id' }, pipeline: [" +
                    "{ $match: { $expr: { $and: [ { $eq: ['$problemId', '$$pid'] }, " +
                    "{ $eq: ['$userId', ?0] } ] } } } ], as: 'ps' } }",
            "{ $match: { ps: { $size: 0 } } }",
            "{ $sample: { size: 1 } }",
            "{ $project: { _id: 0, id: '$_id' } }"
    })
    Mono<String> findRandomUnsolvedProblemId(String userId);

    @Aggregation(pipeline = {
            "{ $sample: { size: 1 } }",
            "{ $project: { _id: 0, id: '$_id' } }"
    })
    Mono<String> findRandomProblemId();
}
