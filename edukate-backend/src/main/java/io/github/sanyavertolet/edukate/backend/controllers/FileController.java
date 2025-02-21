package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/get/{key}")
    public Flux<ByteBuffer> getFile(@PathVariable String key) {
        return fileService.getFile(key)
                .onErrorResume(Exception.class, e ->
                        Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e))
                );
    }

    @GetMapping("/exists/{key}")
    public Mono<Boolean> doesFileExist(@PathVariable String key) {
        return fileService.doesFileExist(key);
    }
}
