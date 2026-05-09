package io.github.sanyavertolet.edukate.backend.dtos.ingestion

import io.github.sanyavertolet.edukate.backend.entities.Subproblem
import io.github.sanyavertolet.edukate.common.ContentLanguage

data class BookIngestionRequest(val book: BookData, val problems: List<ProblemIngestionEntry>)

data class BookData(
    val slug: String,
    val subject: String,
    val title: String,
    val citation: String,
    val description: String? = null,
)

data class ProblemIngestionEntry(
    val code: String,
    val isHard: Boolean = false,
    val images: List<String> = emptyList(),
    val answer: AnswerIngestionEntry? = null,
    val localizations: Map<ContentLanguage, ProblemLocalizationData>,
)

data class ProblemLocalizationData(
    val text: String,
    val tags: List<String> = emptyList(),
    val subproblems: List<Subproblem> = emptyList(),
)

data class AnswerIngestionEntry(
    val images: List<String> = emptyList(),
    val localizations: Map<ContentLanguage, AnswerLocalizationData>,
)

data class AnswerLocalizationData(val text: String, val notes: String? = null)
