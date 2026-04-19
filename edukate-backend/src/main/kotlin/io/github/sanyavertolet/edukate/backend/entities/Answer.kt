package io.github.sanyavertolet.edukate.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("answers")
data class Answer(
    @Id val id: Long? = null,
    val problemId: Long,
    val text: String,
    val notes: String? = null,
    val images: List<String> = emptyList(),
)
