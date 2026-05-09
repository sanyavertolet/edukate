package io.github.sanyavertolet.edukate.backend.entities

import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("problems")
data class Problem(
    @Id val id: Long? = null,
    val bookId: Long,
    val code: String,
    val key: String = "",
    val isHard: Boolean = false,
    val images: List<String> = emptyList(),
    val createdAt: Instant = Instant.now(),
) {
    enum class Status {
        SOLVED,
        SOLVING,
        FAILED,
        NOT_SOLVED,
    }
}
