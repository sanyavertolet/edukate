package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.backend.entities.Problem

data class ProblemDto(
    val id: String,
    val isHard: Boolean,
    val tags: List<String>,
    val text: String,
    val subtasks: List<Problem.Subtask>,
    val images: List<String>,
    val status: Problem.Status,
    val hasResult: Boolean,
)
