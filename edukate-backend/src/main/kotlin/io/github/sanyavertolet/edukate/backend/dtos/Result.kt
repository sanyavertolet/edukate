package io.github.sanyavertolet.edukate.backend.dtos

data class Result(
    val id: String,
    val text: String,
    val notes: String,
    val type: ResultType,
    val images: List<String>,
) {
    fun withImages(images: List<String>): Result = copy(images = images)

    enum class ResultType {
        FORMULA,
        TEXT,
        NUMERIC,
    }
}
