package io.github.sanyavertolet.edukate.backend.controllers.files;

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata;
import io.github.sanyavertolet.edukate.backend.services.files.TempFileService;
import io.github.sanyavertolet.edukate.common.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;

@RestController
@RequestMapping("/api/v1/files/temp")
@RequiredArgsConstructor
@Validated
@Tag(name = "Temporary Files", description = "API for managing temporary files")
public class TempFileController {
    private final TempFileService tempFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            summary = "Upload temporary file",
            description = "Uploads a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded temporary file",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to upload file", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    public Mono<String> uploadTempFile(@RequestPart Flux<ByteBuffer> content, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMap(userId -> Mono.fromCallable(UUID::randomUUID).map(UUID::toString).flatMap(uuid ->
                        tempFileService.uploadFile(userId, uuid, content).thenReturn(uuid)
                ));
    }

    @DeleteMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            summary = "Delete temporary file",
            description = "Deletes a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted temporary file",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to delete file", content = @Content)
    })
    @Parameters({
            @Parameter(name = "fileName", description = "Name of a file to delete", in = QUERY, required = true),
    })
    public Mono<String> deleteTempFile(@RequestParam @NotBlank String fileName, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .filterWhen(userId -> tempFileService.doesExist(userId, fileName))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")))
                .flatMap(userId -> tempFileService.deleteFile(userId, fileName))
                .flatMap(success -> success
                        ? Mono.just(fileName)
                        : Mono.error(
                                new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file")
                        )
                );
    }

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
            summary = "Download temporary file",
            description = "Downloads a temporary file for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully downloaded temporary file",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "File not found", content = @Content)
    })
    @Parameters({
            @Parameter(name = "fileName", description = "File name to download", in = QUERY, required = true),
    })
    public Flux<ByteBuffer> downloadTempFile(@RequestParam @NotBlank String fileName, Authentication authentication) {
        return AuthUtils.monoId(authentication)
                .flatMapMany(userId -> tempFileService.getFile(userId, fileName))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")));
    }

    @GetMapping
    @Operation(
            summary = "Get temporary files",
            description = "Retrieves a list of temporary files for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved temporary files",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = FileMetadata.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    public Flux<FileMetadata> getTempFiles(Authentication authentication) {
        return AuthUtils.monoId(authentication).flatMapMany(tempFileService::listFileMetadata);
    }
}
