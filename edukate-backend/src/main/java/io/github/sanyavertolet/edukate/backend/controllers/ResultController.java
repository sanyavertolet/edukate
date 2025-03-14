package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.Result;
import io.github.sanyavertolet.edukate.backend.services.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/api/v1/results")
@RestController
public class ResultController {
    private final ResultService resultService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Mono<Result> getResultById(@PathVariable String id) {
        return resultService.findResultById(id);
    }
}
