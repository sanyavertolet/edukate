package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.dtos.SupervisorTicketDto
import io.github.sanyavertolet.edukate.backend.entities.SupervisorTicket
import io.github.sanyavertolet.edukate.backend.repositories.FileObjectRepository
import io.github.sanyavertolet.edukate.backend.services.ProblemService
import io.github.sanyavertolet.edukate.backend.services.ProblemSetService
import io.github.sanyavertolet.edukate.backend.services.SubmissionService
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@Component
class SupervisorTicketMapper(
    private val submissionService: SubmissionService,
    private val problemService: ProblemService,
    private val problemSetService: ProblemSetService,
    private val fileObjectRepository: FileObjectRepository,
    private val fileManager: FileManager,
) {
    fun toDto(ticket: SupervisorTicket): Mono<SupervisorTicketDto> =
        submissionService.findById(ticket.submissionId).flatMap { submission ->
            Mono.zip(
                    problemService.findProblemById(submission.problemId).map { it.key },
                    problemSetService.findById(ticket.problemSetId).map { it.shareCode },
                    collectFileUrls(submission.fileObjectIds),
                )
                .map { (problemKey, shareCode, fileUrls) ->
                    SupervisorTicketDto(
                        id = requireNotNull(ticket.id),
                        problemKey = problemKey,
                        problemSetShareCode = shareCode,
                        fileUrls = fileUrls,
                        status = ticket.status,
                        createdAt = requireNotNull(ticket.createdAt),
                    )
                }
        }

    private fun collectFileUrls(fileObjectIds: List<String>): Mono<List<String>> {
        if (fileObjectIds.isEmpty()) return Mono.just(emptyList())
        return fileObjectRepository
            .findAllById(fileObjectIds.mapNotNull { it.toLongOrNull() })
            .map { it.key }
            .flatMapSequential { fileManager.getPresignedUrl(it) }
            .collectList()
    }
}
