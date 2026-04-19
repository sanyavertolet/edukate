package io.github.sanyavertolet.edukate.backend.controllers.internal

import io.github.sanyavertolet.edukate.backend.services.CheckerSchedulerService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Hidden
@RestController
@RequestMapping("/internal/checker")
class CheckerInternalController(
    private val submissionService: SubmissionService,
    private val checkerSchedulerService: CheckerSchedulerService,
) {
    @PostMapping("/ai")
    fun checkSubmission(@RequestParam("id") submissionId: Long): Mono<Void> =
        submissionService.findById(submissionId).flatMap { checkerSchedulerService.scheduleCheck(it) }
}
