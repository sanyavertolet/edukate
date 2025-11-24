package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.CheckResultDto;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.backend.services.CheckResultService;
import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService;
import io.github.sanyavertolet.edukate.backend.entities.CheckResult;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
import io.github.sanyavertolet.edukate.common.checks.CheckResultInfo;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/checker")
@Tag(name = "Checker", description = "API for scheduling checks and retrieving check results")
public class CheckerController {
    private final CheckResultService checkResultService;
    private final CheckerSchedulerService checkerSchedulerService;
    private final SubmissionService submissionService;

    @PostMapping("/ai")
    @Operation(
            summary = "Schedule AI check",
            description = "Schedules an asynchronous AI check for the provided submission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Check scheduled", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "submissionId", description = "Submission identifier", in = QUERY, required = true,
                    schema = @Schema(implementation = String.class))
    })
    public Mono<ResponseEntity<Void>> aiCheck(@RequestParam String submissionId, Authentication authentication) {
        return submissionService.getSubmissionIfOwns(submissionId, AuthUtils.id(authentication))
                .flatMap(checkerSchedulerService::scheduleCheck)
                .thenReturn(ResponseEntity.accepted().build());
    }

    @PostMapping("/self")
    @Operation(
            summary = "Mark submission as self-checked",
            description = "Creates a self-check result for the provided submission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Self-check accepted", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "submissionId", description = "Submission identifier", in = QUERY, required = true,
                    schema = @Schema(implementation = String.class))
    })
    public Mono<ResponseEntity<Void>> selfCheck(@RequestParam String submissionId, Authentication authentication) {
        return submissionService.getSubmissionIfOwns(submissionId, AuthUtils.id(authentication))
                .map(Submission::getId)
                .map(id -> CheckResult.self().submissionId(id).build())
                .flatMap(checkResultService::saveAndUpdateSubmission)
                .map(_ -> ResponseEntity.status(HttpStatus.ACCEPTED).build());
    }

    @SuppressWarnings("unused")
    @PostMapping("/supervisor")
    @Operation(
            summary = "Request supervisor check",
            description = "Requests a supervisor check for the provided submission (not implemented yet)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "501", description = "Not implemented", content = @Content)
    })
    @Parameters({
            @Parameter(name = "submissionId", description = "Submission identifier", in = QUERY, required = true,
                    schema = @Schema(implementation = String.class))
    })
    public Mono<ResponseEntity<Void>> supervisorCheck(
            @RequestParam String submissionId, Authentication authentication
    ) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build());
    }

    @GetMapping("/by-id/{id}")
    @Operation(
            summary = "Get check result by id",
            description = "Retrieves a single check result by its identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved check result",
                    content = @Content(schema = @Schema(implementation = CheckResultDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Check result not found", content = @Content)
    })
    @Parameters({
        @Parameter(name = "id", description = "Check result identifier", in = PATH, required = true,
                schema = @Schema(implementation = String.class))
    })
    public Mono<CheckResultDto> getCheckResultById(@PathVariable String id, Authentication authentication) {
        return checkResultService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Check result not found")))
                .flatMap(result ->
                        submissionService.getSubmissionIfOwns(result.getSubmissionId(), AuthUtils.id(authentication))
                                .thenReturn(result)
                )
                .map(CheckResult::toCheckResultDto);
    }

    @GetMapping("/submissions/{submissionId}")
    @Operation(
            summary = "Get check results for submission",
            description = "Retrieves all check results (lightweight info) for a submission owned by the user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved check results",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CheckResultInfo.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @Parameters({
            @Parameter(name = "submissionId", description = "Submission identifier", in = PATH, required = true,
                    schema = @Schema(implementation = String.class))
    })
    public Flux<CheckResultInfo> getCheckResultsBySubmissionId(
            @PathVariable String submissionId,
            Authentication authentication
    ) {
        return submissionService.getSubmissionIfOwns(submissionId, AuthUtils.id(authentication))
                .map(Submission::getId)
                .flatMapMany(checkResultService::findAllBySubmissionId)
                .map(CheckResult::toCheckResultInfo);
    }
}
