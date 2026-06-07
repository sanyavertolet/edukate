package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.CheckResultDto
import io.github.sanyavertolet.edukate.backend.dtos.CreateSupervisorTicketRequest
import io.github.sanyavertolet.edukate.backend.dtos.SupervisorTicketDto
import io.github.sanyavertolet.edukate.backend.dtos.SupervisorVerdict
import io.github.sanyavertolet.edukate.backend.entities.CheckResult
import io.github.sanyavertolet.edukate.backend.entities.SupervisorTicket
import io.github.sanyavertolet.edukate.backend.mappers.SupervisorTicketMapper
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import io.github.sanyavertolet.edukate.backend.repositories.SupervisorTicketRepository
import io.github.sanyavertolet.edukate.backend.services.CheckResultService
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.backend.services.UserService
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo
import io.github.sanyavertolet.edukate.common.checks.SupervisorTicketStatus
import io.github.sanyavertolet.edukate.common.notifications.CheckedNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.notifications.SimpleNotificationCreateRequest
import io.github.sanyavertolet.edukate.common.services.Notifier
import io.github.sanyavertolet.edukate.common.utils.forbiddenIf
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
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@RestController
@RequestMapping("/api/v1/checker")
@Tag(name = "Checker", description = "API for scheduling checks and retrieving check results")
@SecurityRequirement(name = "cookieAuth")
class CheckerController(
    private val checkResultService: CheckResultService,
    private val checkerSchedulerService: CheckerSchedulerService,
    private val submissionService: SubmissionService,
    private val supervisorTicketRepository: SupervisorTicketRepository,
    private val supervisorTicketMapper: SupervisorTicketMapper,
    private val problemSetService: ProblemSetService,
    private val userService: UserService,
    private val problemRepository: ProblemRepository,
    private val notifier: Notifier,
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
            .flatMap { checkResultService.saveCheckResult(it) }
            .map { ResponseEntity.accepted().build() }

    @PostMapping("/supervisor")
    @Operation(
        summary = "Request supervisor check",
        description =
            """
            Creates a supervisor review ticket for the given submission and notifies the assigned supervisor.
            The caller must be the owner of the submission.
            The specified supervisor must have at least the MODERATOR role in the given problem set.
            A PENDING check result stub is created and linked to the ticket; it will be resolved when
            the supervisor submits a verdict via POST /supervisor/{ticketId}/verdict.
        """,
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Supervisor ticket created and supervisor notified"),
                ApiResponse(responseCode = "400", description = "Malformed request body"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Not submission owner or supervisor lacks moderator role"),
                ApiResponse(responseCode = "404", description = "Submission, problem set, or supervisor not found"),
            ]
    )
    fun supervisorCheck(
        @RequestBody request: CreateSupervisorTicketRequest,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> {
        val callerId = requireNotNull(authentication.id())
        return submissionService
            .getSubmissionIfOwns(request.submissionId, callerId)
            .orNotFound("Submission not found")
            .flatMap {
                Mono.zip(
                    problemSetService.findByShareCode(request.problemSetCode).orNotFound("Problem set not found"),
                    userService.findUserByName(request.supervisorName).orNotFound("Supervisor not found"),
                )
            }
            .flatMap { (problemSet, supervisor) ->
                val supervisorId = requireNotNull(supervisor.id)
                with(problemSetService) { Mono.just(problemSet).assertIsModeratorOf(supervisorId) }
                    .flatMap { checkResultService.saveCheckResult(CheckResult.supervisorPending(request.submissionId)) }
                    .flatMap { stub ->
                        supervisorTicketRepository.save(
                            SupervisorTicket(
                                submissionId = request.submissionId,
                                problemSetId = requireNotNull(problemSet.id),
                                supervisorId = supervisorId,
                                checkResultId = requireNotNull(stub.id),
                            )
                        )
                    }
            }
            .flatMap { ticket ->
                notifier.notify(
                    SimpleNotificationCreateRequest(
                        targetUserId = ticket.supervisorId,
                        title = "New supervisor review request",
                        message = "A submission has been assigned to you for review.",
                        source = "edukate-checker",
                    )
                )
            }
            .thenReturn(ResponseEntity.accepted().build())
    }

    @PostMapping("/supervisor/{ticketId}/verdict")
    @Operation(
        summary = "Submit supervisor verdict",
        description =
            """
            Submits a verdict for a supervisor review ticket.
            The caller must be the supervisor assigned to the ticket (supervisorId matches authenticated user).
            Allowed statuses in the verdict are SUCCESS and MISTAKE only.
            On success the linked check result is promoted from PENDING to the verdict status (trustLevel = 1.0),
            the ticket is marked RESOLVED, and the submission owner receives a notification.
        """,
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "202", description = "Verdict accepted; check result promoted, owner notified"),
                ApiResponse(responseCode = "400", description = "Bad request: SUCCESS/MISTAKE only, explanation non-blank"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "403", description = "Caller is not the assigned supervisor for this ticket"),
                ApiResponse(responseCode = "404", description = "Ticket or associated submission not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "ticketId",
                    description = "Supervisor ticket identifier (from GET /supervisor/my-tickets)",
                    `in` = ParameterIn.PATH,
                    required = true,
                    schema = Schema(implementation = Long::class),
                )
            ]
    )
    fun supervisorVerdict(
        @PathVariable ticketId: Long,
        @RequestBody @Valid verdict: SupervisorVerdict,
        authentication: Authentication,
    ): Mono<ResponseEntity<Void>> {
        val callerId = requireNotNull(authentication.id())
        return supervisorTicketRepository
            .findById(ticketId)
            .orNotFound("Supervisor ticket not found")
            .forbiddenIf("Not the assigned supervisor") { it.supervisorId != callerId }
            .flatMap { ticket ->
                Mono.zip(
                        checkResultService.promoteWithVerdict(ticket.checkResultId, verdict),
                        supervisorTicketRepository.save(ticket.copy(status = SupervisorTicketStatus.RESOLVED)),
                        submissionService.findById(ticket.submissionId).orNotFound("Submission not found"),
                    )
                    .flatMap { (saved, _, submission) ->
                        problemRepository.findById(submission.problemId).orNotFound("Problem not found").flatMap { problem ->
                            notifier.notify(
                                CheckedNotificationCreateRequest.from(
                                    submission.userId,
                                    ticket.submissionId,
                                    problem.key,
                                    saved.status,
                                )
                            )
                        }
                    }
            }
            .thenReturn(ResponseEntity.accepted().build())
    }

    @GetMapping("/supervisor/my-tickets")
    @Operation(
        summary = "List my supervisor tickets",
        description =
            """
            Returns supervisor review tickets assigned to the authenticated user, newest first.
            Optionally filtered to a single problem set via the problemSetCode query parameter.
            Any authenticated user may call this endpoint; results are always scoped to the caller.
            Supports standard Spring pagination parameters: page (0-based), size (default 20).
        """,
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Ticket list (may be empty)"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "problemSetCode",
                    description = "Optional problem set share code to filter results to a single problem set",
                    `in` = ParameterIn.QUERY,
                    required = false,
                    schema = Schema(implementation = String::class),
                ),
                Parameter(
                    name = "page",
                    description = "Zero-based page index (default 0)",
                    `in` = ParameterIn.QUERY,
                    required = false,
                    schema = Schema(implementation = Int::class),
                ),
                Parameter(
                    name = "size",
                    description = "Page size (default 20)",
                    `in` = ParameterIn.QUERY,
                    required = false,
                    schema = Schema(implementation = Int::class),
                ),
            ]
    )
    fun getMySupervisorTickets(
        @RequestParam(required = false) problemSetCode: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        authentication: Authentication,
    ): Flux<SupervisorTicketDto> {
        val callerId = requireNotNull(authentication.id())
        val pageable: Pageable = PageRequest.of(page, size)
        val tickets =
            if (problemSetCode != null) {
                supervisorTicketRepository.findBySupervisorIdAndProblemSetCode(callerId, problemSetCode, pageable)
            } else {
                supervisorTicketRepository.findBySupervisorId(callerId, pageable)
            }
        return tickets.flatMap { supervisorTicketMapper.toDto(it) }
    }

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
        description = "Retrieves all check results (lightweight info) for any submission",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved check results"),
                ApiResponse(responseCode = "401", description = "Unauthorized"),
                ApiResponse(responseCode = "404", description = "Submission not found"),
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
    fun getCheckResultsBySubmissionId(@PathVariable submissionId: Long): Flux<CheckResultInfo> =
        submissionService
            .findById(submissionId)
            .orNotFound("Submission not found")
            .mapNotNull { it.id }
            .flatMapMany { checkResultService.findAllBySubmissionId(it) }
            .map { it.toCheckResultInfo() }
}
