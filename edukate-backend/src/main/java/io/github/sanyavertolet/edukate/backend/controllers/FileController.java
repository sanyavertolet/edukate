package io.github.sanyavertolet.edukate.backend.controllers;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.services.FileService;
import io.github.sanyavertolet.edukate.backend.storage.FileKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Files", description = "API for managing files and temporary files")
public class FileController {
    private final FileService fileService;

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/get")
    @Operation(
            summary = "Get file content",
            description = "Retrieves the content of a file by its key and optional key prefix"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved file content"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires MODERATOR role", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "key", description = "File key", required = true),
            @Parameter(name = "keyPrefix", description = "Optional key prefix")
    })
    public Flux<ByteBuffer> getFile(@RequestParam String key, @RequestParam(required = false) String keyPrefix) {
        return Mono.fromCallable(() -> FileKeys.prefixed(keyPrefix, key))
                .flatMapMany(fileService::getFile)
                .onErrorResume(Exception.class, e ->
                        Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e))
                );
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping("/exists")
    @Operation(
            summary = "Check if file exists",
            description = "Checks if a file with the given key exists in the storage"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked file existence",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires MODERATOR role", content = @Content)
    })
    @Parameters({
            @Parameter(name = "key", description = "File key to check", required = true)
    })
    public Mono<Boolean> doesFileExist(@RequestParam String key) {
        return fileService.doesFileExist(key);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/temp")
    @Operation(
            summary = "Upload temporary file",
            description = "Uploads a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded temporary file",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to upload file", content = @Content)
    })
    @Parameters({
            @Parameter(name = "content", description = "File content to upload", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<String> uploadTempFile(
            @RequestPart Flux<ByteBuffer> content, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return Mono.fromCallable(UUID::randomUUID)
                .flatMap(uuid ->
                        fileService.uploadFile(FileKeys.temp(authentication.getName(), uuid.toString()), content)
                                .thenReturn(uuid.toString()));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/temp")
    @Operation(
            summary = "Delete temporary file",
            description = "Deletes a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted temporary file",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to delete file", content = @Content)
    })
    @Parameters({
            @Parameter(name = "key", description = "File key to delete", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Mono<String> deleteTempFile(
            @RequestParam String key, 
            @Parameter(hidden = true) Authentication authentication
    ) {
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
    @GetMapping("/temp/get")
    @Operation(
            summary = "Download temporary file",
            description = "Downloads a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully downloaded temporary file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "fileName", description = "File name to download", required = true),
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Flux<ByteBuffer> downloadTempFile(
            @RequestParam String fileName, 
            @Parameter(hidden = true) Authentication authentication
    ) {
        return Mono.just(fileName)
                .map(filename -> FileKeys.temp(authentication.getName(), filename))
                .flatMapMany(fileService::getFile);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/temp")
    @Operation(
            summary = "Get temporary files",
            description = "Retrieves a list of temporary files for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved temporary files",
                    content = @Content(schema = @Schema(implementation = FileMetadata.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires USER role", content = @Content)
    })
    @Parameters({
            @Parameter(name = "authentication", description = "Spring authentication", hidden = true)
    })
    public Flux<FileMetadata> getTempFiles(@Parameter(hidden = true) Authentication authentication) {
        return Mono.fromCallable(authentication::getName)
                .flatMap(userName -> Mono.zip(
                        Mono.just(userName),
                        Mono.just(FileKeys.tempDir(userName))
                ))
                .flatMapMany(tuple -> fileService.listFileMetadataWithPrefix(tuple.getT2(), tuple.getT1()))
                .flatMap(fileService::updateKeyInFileMetadata);
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PostMapping("/upload")
    @Operation(
            summary = "Upload file",
            description = "Uploads a file with the specified key (requires MODERATOR role)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded file",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires MODERATOR role", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to upload file", content = @Content)
    })
    @Parameters({
            @Parameter(name = "key", description = "File key to use for storage", required = true),
            @Parameter(name = "content", description = "File content to upload", required = true)
    })
    public Mono<String> uploadFile(@RequestParam String key, @RequestBody Flux<ByteBuffer> content) {
        return fileService.uploadFile(key, content);
    }
}
