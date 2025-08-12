package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.entities.files.SubmissionFileKey;
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository;
import io.github.sanyavertolet.edukate.backend.services.files.BaseFileService;
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService;
import io.github.sanyavertolet.edukate.backend.utils.StatusCount;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final BaseFileService baseFileService;
    private final SubmissionFileService submissionFileService;
    private final UserService userService;

    @Transactional
    public Mono<Submission> saveSubmission(CreateSubmissionRequest submissionRequest, Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMap(userId -> saveSubmission(userId, submissionRequest));
    }

    @Transactional
    public Mono<Submission> saveSubmission(String userId, CreateSubmissionRequest submissionRequest) {
        return Mono.just(submissionRequest)
                .map(request -> Submission.of(request.getProblemId(), userId, request.getFileKeys()))
                .flatMap(submissionRepository::save)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(submission ->
                        submissionFileService.moveSubmissionFiles(userId, submission.getId(), submissionRequest).subscribe()
                );
    }

    public Mono<Submission> findSubmissionById(String id) {
        return submissionRepository.findById(id);
    }

    public Flux<Submission> findSubmissionsByProblemIdAndUserId(String problemId, String userId, Pageable pageable) {
        return submissionRepository.findAllByProblemIdAndUserId(problemId, userId, pageable);
    }

    public Flux<Submission> findSubmissionsByStatusIn(List<Submission.Status> statuses, Pageable pageable) {
        return submissionRepository.findAllByStatusIn(statuses, pageable);
    }

    public Flux<ProblemMetadata> updateStatusInMetadataMany(Authentication authentication, List<ProblemMetadata> problemMetadataList) {
        return Flux.concat(
                problemMetadataList.stream()
                        .map(problemMetadata -> updateStatusInMetadata(authentication, problemMetadata))
                        .toList()
        );
    }

    public Mono<ProblemMetadata> updateStatusInMetadata(Authentication authentication, ProblemMetadata problemMetadata) {
        return Mono.justOrEmpty(problemMetadata)
                .zipWhen(metadata -> collectProblemStatus(metadata.getName(), authentication))
                .map(tuple -> {
                    ProblemMetadata metadata = tuple.getT1();
                    Problem.Status status = tuple.getT2();
                    metadata.setStatus(status);
                    return metadata;
                })
                .defaultIfEmpty(problemMetadata);
    }

    public Mono<ProblemDto> updateStatusInDto(Authentication authentication, ProblemDto problemDto) {
        return Mono.justOrEmpty(problemDto)
                .zipWhen(dto -> collectProblemStatus(dto.getId(), authentication))
                .map(tuple -> {
                    ProblemDto dto = tuple.getT1();
                    Problem.Status status = tuple.getT2();
                    dto.setStatus(status);
                    return dto;
                })
                .defaultIfEmpty(problemDto);
    }

    private Mono<Problem.Status> collectProblemStatus(String problemId, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMap(userId -> submissionRepository.countAllForUserByStatus(problemId, userId).collectList())
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

    public Mono<SubmissionDto> createSubmissionDto(Submission submission) {
        return Mono.justOrEmpty(submission)
                .map(sbm -> new SubmissionDto(
                        sbm.getId(), sbm.getProblemId(), sbm.getUserId(),
                        sbm.getStatus(), sbm.getCreatedAt(), sbm.getFileKeys()
                ))
                .flatMap(this::updateFileUrlsInDto)
                .flatMap(this::updateUserNameInDto);
    }

    private Mono<SubmissionDto> updateUserNameInDto(SubmissionDto submissionDto) {
        return userService.findUserById(submissionDto.getUserName())
                .map(User::getName)
                .defaultIfEmpty("UNKNOWN")
                .map(submissionDto::withUserName);
    }

    private Mono<SubmissionDto> updateFileUrlsInDto(SubmissionDto submissionDto) {
        return Flux.fromIterable(submissionDto.getFileUrls())
                .map(fileName -> SubmissionFileKey.of(
                        submissionDto.getUserName(), submissionDto.getProblemId(), submissionDto.getId(), fileName
                ))
                .flatMap(baseFileService::getDownloadUrlOrEmpty)
                .collectList()
                .map(submissionDto::withFileUrls);
    }
}
