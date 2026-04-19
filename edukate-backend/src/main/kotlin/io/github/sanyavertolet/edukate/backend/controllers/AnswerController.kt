package io.github.sanyavertolet.edukate.backend.controllers

import io.github.sanyavertolet.edukate.backend.dtos.AnswerDto
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.github.sanyavertolet.edukate.common.utils.orNotFound
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Validated
@SecurityRequirements
@RequestMapping("/api/v1/answers")
@Tag(name = "Answers", description = "API for retrieving problem answers")
class AnswerController(private val answerService: AnswerService) {
    @GetMapping("/{bookSlug}/{code}")
    @Operation(summary = "Get answer by problem key", description = "Retrieves an answer for a specific problem")
    @ApiResponses(
        value =
            [
                ApiResponse(responseCode = "200", description = "Successfully retrieved answer"),
                ApiResponse(responseCode = "404", description = "Answer not found"),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(name = "bookSlug", description = "Book slug", `in` = ParameterIn.PATH, required = true),
                Parameter(name = "code", description = "Problem code", `in` = ParameterIn.PATH, required = true),
            ]
    )
    fun getAnswerByProblemKey(@PathVariable bookSlug: String, @PathVariable code: String): Mono<AnswerDto> =
        "$bookSlug/$code".let { key -> answerService.findByProblemKey(key).orNotFound("Answer for problem $key not found") }
}
