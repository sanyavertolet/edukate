package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.entities.Problem;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Hidden
@RestController
@RequestMapping("/internal/problems")
@RequiredArgsConstructor
public class ProblemInternalController {
    private final ProblemService problemService;

    @PostMapping
    public Mono<Problem> postProblem(@RequestBody Problem problem) {
        return problemService.updateProblem(problem);
    }

    @PostMapping("/batch")
    public Flux<String> postProblemBatch(@RequestBody Flux<Problem> problems) {
        return problemService.updateProblemBatch(problems).map(Problem::getId);
    }

    @DeleteMapping("/{id}")
    public Mono<Boolean> deleteProblem(@PathVariable String id) {
        return problemService.deleteProblemById(id);
    }

    @PatchMapping("/missing-internal-indices")
    public Mono<Long> updateMissingInternalIndices() {
        return problemService.updateMissingInternalIndices();
    }
}
