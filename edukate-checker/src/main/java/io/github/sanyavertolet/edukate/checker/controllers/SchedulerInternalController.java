package io.github.sanyavertolet.edukate.checker.controllers;

import io.github.sanyavertolet.edukate.checker.services.CheckerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/internal/schedule")
public class SchedulerInternalController {
    private final CheckerService checkerService;

    @PostMapping("/ai")
    public Mono<Void> aiCheck(@RequestParam(name = "id") String submissionId) {
        return checkerService.runCheck(submissionId);
    }
}
