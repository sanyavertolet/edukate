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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Submissions", description = "API for managing problem submissions")
public class SubmissionController {
    private final ProblemService problemService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final FileService fileService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/by-id")
    @Operation(
            summary = "Get submission by ID",
            description = "Retrieves a specific submission by its ID for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submission",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "id", description = "Submission ID", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true),
    })
    public Mono<SubmissionDto> getSubmissionById(
            @RequestParam String id,
            Authentication authentication
    ) {
        return submissionService.findSubmissionById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submission.getUserName().equals(authentication.getName()))
                .map(Submission::toDto)
                .flatMap(submissionService::updateFileUrlsInDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    @Operation(
            summary = "Upload a submission",
            description = "Creates a new submission for a problem with the provided files"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created submission",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not enough permissions", content = @Content),
            @ApiResponse(responseCode = "404", description = "User, problem, or files not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to save submission", content = @Content)
    })
    @Parameters({
            @Parameter(name = "submissionRequest", description = "Submission details", required = true),
            @Parameter(name = "check", description = "Type of check to perform on the submission"),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
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
    @Operation(
            summary = "Get submissions by username and problem ID",
            description = "Retrieves paginated submissions for a specific user and problem"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @Parameters({
            @Parameter(name = "problemId", description = "Problem ID", required = true),
            @Parameter(name = "username", description = "Username", required = true),
            @Parameter(name = "page", description = "Page number (zero-based)"),
            @Parameter(name = "size", description = "Number of submissions per page")
    })
    public Flux<SubmissionDto> getSubmissionsByUsernameAndProblemId(
            @PathVariable String problemId,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return submissionService.findSubmissionsByUserNameAndProblemId(
                username,
                problemId,
                PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
        )
                .map(Submission::toDto)
                .flatMap(submissionService::updateFileUrlsInDto);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    @Operation(
            summary = "Get all successful submissions",
            description = "Retrieves paginated list of all submissions with SUCCESS status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)"),
            @Parameter(name = "size", description = "Number of submissions per page")
    })
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
