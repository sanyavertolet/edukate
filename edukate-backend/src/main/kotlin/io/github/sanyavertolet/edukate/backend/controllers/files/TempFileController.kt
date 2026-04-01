package io.github.sanyavertolet.edukate.backend.controllers.files

import io.github.sanyavertolet.edukate.backend.dtos.FileMetadata
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.utils.id
import io.github.sanyavertolet.edukate.common.utils.monoId
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.util.UUID

@RestController
@RequestMapping("/api/v1/files/temp")
@Validated
@Tag(name = "Temporary Files", description = "API for managing temporary files")
class TempFileController(private val fileManager: FileManager) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    @Operation(summary = "Upload temporary file", description = "Uploads a temporary file for the authenticated user")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully uploaded temporary file",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(responseCode = "500", description = "Failed to uploadDeprecated file", content = [Content()]),
            ]
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content =
            [
                Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = Schema(type = "string", format = "binary"),
                )
            ],
    )
    fun uploadTempFile(@RequestPart("content") content: Flux<ByteBuffer>, authentication: Authentication): Mono<String> {
        val contentType = defaultMediaType()
        val requesterId = requireNotNull(authentication.id())
        return Mono.fromCallable { TempFileKey(requesterId, UUID.randomUUID().toString()) }
            .flatMap { fileKey -> fileManager.uploadFile(fileKey, contentType, content) }
            .map { it.fileName }
    }

    // todo: detect content type???
    private fun defaultMediaType(): MediaType = MediaType.IMAGE_JPEG

    @DeleteMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
    @Operation(summary = "Delete temporary file", description = "Deletes a temporary file for the authenticated user")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully deleted temporary file",
                    content = [Content(schema = Schema(implementation = String::class))],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(responseCode = "404", description = "File not found", content = [Content()]),
                ApiResponse(responseCode = "500", description = "Failed to delete file", content = [Content()]),
            ]
    )
    @Parameters(
        value =
            [
                Parameter(
                    name = "fileName",
                    description = "Name of a file to delete",
                    `in` = ParameterIn.QUERY,
                    required = true,
                )
            ]
    )
    fun deleteTempFile(@RequestParam @NotBlank fileName: String, authentication: Authentication): Mono<String> =
        Mono.fromCallable { TempFileKey(requireNotNull(authentication.id()), fileName) }
            .flatMap { fileManager.deleteFile(it) }
            .flatMap { success ->
                if (success) Mono.just(fileName)
                else Mono.error(ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file"))
            }

    @GetMapping("/get", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @Operation(summary = "Download temporary file", description = "Downloads a temporary file for the authenticated user")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully downloaded temporary file",
                    content =
                        [
                            Content(
                                mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                schema = Schema(type = "string", format = "binary"),
                            )
                        ],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
                ApiResponse(responseCode = "404", description = "File not found", content = [Content()]),
            ]
    )
    @Parameters(
        value =
            [Parameter(name = "fileName", description = "File name to download", `in` = ParameterIn.QUERY, required = true)]
    )
    fun downloadTempFile(@RequestParam @NotBlank fileName: String, authentication: Authentication): Flux<ByteBuffer> =
        Mono.fromCallable { TempFileKey(requireNotNull(authentication.id()), fileName) }
            .flatMapMany { fileManager.getFileContent(it) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")))

    @GetMapping
    @Operation(
        summary = "Get temporary files",
        description = "Retrieves a list of temporary files for the authenticated user",
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved temporary files",
                    content = [Content(array = ArraySchema(schema = Schema(implementation = FileMetadata::class)))],
                ),
                ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content()]),
            ]
    )
    fun getTempFiles(authentication: Authentication): Flux<FileMetadata> =
        authentication
            .monoId()
            .map { TempFileKey.prefix(it) }
            .flatMapMany { prefix -> fileManager.listFileMetadataWithPrefix(prefix, authentication.name) }
}
