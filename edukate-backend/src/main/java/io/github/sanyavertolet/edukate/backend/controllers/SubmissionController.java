package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest;
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto;
import io.github.sanyavertolet.edukate.backend.entities.files.FileKey;
import io.github.sanyavertolet.edukate.backend.entities.files.TempFileKey;
import io.github.sanyavertolet.edukate.backend.services.files.BaseFileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.github.sanyavertolet.edukate.backend.services.UserService;
import io.github.sanyavertolet.edukate.common.SubmissionStatus;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
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
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/submissions")
@Tag(name = "Submissions", description = "API for managing problem submissions")
public class SubmissionController {
    private final ProblemService problemService;
    private final UserService userService;
    private final SubmissionService submissionService;
    private final BaseFileService baseFileService;

    @GetMapping("/by-id/{id}")
    @Operation(
            summary = "Get submission by ID",
            description = "Retrieves a specific submission by its ID for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submission",
                    content = @Content(schema = @Schema(implementation = SubmissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - user id does not match",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "id", description = "Submission ID", in = PATH, required = true),
    })
    public Mono<SubmissionDto> getSubmissionById(
            @PathVariable @NotBlank String id,
            Authentication authentication
    ) {
        return submissionService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found")))
                .filter(submission -> submission.getUserId().equals(AuthUtils.id(authentication)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")))
                .flatMap(submissionService::prepareDto);
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
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateSubmissionRequest.class)
            )
    )
    public Mono<SubmissionDto> uploadSubmission(
            @RequestBody @Valid CreateSubmissionRequest submissionRequest,
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
                .flatMap(submissionService::prepareDto);
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
            @PathVariable @NotBlank String problemId,
            @PathVariable @NotBlank String username,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return userService.findUserByName(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User " + username + " not found")))
                .flatMapMany(user -> submissionService.findSubmissionsByProblemIdAndUserId(
                        problemId, user.getId(), sortedPageable(page, size)
                ))
                .flatMapSequential(submissionService::prepareDto);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my submissions",
            description = "Requires an authenticated session via the gateway cookie. " +
                    "Returns the authenticated user's submissions, optionally filtered by problemId. " +
                    "Results are paginated (page, size) and sorted by createdAt in descending order."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved submissions", content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = SubmissionDto.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @Parameters({
            @Parameter(name = "problemId", in = QUERY,
                    description = "Optional problem ID to filter the user's submissions"),
            @Parameter(name = "page", in = QUERY, description = "Page number (zero-based)",
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", in = QUERY, description = "Number of submissions per page (max 100)",
                    schema = @Schema(minimum = "1", maximum = "100"))
    })
    public Flux<SubmissionDto> getMySubmissions(
            @RequestParam(required = false) String problemId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size,
            Authentication authentication
    ) {
        return AuthUtils.monoId(authentication)
                .flatMapMany(userId ->
                        submissionService.findUserSubmissions(userId, problemId, sortedPageable(page, size))
                )
                .flatMapSequential(submissionService::prepareDto);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @GetMapping("/all")
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
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY,
                    schema = @Schema(minimum = "0")),
            @Parameter(name = "size", description = "Number of submissions per page", in = QUERY,
                    schema = @Schema(minimum = "1", maximum = "100")),
    })
    public Flux<SubmissionDto> getSubmissions(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Positive int size
    ) {
        return submissionService.findSubmissionsByStatusIn(
                List.of(SubmissionStatus.SUCCESS), sortedPageable(page, size)
        )
                .flatMap(submissionService::prepareDto);
    }

    private Pageable sortedPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
    }
}
