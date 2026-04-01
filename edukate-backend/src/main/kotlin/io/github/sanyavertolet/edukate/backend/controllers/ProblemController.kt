package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Validated
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems", description = "API for managing and retrieving problems")
class ProblemController(private val problemService: ProblemService) {
    @GetMapping
    @Operation(summary = "Get problem list", description = "Retrieves a paginated list of problem metadata")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved problem list",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = ProblemMetadata::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "page",
                    description = "Page number (zero-based)",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "0"),
                ),
                Parameter(
                    name = "size",
                    description = "Number of problems per page",
                    `in` = ParameterIn.QUERY,
                    schema = Schema(minimum = "1", maximum = "100"),
                ),
            ]
    )
    fun getProblemList(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        authentication: Authentication?,
    ): Flux<ProblemMetadata> =
        problemService.getFilteredProblems(PageRequest.of(page, size)).flatMapSequential { problem ->
            problemService.prepareMetadata(problem, authentication)
        }

    @GetMapping("/count")
    @Operation(summary = "Count problems", description = "Returns the total number of problems in the system")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved problem count",
                    content = [Content(schema = Schema(implementation = Long::class))],
                )
            ]
    )
    fun count(): Mono<Long> = problemService.countProblems()

    @GetMapping("/by-prefix")
    @Operation(
        summary = "Get problem IDs by prefix",
        description = "Retrieves a list of problem IDs that match the given prefix",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved problem IDs",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "prefix",
                    description = "The prefix to match problem IDs against",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
                Parameter(name = "limit", description = "Maximum number of results to return", `in` = ParameterIn.QUERY),
            ]
    )
    fun getProblemIdsByPrefix(
        @RequestParam @NotBlank prefix: String,
        @RequestParam(required = false, defaultValue = "5") @Positive limit: Int,
    ): Mono<List<String>> = problemService.getProblemIdsByPrefix(prefix, limit).collectList()

    @GetMapping("/{id}")
    @Operation(summary = "Get problem by ID", description = "Retrieves a specific problem by its ID")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved problem",
                    content = [Content(schema = Schema(implementation = ProblemDto::class))],
                ),
                ApiResponse(responseCode = "400", description = "Validation failed", content = [Content()]),
                ApiResponse(responseCode = "404", description = "Problem not found", content = [Content()]),
            ]
    )
    @Parameters(value = [Parameter(name = "id", description = "Problem ID", `in` = ParameterIn.PATH, required = true)])
    fun getProblem(@PathVariable @NotBlank id: String, authentication: Authentication?): Mono<ProblemDto> =
        problemService
            .findProblemById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found")))
            .flatMap { problem -> problemService.prepareDto(problem, authentication) }

    @Suppress("MaxLineLength")
    @GetMapping("/random")
    @Operation(
        summary = "Get random problem ID",
        description =
            "Returns a random problem ID. If the user is authenticated, prioritizes problems the user hasn't solved yet; otherwise returns any random problem.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved random problem ID",
                    content = [Content(mediaType = "text/plain", schema = Schema(implementation = String::class))],
                )
            ]
    )
    fun getRandomUnsolvedProblemId(authentication: Authentication?): Mono<String> =
        problemService.getRandomUnsolvedProblemId(authentication)
}
