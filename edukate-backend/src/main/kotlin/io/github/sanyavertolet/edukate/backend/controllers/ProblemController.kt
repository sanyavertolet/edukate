package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.filters.ProblemFilter
import io.github.sanyavertolet.edukate.backend.mappers.ProblemMapper
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Validated
@SecurityRequirements
@RequestMapping("/api/v1/problems")
@Tag(name = "Problems", description = "API for managing and retrieving problems")
class ProblemController(private val problemService: ProblemService, private val problemMapper: ProblemMapper) {
    @GetMapping
    @Operation(summary = "Get problem list", description = "Retrieves a paginated list of problem metadata")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem list"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(
                    responseCode = "401",
                    description = "Authentication required when filtering by status (other than NOT_SOLVED)",
                ),
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
    @Suppress("LongParameterList")
    fun getProblemList(
        @RequestParam(defaultValue = "0") @PositiveOrZero page: Int,
        @RequestParam(defaultValue = "10") @Positive size: Int,
        @RequestParam(required = false) bookSlug: String?,
        @RequestParam(required = false) prefix: String?,
        @RequestParam(required = false) status: Problem.Status?,
        @RequestParam(required = false) isHard: Boolean?,
        @RequestParam(required = false) hasPictures: Boolean?,
        @RequestParam(required = false) hasResult: Boolean?,
        authentication: Authentication?,
    ): Flux<ProblemMetadata> =
        problemService
            .getFilteredProblems(
                ProblemFilter(bookSlug, prefix, status, isHard, hasPictures, hasResult),
                authentication,
                PageRequest.of(page, size),
            )
            .flatMapSequential { problem -> problemMapper.toMetadata(problem, authentication) }

    @GetMapping("/count")
    @Operation(summary = "Count problems", description = "Returns the total number of problems matching the filter")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem count"),
                ApiResponse(
                    responseCode = "401",
                    description = "Authentication required when filtering by status (other than NOT_SOLVED)",
                ),
            ]
    )
    @Suppress("LongParameterList")
    fun count(
        @RequestParam(required = false) bookSlug: String?,
        @RequestParam(required = false) prefix: String?,
        @RequestParam(required = false) status: Problem.Status?,
        @RequestParam(required = false) isHard: Boolean?,
        @RequestParam(required = false) hasPictures: Boolean?,
        @RequestParam(required = false) hasResult: Boolean?,
        authentication: Authentication?,
    ): Mono<Long> =
        problemService.countFilteredProblems(
            ProblemFilter(bookSlug, prefix, status, isHard, hasPictures, hasResult),
            authentication,
        )

    @GetMapping("/by-prefix")
    @Operation(
        summary = "Get problem codes by prefix",
        description = "Retrieves a list of problem codes that match the given prefix",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem codes"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "prefix",
                    description = "The prefix to match problem codes against",
                    `in` = ParameterIn.QUERY,
                    required = true,
                ),
                Parameter(name = "limit", description = "Maximum number of results to return", `in` = ParameterIn.QUERY),
            ]
    )
    fun getProblemCodesByPrefix(
        @RequestParam @NotBlank prefix: String,
        @RequestParam(required = false, defaultValue = "5") @Positive limit: Int,
    ): Mono<List<String>> = problemService.getProblemCodesByPrefix(prefix, limit).collectList()

    @GetMapping("/{bookSlug}/{code}")
    @Operation(summary = "Get problem by key", description = "Retrieves a specific problem by its book slug and code")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved problem"),
                ApiResponse(responseCode = "400", description = "Validation failed"),
                ApiResponse(responseCode = "404", description = "Problem not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "bookSlug", description = "Book slug", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "code", description = "Problem code", `in` = ParameterIn.PATH, required = true),
            ]
    )
    fun getProblem(
        @PathVariable bookSlug: String,
        @PathVariable code: String,
        authentication: Authentication?,
    ): Mono<ProblemDto> =
        problemService.findProblemByKey("$bookSlug/$code").orNotFound("Problem not found").flatMap { problem ->
            problemMapper.toDto(problem, authentication)
        }

    @Suppress("MaxLineLength")
    @GetMapping("/random")
    @Operation(
        summary = "Get random problem key",
        description =
            "Returns a random problem key (bookSlug/code). If the user is authenticated, prioritizes problems the user hasn't solved yet; otherwise returns any random problem.",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved random problem key",
                    content = [Content(mediaType = "text/plain", schema = Schema(implementation = String::class))],
                )
            ]
    )
    fun getRandomUnsolvedProblemKey(authentication: Authentication?): Mono<String> =
        problemService.getRandomUnsolvedProblemKey(authentication)
}
