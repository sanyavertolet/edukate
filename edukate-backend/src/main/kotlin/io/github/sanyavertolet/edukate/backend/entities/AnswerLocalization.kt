package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.relational.core.mapping.Table

@Table("answer_localizations")
data class AnswerLocalization(
    val answerId: Long,
    val language: ContentLanguage,
    val text: String,
    val notes: String? = null,
)
