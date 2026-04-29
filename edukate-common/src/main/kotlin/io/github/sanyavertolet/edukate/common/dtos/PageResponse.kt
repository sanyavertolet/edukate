package io.github.sanyavertolet.edukate.common.dtos

data class PageResponse<T>(val content: List<T>, val page: Int, val size: Int, val totalElements: Long, val totalPages: Int)
