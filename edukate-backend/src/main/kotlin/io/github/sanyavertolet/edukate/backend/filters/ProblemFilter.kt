package io.github.sanyavertolet.edukate.backend.filters

import io.github.sanyavertolet.edukate.backend.entities.Problem

@Suppress("DataClassContainsFunctions")
data class ProblemFilter(
    val prefix: String? = null,
    val status: Problem.Status? = null,
    val isHard: Boolean? = null,
    val hasPictures: Boolean? = null,
    val hasResult: Boolean? = null,
) {
    fun requiresAggregation(): Boolean = status != null || isHard != null || hasPictures != null || hasResult != null
}
