package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.ContentLanguage

data class AnswerDto(val text: String, val notes: String?, val images: List<String>, val language: ContentLanguage)
