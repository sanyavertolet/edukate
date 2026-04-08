package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.Result
import io.github.sanyavertolet.edukate.backend.services.ResultService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/results")
@Validated
@Tag(name = "Results", description = "API for retrieving problem results")
class ResultController(private val resultService: ResultService) {
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get result by ID", description = "Retrieves a specific result by its ID")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved result"),
                ApiResponse(responseCode = "404", description = "Result not found"),
            ]
    )
    @Parameters(value = [Parameter(name = "id", description = "Result ID", `in` = ParameterIn.PATH, required = true)])
    fun getResultById(@PathVariable @NotBlank id: String): Mono<Result> =
        resultService
            .findResultById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found")))
}
