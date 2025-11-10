package io.github.sanyavertolet.edukate.backend.repositories;

import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckResultRepository extends ReactiveMongoRepository<CheckResult, String> {

}
