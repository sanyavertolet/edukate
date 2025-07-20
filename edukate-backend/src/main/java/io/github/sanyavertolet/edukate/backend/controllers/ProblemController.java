package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto;
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata;
import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import io.github.sanyavertolet.edukate.backend.services.SubmissionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "API for managing and retrieving problems")
public class ProblemController {
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    @GetMapping
    @Operation(
            summary = "Get problem list",
            description = "Retrieves a paginated list of problem metadata"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved problem list",
                    content = @Content(schema = @Schema(implementation = ProblemMetadata.class))),
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number (zero-based)", in = QUERY),
            @Parameter(name = "size", description = "Number of problems per page", in = QUERY),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Flux<ProblemMetadata> getProblemList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return Mono.just(PageRequest.of(page, size))
                .flatMapMany(problemService::getFilteredProblems)
                .collectList()
                .map(problems -> problems.stream().map(Problem::toProblemMetadata).toList())
                .flatMapMany(problemMetadataList ->
                        submissionService.updateStatusInMetadataMany(authentication, problemMetadataList)
                );
    }

    @GetMapping("/count")
    @Operation(
            summary = "Count problems",
            description = "Returns the total number of problems in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved problem count",
                    content = @Content(schema = @Schema(implementation = Long.class)))
    })
    public Mono<Long> count() {
        return problemService.countProblems();
    }

    @GetMapping("/by-prefix")
    @Operation(
            summary = "Get problem IDs by prefix",
            description = "Retrieves a list of problem IDs that match the given prefix"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved problem IDs",
                    content = @Content(schema = @Schema(implementation = String.class)))
    })
    @Parameters({
            @Parameter(name = "prefix", description = "The prefix to match problem IDs against", in = QUERY,
                    required = true),
            @Parameter(name = "limit", description = "Maximum number of results to return", in = QUERY)
    })
    public Mono<List<String>> getProblemIdsByPrefix(
            @RequestParam String prefix, 
            @RequestParam(required = false, defaultValue = "5") int limit
    ) {
        return problemService.getProblemIdsByPrefix(prefix, limit).collectList();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get problem by ID",
            description = "Retrieves a specific problem by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved problem",
                    content = @Content(schema = @Schema(implementation = ProblemDto.class))),
            @ApiResponse(responseCode = "404", description = "Problem not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "id", description = "Problem ID", in = PATH, required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<ProblemDto> getProblem(
            @PathVariable String id,
            Authentication authentication
    ) {
        return problemService.findProblemById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found")))
                .map(Problem::toProblemDto)
                .flatMap(problemService::updateImagesInDto)
                .flatMap(problemDto -> submissionService.updateStatusInDto(authentication, problemDto));
    }
}
