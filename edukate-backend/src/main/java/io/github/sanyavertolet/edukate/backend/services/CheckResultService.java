package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.repositories.CheckResultRepository;
import io.github.sanyavertolet.edukate.backend.entities.CheckResult;
import io.github.sanyavertolet.edukate.common.SubmissionStatus;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@AllArgsConstructor
public class CheckResultService {
    private final CheckResultRepository checkResultRepository;
    private final SubmissionService submissionService;

    @Transactional
    public Mono<Tuple2<CheckResult, Submission>> saveAndUpdateSubmission(CheckResult checkResult) {
        return submissionService.findById(checkResult.getSubmissionId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .flatMap(submission -> checkResultRepository.save(checkResult)
                        .map(saved -> Tuples.of(saved, submission)))
                .flatMap(tuple -> {
                    CheckResult saved = tuple.getT1();
                    Submission submission = tuple.getT2();
                    SubmissionStatus newStatus = SubmissionStatus.from(saved.getStatus());
                    submission.setStatus(SubmissionStatus.best(submission.getStatus(), newStatus));
                    return submissionService.update(submission).thenReturn(tuple);
                });
    }

    public Mono<CheckResult> findById(String id) {
        return checkResultRepository.findById(id);
    }

    public Flux<CheckResult> findAllBySubmissionId(String submissionId) {
        return checkResultRepository.findBySubmissionId(
                submissionId, Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}
