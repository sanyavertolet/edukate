package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.backend.dtos.SubmissionDto
import io.github.sanyavertolet.edukate.backend.mappers.SubmissionMapper
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.common.utils.orForbidden
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@RestController
@Validated
@RequestMapping("/api/v1/submissions")
@Tag(name = "Submissions", description = "API for managing problem submissions")
class SubmissionController(
    private val problemService: ProblemService,
    private val userService: UserService,
    private val submissionService: SubmissionService,
    private val submissionMapper: SubmissionMapper,
    private val fileManager: FileManager,
) {
    @GetMapping("/by-id/{id}")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get submission by ID",
        description = "Retrieves a specific submission by its ID for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved submission"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - user id does not match"),
                ApiResponse(responseCode = "404", description = "Submission not found"),
            ]
    )
    @Parameters(value = [Parameter(name = "id", description = "Submission ID", `in` = ParameterIn.PATH, required = true)])
    fun getSubmissionById(@PathVariable id: Long, authentication: Authentication): Mono<SubmissionDto> =
        authentication
            .monoId()
            .flatMap { userId ->
                submissionService
                    .findById(id)
                    .orNotFound("Submission not found")
                    .filter { it.userId == userId }
                    .orForbidden("Access denied")
            }
            .flatMap { submissionMapper.toDto(it) }

    @PostMapping
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Upload a submission",
        description = "Creates a new submission for a problem with the provided files",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully created submission"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - No submit permission"),
                ApiResponse(responseCode = "404", description = "User, problem, or files not found"),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = CreateSubmissionRequest::class),
                )
            ],
    )
    fun uploadSubmission(
        @RequestBody @Valid submissionRequest: CreateSubmissionRequest,
        authentication: Authentication,
    ): Mono<SubmissionDto> =
        authentication
            .monoId()
            .flatMap { userService.findUserById(it) }
            .orNotFound("User not found")
            .filterWhen { userService.hasUserPermissionToSubmit(it) }
            .orForbidden("Not enough permission")
            .filterWhen { problemService.findProblemByKey(submissionRequest.problemKey).hasElement() }
            .orNotFound("Problem not found.")
            .flatMap { user ->
                submissionRequest.fileNames
                    .toFlux()
                    .map { fileName -> TempFileKey(requireNotNull(user.id), fileName) as FileKey }
                    .collectList()
                    .filterWhen { fileManager.doFilesExist(it) }
                    .orNotFound("Could not find files.")
                    .then(submissionService.saveSubmission(submissionRequest, authentication))
            }
            .flatMap { submissionMapper.toDto(it) }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @GetMapping("/{bookSlug}/{code}/{username}")
    @Operation(
        summary = "Get submissions by username and problem key",
        description = "Retrieves paginated submissions for a specific user and problem",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved submissions"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied - Requires MODERATOR role"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "bookSlug", description = "Book slug", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "code", description = "Problem code", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "username", description = "Username", `in` = ParameterIn.PATH, required = true),
                Parameter(
                    name = "page",
                    description = "Page number (zero-based)",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    description = "Number of submissions per page",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getSubmissionsByUsernameAndProblemKey(
        @PathVariable bookSlug: String,
        @PathVariable code: String,
        @PathVariable username: String,
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
    ): Flux<SubmissionDto> {
        val problemKey = "$bookSlug/$code"
        return Mono.zip(
                userService.findUserByName(username).orNotFound("User $username not found"),
                problemService.findProblemByKey(problemKey).orNotFound("Problem $problemKey not found"),
            )
            .flatMapMany { tuple ->
                submissionService.findSubmissionsByProblemIdAndUserId(
                    requireNotNull(tuple.t2.id),
                    requireNotNull(tuple.t1.id),
                    sortedPageable(page, size),
                )
            }
            .flatMapSequential { submissionMapper.toDto(it) }
    }

    @Suppress("MaxLineLength")
    @GetMapping("/my")
    @SecurityRequirement(name = "cookieAuth")
    @Operation(
        summary = "Get my submissions",
        description =
            "Requires an authenticated session via the gateway cookie. Returns the authenticated user's submissions, optionally filtered by problemId. Results are paginated (page, size) and sorted by createdAt in descending order.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved submissions"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "problemKey",
                    `in` = ParameterIn.QUERY,
                    description = "Optional problem key (bookSlug/code) to filter the user's submissions",
                ),
                Parameter(
                    name = "page",
                    `in` = ParameterIn.QUERY,
                    description = "Page number (zero-based)",
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    `in` = ParameterIn.QUERY,
                    description = "Number of submissions per page (max 100)",
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getMySubmissions(
        @RequestParam(required = false) problemKey: String?,
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication,
    ): Flux<SubmissionDto> =
        authentication
            .monoId()
            .flatMapMany { userId ->
                if (problemKey != null) {
                    resolveProblemId(problemKey).flatMapMany { problemId ->
                        submissionService.findUserSubmissions(userId, problemId, sortedPageable(page, size))
                    }
                } else {
                    submissionService.findUserSubmissions(userId, null, sortedPageable(page, size))
                }
            }
            .flatMapSequential { submissionMapper.toDto(it) }

    private fun resolveProblemId(key: String): Mono<Long> =
        problemService.findProblemByKey(key).orNotFound("Problem $key not found").mapNotNull { problem -> problem.id }

    private fun sortedPageable(page: Int, size: Int): Pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt")
}
