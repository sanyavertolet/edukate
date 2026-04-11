package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.backend.dtos.ProblemDto
import io.github.sanyavertolet.edukate.backend.dtos.ProblemMetadata
import io.github.sanyavertolet.edukate.backend.dtos.Result
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("problems")
data class Problem(
    @field:Id val id: String,
    val isHard: Boolean,
    val tags: List<String>,
    val text: String,
    val subtasks: List<Subtask>,
    val images: List<String>,
    val result: Result? = null,
) {
    fun toProblemMetadata(status: Status): ProblemMetadata = ProblemMetadata(id, isHard, tags, status)

    fun toProblemDto(status: Status, images: List<String>): ProblemDto =
        ProblemDto(id, isHard, tags, text, subtasks, images, status, result != null)

    data class Subtask(val id: String, val text: String)

    enum class Status {
        SOLVED,
        SOLVING,
        FAILED,
        NOT_SOLVED,
    }
}
