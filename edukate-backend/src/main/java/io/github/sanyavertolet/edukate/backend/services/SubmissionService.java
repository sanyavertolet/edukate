package io.github.sanyavertolet.edukate.backend.services;

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.entities.files.FileObject;
import io.github.sanyavertolet.edukate.backend.entities.files.SubmissionFileKey;
import io.github.sanyavertolet.edukate.backend.permissions.SubmissionPermissionEvaluator;
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository;
import io.github.sanyavertolet.edukate.backend.repositories.SubmissionRepository;
import io.github.sanyavertolet.edukate.backend.services.files.BaseFileService;
import io.github.sanyavertolet.edukate.backend.services.files.SubmissionFileService;
import io.github.sanyavertolet.edukate.common.SubmissionStatus;
import io.github.sanyavertolet.edukate.common.checks.SubmissionContext;
import io.github.sanyavertolet.edukate.common.entities.User;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private static final String DEFAULT_USER_NAME = "UNKNOWN";
    private final SubmissionRepository submissionRepository;
    private final BaseFileService baseFileService;
    private final SubmissionFileService submissionFileService;
    private final UserService userService;
    private final FileObjectRepository fileObjectRepository;
    private final ProblemService problemService;
    private final SubmissionPermissionEvaluator submissionPermissionEvaluator;

    @Transactional
    public Mono<Submission> saveSubmission(CreateSubmissionRequest submissionRequest, Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMap(userId -> saveSubmission(userId, submissionRequest));
    }

    public Mono<Submission> update(Submission submission) {
        return submissionRepository.save(submission);
    }

    @Transactional
    public Mono<Submission> saveSubmission(String userId, CreateSubmissionRequest submissionRequest) {
        return Mono.just(Submission.of(submissionRequest.getProblemId(), userId))
                .flatMap(submissionRepository::save)
                .flatMap(submission ->
                        submissionFileService.moveSubmissionFiles(userId, submission.getId(), submissionRequest)
                                .then(Flux.fromIterable(submissionRequest.getFileNames())
                                        .map(fileName -> SubmissionFileKey.of(
                                                userId, submissionRequest.getProblemId(), submission.getId(), fileName
                                        ).toString())
                                        .flatMap(fileObjectRepository::findByKeyPath)
                                        .map(FileObject::getId)
                                        .collectList()
                                )
                                .flatMap(ids -> {
                                    submission.setFileObjectIds(ids);
                                    return submissionRepository.save(submission);
                                })
                );
    }

    public Mono<Submission> findById(String id) {
        return submissionRepository.findById(id);
    }

    public Flux<Submission> findSubmissionsByProblemIdAndUserId(String problemId, String userId, Pageable pageable) {
        return submissionRepository.findAllByProblemIdAndUserId(problemId, userId, pageable);
    }

    /**
     * If problemId is null, then returns submissions by user id regardless of the problem.
     */
    public Flux<Submission> findUserSubmissions(String userId, @Nullable String problemId, Pageable pageable) {
        if (problemId != null) {
            return submissionRepository.findAllByProblemIdAndUserId(problemId, userId, pageable);
        }
        return submissionRepository.findAllByUserId(userId, pageable);
    }

    public Flux<Submission> findSubmissionsByStatusIn(List<SubmissionStatus> statuses, Pageable pageable) {
        return submissionRepository.findAllByStatusIn(statuses, pageable);
    }

    public Mono<Submission> getSubmissionIfOwns(String submissionId, String userId) {
        return submissionRepository.findById(submissionId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submissionPermissionEvaluator.isOwner(submission, userId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")));
    }

    public Mono<SubmissionDto> prepareDto(@NonNull Submission submission) {
        return Mono.fromCallable(() -> SubmissionDto.builder()
                        .id(submission.getId())
                        .problemId(submission.getProblemId())
                        .status(submission.getStatus())
                        .createdAt(submission.getCreatedAt())
                        .build()
                )
                .flatMap(dto -> fileObjectRepository.findAllById(submission.getFileObjectIds())
                        .map(FileObject::getKey)
                        .flatMapSequential(baseFileService::getDownloadUrlOrEmpty)
                        .collectList()
                        .map(dto::withFileUrls)
                )
                .flatMap(dto -> userService.findUserById(submission.getUserId())
                        .map(User::getName)
                        .defaultIfEmpty(DEFAULT_USER_NAME)
                        .map(dto::withUserName)
                );
    }

    public Mono<SubmissionContext> prepareContext(@NonNull Submission submission) {
        return Mono.fromCallable(submission::getProblemId)
                .flatMap(problemService::findProblemById)
                .flatMap(problem -> {
                    String problemText = problem.getText();
                    return baseFileService.getDownloadUrlsByFileObjectIds(submission.getFileObjectIds()).collectList()
                            .zipWith(problemService.problemImageDownloadUrls(problem.getId(), problem.getImages())
                                    .collectList())
                            .map(tuple -> SubmissionContext.builder()
                                    .submissionId(submission.getId())
                                    .problemId(problem.getId())
                                    .problemText(problemText)
                                    .submissionImageUrls(tuple.getT1())
                                    .problemImageUrls(tuple.getT2())
                                    .build()
                            );
                });
    }
}
