package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.CheckResultDto
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.services.CheckResultService
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/checker")
@Tag(name = "Checker", description = "API for scheduling checks and retrieving check results")
@SecurityRequirement(name = "cookieAuth")
class CheckerController(
    private val checkResultService: CheckResultService,
    private val checkerSchedulerService: CheckerSchedulerService,
    private val submissionService: SubmissionService,
) {
    @PostMapping("/ai")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @Operation(summary = "Schedule AI check", description = "Schedules an asynchronous AI check for the provided submission")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Check scheduled"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied"),
                ApiResponse(responseCode = "404", description = "Submission not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "id",
                    description = "Submission identifier",
                    `in` = ParameterIn.QUERY,
                    required = true,
                    schema = Schema(implementation = String::class),
                )
            ]
    )
    fun aiCheck(@RequestParam(name = "id") submissionId: Long, authentication: Authentication): Mono<ResponseEntity<Void>> =
        submissionService
            .getSubmissionIfOwns(submissionId, requireNotNull(authentication.id()))
            .flatMap { checkerSchedulerService.scheduleCheck(it) }
            .thenReturn(ResponseEntity.accepted().build())

    @PostMapping("/self")
    @Operation(
        summary = "Mark submission as self-checked",
        description = "Creates a self-check result for the provided submission",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Self-check accepted"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied"),
                ApiResponse(responseCode = "404", description = "Submission not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "id",
                    description = "Submission identifier",
                    `in` = ParameterIn.QUERY,
                    required = true,
                    schema = Schema(implementation = String::class),
                )
            ]
    )
    fun selfCheck(
        @RequestParam(name = "id") submissionId: Long,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> =
        submissionService
            .getSubmissionIfOwns(submissionId, requireNotNull(authentication.id()))
            .map { submission -> CheckResult.self(requireNotNull(submission.id)) }
            .flatMap { checkResultService.saveAndUpdateSubmission(it) }
            .map { ResponseEntity.accepted().build() }

    @Suppress("unused")
    @PostMapping("/supervisor")
    @Operation(
        summary = "Request supervisor check",
        description = "Requests a supervisor check for the provided submission (not implemented yet)",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Reserved for future implementation"),
                ApiResponse(responseCode = "501", description = "Not implemented"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "id",
                    description = "Submission identifier",
                    `in` = ParameterIn.QUERY,
                    required = true,
                    schema = Schema(implementation = String::class),
                )
            ]
    )
    fun supervisorCheck(
        @RequestParam(name = "id") submissionId: Long,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> = Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build())

    @GetMapping("/by-id/{id}")
    @Operation(summary = "Get check result by id", description = "Retrieves a single check result by its identifier")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved check result"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied"),
                ApiResponse(responseCode = "404", description = "Check result not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "id",
                    description = "Check result identifier",
                    `in` = ParameterIn.PATH,
                    required = true,
                    schema = Schema(implementation = String::class),
                )
            ]
    )
    fun getCheckResultById(@PathVariable id: Long, authentication: Authentication): Mono<CheckResultDto> =
        checkResultService
            .findById(id)
            .orNotFound("Check result not found")
            .flatMap { result ->
                val requesterId = requireNotNull(authentication.id())
                submissionService.getSubmissionIfOwns(result.submissionId, requesterId).thenReturn(result)
            }
            .map { it.toCheckResultDto() }

    @GetMapping("/submissions/{submissionId}")
    @Operation(
        summary = "Get check results for submission",
        description = "Retrieves all check results (lightweight info) for a submission owned by the user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved check results"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Access denied"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "submissionId",
                    description = "Submission identifier",
                    `in` = ParameterIn.PATH,
                    required = true,
                    schema = Schema(implementation = String::class),
                )
            ]
    )
    fun getCheckResultsBySubmissionId(
        @PathVariable submissionId: Long,
        authentication: Authentication,
    ): Flux<CheckResultInfo> =
        submissionService
            .getSubmissionIfOwns(submissionId, requireNotNull(authentication.id()))
            .mapNotNull { it.id }
            .flatMapMany { checkResultService.findAllBySubmissionId(it) }
            .map { it.toCheckResultInfo() }
}
