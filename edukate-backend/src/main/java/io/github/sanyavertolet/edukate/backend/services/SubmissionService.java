package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository;
import io.github.sanyavertolet.edukate.backend.utils.StatusCount;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    public Mono<Submission> saveSubmission(String problemId, String userId, String fileKey) {
        return Mono.just(Submission.of(problemId, userId, fileKey))
                // TODO: remove me when file submission mechanism is implemented
                .map(submission -> submission.markAs(Submission.Status.SUCCESS))
                .flatMap(submissionRepository::save);
    }

    public Mono<Submission> findSubmissionById(String id) {
        return submissionRepository.findById(id);
    }

    public Flux<Submission> findSubmissionsByUsernameAndProblemId(String username, String problemId, Pageable pageable) {
        return submissionRepository.findAllByProblemIdAndUserId(problemId, username, pageable);
    }

    public Flux<Submission> findSubmissionsByStatusIn(List<Submission.Status> statuses, Pageable pageable) {
        return submissionRepository.findAllByStatusIn(statuses, pageable);
    }

    public Flux<ProblemMetadata> updateStatusInMetadataMany(Authentication authentication, List<ProblemMetadata> problemMetadataList) {
        return Flux.concat(
                problemMetadataList.stream()
                        .map(problemMetadata -> updateStatusInMetadata(authentication, problemMetadata))
                        .map(it -> it.doOnNext(System.out::println))
                        .toList()
        );
    }

    public Mono<ProblemMetadata> updateStatusInMetadata(Authentication authentication, ProblemMetadata problemMetadata) {
        return Mono.justOrEmpty(problemMetadata)
                .zipWhen(metadata -> collectProblemStatus(authentication, metadata.getName()))
                .map(tuple -> {
                    ProblemMetadata metadata = tuple.getT1();
                    Problem.Status status = tuple.getT2();
                    metadata.setStatus(status);
                    return metadata;
                })
                .defaultIfEmpty(problemMetadata);
    }

    public Mono<Problem.Status> collectProblemStatus(Authentication authentication, String problemId) {
        return Mono.justOrEmpty(authentication)
                .flatMap(auth ->
                        submissionRepository.countAllForUserByStatus(problemId, auth.getName()).collectList()
                )
                .map(StatusCount::asMap)
                .flatMap(this::statusDecision);
    }

    /**
     * Submission.Status.SUCCESS > 0    =>  Problem.Status.SOLVED
     * Submission.Status.FAILED > 0     =>  Problem.Status.FAILED
     * Submission.Status.PENDING > 0    =>  Problem.Status.SOLVING
     * else                             =>  Problem.Status.NOT_SOLVED
     */
    private Mono<Problem.Status> statusDecision(Map<Submission.Status, Long> statusCounts) {
        return Mono.fromCallable(() -> {
            if (statusCounts.getOrDefault(Submission.Status.SUCCESS, 0L) > 0) {
                return Problem.Status.SOLVED;
            } else if (statusCounts.getOrDefault(Submission.Status.FAILED, 0L) > 0) {
                return Problem.Status.FAILED;
            } else if (statusCounts.getOrDefault(Submission.Status.PENDING, 0L) > 0) {
                return Problem.Status.SOLVING;
            } else {
                return Problem.Status.NOT_SOLVED;
            }
        });
    }
}
