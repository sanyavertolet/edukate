package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.backend.entities.UserProblemStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserProblemStatusRepository extends ReactiveReadOnlyRepository<UserProblemStatus, String> {
    Mono<UserProblemStatus> findByUserIdAndProblemId(String userId, String problemId);
}
