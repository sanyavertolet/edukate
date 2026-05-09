package io.github.sanyavertolet.edukate.backend.entities

import io.github.sanyavertolet.edukate.common.ContentLanguage
import org.springframework.data.relational.core.mapping.Table

@Table("problem_localizations")
data class ProblemLocalization(
    val problemId: Long,
    val language: ContentLanguage,
    val text: String,
    val tags: List<String> = emptyList(),
    val subproblems: List<Subproblem> = emptyList(),
)
