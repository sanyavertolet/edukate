package io.github.sanyavertolet.edukate.backend.services.files

import io.github.sanyavertolet.edukate.backend.dtos.CreateSubmissionRequest
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

@Service
class SubmissionFileService(private val fileManager: FileManager) {
    fun moveSubmissionFiles(userId: String, submissionId: String, request: CreateSubmissionRequest): Flux<FileKey> =
        request.fileNames.toFlux().flatMapSequential { file ->
            fileManager.moveFile(TempFileKey(userId, file), SubmissionFileKey(userId, request.problemId, submissionId, file))
        }
}
