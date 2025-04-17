package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.storage.FileKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/get/{key}")
    public Flux<ByteBuffer> getFile(@PathVariable String key, @RequestParam(required = false) String keyPrefix) {
        return Mono.fromCallable(() -> FileKeys.prefixed(keyPrefix, key))
                .flatMapMany(fileService::getFile)
                .onErrorResume(Exception.class, e ->
                        Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e))
                );
    }

    @GetMapping("/exists/{key}")
    public Mono<Boolean> doesFileExist(@PathVariable String key) {
        return fileService.doesFileExist(key);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/temp")
    public Mono<String> uploadTempFile(@RequestBody Flux<ByteBuffer> content, Authentication authentication) {
        return Mono.fromCallable(UUID::randomUUID)
                .flatMap(uuid ->
                        fileService.uploadFile(FileKeys.temp(authentication.getName(), uuid.toString()), content)
                                .thenReturn(uuid.toString()));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/temp")
    public Mono<String> deleteTempFile(@RequestParam String key, Authentication authentication) {
        return Mono.fromCallable(authentication::getName)
                .map(userName -> FileKeys.temp(userName, key))
                .filterWhen(fileService::doesFileExist)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")))
                .flatMap(fileService::deleteFile)
                .filter(isOk -> isOk)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete temp file")))
                .map(_ -> key);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/upload")
    public Mono<String> uploadFile(@RequestParam String key, @RequestBody Flux<ByteBuffer> content) {
        return fileService.uploadFile(key, content);
    }
}
