package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.Subproblem
import io.github.sanyavertolet.edukate.common.ContentLanguage

data class ProblemDto(
    val key: String,
    val code: String,
    val bookSlug: String,
    val isHard: Boolean,
    val tags: List<String>,
    val text: String,
    val subproblems: List<Subproblem>,
    val images: List<String>,
    val status: Problem.Status,
    val hasResult: Boolean,
    val language: ContentLanguage,
)
