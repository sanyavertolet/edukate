package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.domain.CheckType;
import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.backend.storage.FileKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final ProblemService problemService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final FileService fileService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/by-id")
    public Mono<SubmissionDto> getSubmissionById(
            @RequestParam String id,
            Authentication authentication
    ) {
        return submissionService.findSubmissionById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submission.getUserId().equals(authentication.getName()))
                .map(Submission::toDto)
                .flatMap(submissionService::updateFileUrlsInDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public Mono<SubmissionDto> uploadSubmission(
            @RequestBody CreateSubmissionRequest submissionRequest,
            @RequestParam(required = false, defaultValue = "SELF", name = "check") CheckType checkType,
            Authentication authentication
    ) {
        return userService.findUserByName(authentication.getName())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .filterWhen(userService::hasUserPermissionToSubmit)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permission")))
                .filterWhen(_ -> problemExists(submissionRequest.getProblemId()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found.")))
                .then(Mono.fromCallable(submissionRequest::getFileKeys))
                .flatMapMany(Flux::fromIterable)
                .map(fileKey -> FileKeys.temp(authentication.getName(), fileKey))
                .collectList()
                .filterWhen(fileService::doFilesExist)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find files.")))
                .flatMap(_ -> submissionService.saveSubmission(authentication.getName(), submissionRequest))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save submission")))
                .map(Submission::toDto)
                .flatMap(submissionService::updateFileUrlsInDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{problemId}/{username}")
    public Flux<SubmissionDto> getSubmissionsByUsernameAndProblemId(
            @PathVariable String problemId,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return submissionService.findSubmissionsByUsernameAndProblemId(
                username,
                problemId,
                PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
        )
                .map(Submission::toDto)
                .flatMap(submissionService::updateFileUrlsInDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public Flux<SubmissionDto> getSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return submissionService.findSubmissionsByStatusIn(
                List.of(Submission.Status.SUCCESS),
                PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
        ).map(Submission::toDto);
    }

    private Mono<Boolean> problemExists(String problemId) {
        return problemService.findProblemById(problemId).hasElement();
    }
}
