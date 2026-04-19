package io.github.sanyavertolet.edukate.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("problems")
data class Problem(
    @Id val id: Long? = null,
    val bookId: Long,
    val code: String,
    val key: String = "",
    val isHard: Boolean = false,
    val tags: List<String> = emptyList(),
    val text: String,
    val subtasks: List<Subtask> = emptyList(),
    val images: List<String> = emptyList(),
) {
    data class Subtask(val id: String, val text: String)

    enum class Status {
        SOLVED,
        SOLVING,
        FAILED,
        NOT_SOLVED,
    }
}
