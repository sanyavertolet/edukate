package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository;
import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CheckResultService {
    private final CheckResultRepository checkResultRepository;

    public Mono<CheckResult> save(CheckResult checkResult) {
        return checkResultRepository.save(checkResult);
    }

    public Mono<CheckResult> saveSelfCheckResult(String submissionId) {
        return Mono.just(CheckResult.self().submissionId(submissionId).build())
                .flatMap(checkResultRepository::save);
    }
}
