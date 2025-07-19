package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.dtos.Result;
import io.github.sanyavertolet.edukate.backend.services.ResultService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Hidden
@RequiredArgsConstructor
@RequestMapping("/internal/results")
@RestController
public class ResultInternalController {
    private final ResultService resultService;

    @PostMapping
    public Mono<String> postResult(@RequestBody Result result) {
        return resultService.updateResult(result);
    }

    @PostMapping("/batch")
    public Flux<String> postResultsBatch(@RequestBody Flux<Result> results) {
        return resultService.updateResultBatch(results);
    }
}
