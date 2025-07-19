package io.github.sanyavertolet.edukate.backend.controllers.internal;

import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

@Hidden
@RestController
@RequestMapping("/internal/files")
@RequiredArgsConstructor
public class FileInternalController {
    private final FileService fileService;

    @PostMapping("/upload/{key}")
    private Mono<String> uploadFile(@PathVariable String key, @RequestBody Flux<ByteBuffer> content) {
        return fileService.uploadFile(key, content);
    }

    @GetMapping("/get/{key}")
    public Flux<ByteBuffer> getFile(@PathVariable String key) {
        return fileService.getFile(key);
    }

    @DeleteMapping("/delete/{key}")
    public Mono<Boolean> deleteFile(@PathVariable String key) {
        return fileService.deleteFile(key);
    }

    @GetMapping("/list")
    public Flux<String> listFiles() {
        return fileService.listFiles();
    }
}
