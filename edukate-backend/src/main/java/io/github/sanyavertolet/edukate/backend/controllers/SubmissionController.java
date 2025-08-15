package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.domain.CheckType;
import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import io.github.sanyavertolet.edukate.backend.entities.files.TempFileKey;
import io.github.sanyavertolet.edukate.backend.services.files.BaseFileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "API for managing problem submissions")
public class SubmissionController {
    private final ProblemService problemService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final BaseFileService baseFileService;

    @GetMapping("/by-id")
    @Operation(
            summary = "Get submission by ID",
            description = "Retrieves a specific submission by its ID for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submission",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - user name does not match",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "id", description = "Submission ID", in = QUERY, required = true),
    })
    public Mono<SubmissionDto> getSubmissionById(@RequestParam String id, Authentication authentication) {
        return submissionService.findSubmissionById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submission.getUserId().equals(AuthUtils.id(authentication)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")))
                .flatMap(submissionService::createSubmissionDto);
    }

    @PostMapping
    @Operation(
            summary = "Upload a submission",
            description = "Creates a new submission for a problem with the provided files"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created submission",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - No submit permission",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User, problem, or files not found", content = @Content),
    })
    @Parameters({
            @Parameter(name = "check", in = QUERY,
                    description = "Type of check to perform on the submission. Reserved for future use"),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateSubmissionRequest.class)
            )
    )
    public Mono<SubmissionDto> uploadSubmission(
            @RequestBody CreateSubmissionRequest submissionRequest,
            @RequestParam(required = false, defaultValue = "SELF", name = "check") CheckType checkType,
            Authentication authentication
    ) {
        return userService.findUserByAuthentication(authentication)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .filterWhen(userService::hasUserPermissionToSubmit)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough permission")))
                .filterWhen(_ -> problemService.findProblemById(submissionRequest.getProblemId()).hasElement())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found.")))
                .flatMap(user -> Flux.fromIterable(submissionRequest.getFileNames())
                        .map(fileName -> TempFileKey.of(user.getId(), fileName))
                        .cast(FileKey.class)
                        .collectList()
                        .filterWhen(baseFileService::doFilesExist)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Could not find files."
                        )))
                        .then(submissionService.saveSubmission(submissionRequest, authentication))
                )
                .flatMap(submissionService::createSubmissionDto);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @GetMapping("/{problemId}/{username}")
    @Operation(
            summary = "Get submissions by username and problem ID",
            description = "Retrieves paginated submissions for a specific user and problem"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubmissionDto.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires MODERATOR role",
                    content = @Content)
    })
    @Parameters({
            @Parameter(name = "problemId", description = "Problem ID", in = PATH, required = true),
            @Parameter(name = "username", description = "Username", in = PATH, required = true),
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of submissions per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<SubmissionDto> getSubmissionsByUsernameAndProblemId(
            @PathVariable String problemId,
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return userService.findUserByName(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + username + " not found")))
                .flatMapMany(user ->
                        submissionService.findSubmissionsByProblemIdAndUserId(
                                problemId, user.getId(),
                                PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")))
                .flatMap(submissionService::createSubmissionDto);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @GetMapping
    @Operation(
            summary = "Get all successful submissions",
            description = "Retrieves paginated list of all submissions with SUCCESS status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubmissionDto.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied - Requires MODERATOR role",
                    content = @Content)
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY),
            @Parameter(name = "size", description = "Number of submissions per page", in = QUERY)
    })
    public Flux<SubmissionDto> getSubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return submissionService.findSubmissionsByStatusIn(
                List.of(Submission.Status.SUCCESS),
                PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
        )
                .flatMap(submissionService::createSubmissionDto);
    }
}
