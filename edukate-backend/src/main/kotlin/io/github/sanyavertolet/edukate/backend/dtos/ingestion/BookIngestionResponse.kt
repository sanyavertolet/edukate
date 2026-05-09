package io.github.sanyavertolet.edukate.backend.dtos.ingestion

data class BookIngestionResponse(
    val bookSlug: String,
    val problemCount: Int,
    val answerCount: Int,
    val problemLocalizationCount: Int,
    val answerLocalizationCount: Int,
)
