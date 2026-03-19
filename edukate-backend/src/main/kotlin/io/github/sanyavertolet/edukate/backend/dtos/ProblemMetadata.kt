package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.backend.entities.Problem

data class ProblemMetadata(val name: String, val isHard: Boolean, val tags: List<String>, val status: Problem.Status)
