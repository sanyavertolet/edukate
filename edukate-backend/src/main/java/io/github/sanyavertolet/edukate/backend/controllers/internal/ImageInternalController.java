package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.services.ProblemService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.ByteBuffer;

@RestController
@RequestMapping("/internal/images")
@AllArgsConstructor
public class ImageInternalController {
    private final FileService fileService;
    private final ProblemService problemService;

    @PostMapping("/upload/{problemId}")
    public Mono<String> uploadImage(@PathVariable String problemId, @RequestParam String fileKey, @RequestBody Flux<ByteBuffer> content) {
        return problemService.findProblemById(problemId)
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id " + problemId + " not found"))
                )
                .doOnNext(_ ->
                        fileService.uploadFile(fileKey, content).subscribeOn(Schedulers.boundedElastic()).subscribe()
                )
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not upload file"))
                )
                .flatMap(problem -> {
                    problem.addImageIfNotPresent(fileKey);
                    return problemService.updateProblem(problem);
                })
                .map(_ -> "Successfully uploaded \"" + fileKey + "\" and linked to problem " + problemId);
    }

    @PutMapping("/link/{problemId}")
    public Mono<String> addImageKey(@PathVariable String problemId, @RequestParam String fileKey) {
        return problemService.findProblemById(problemId)
                .switchIfEmpty(
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id " + problemId + " not found"))
                )
                .flatMap(problem -> {
                    problem.addImageIfNotPresent(fileKey);
                    return problemService.updateProblem(problem);
                })
                .map(_ -> "Successfully added [" + fileKey + "] to problem " + problemId);
    }
}
