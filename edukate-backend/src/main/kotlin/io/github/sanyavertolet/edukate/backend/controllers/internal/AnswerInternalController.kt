package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.services.AnswerService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Hidden
@RestController
@RequestMapping("/internal/answers")
class AnswerInternalController(private val answerService: AnswerService) {
    @PostMapping fun postAnswer(@RequestBody answer: Answer): Mono<Answer> = answerService.saveAnswer(answer)

    @PostMapping("/batch")
    fun postAnswerBatch(@RequestBody answers: Flux<Answer>): Mono<Long> = answerService.saveAnswerBatch(answers)
}
