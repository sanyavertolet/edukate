package io.github.sanyavertolet.edukate.backend.utils

import org.springframework.data.domain.Sort

object SemVerUtils {
    const val MAJOR_FIELD_NAME = "majorId"
    const val MINOR_FIELD_NAME = "minorId"
    const val PATCH_FIELD_NAME = "patchId"

    private const val SEMVER_SEGMENT_COUNT = 3

    fun semVerSort(
        majorField: String = MAJOR_FIELD_NAME,
        minorField: String = MINOR_FIELD_NAME,
        patchField: String = PATCH_FIELD_NAME,
    ): Sort =
        Sort.by(Sort.Order.asc(majorField), Sort.Order.asc(minorField), Sort.Order.asc(patchField), Sort.Order.asc("_id"))

    fun parse(version: String): Triple<Int, Int, Int> {
        val segments = version.split(".")
        require(segments.size == SEMVER_SEGMENT_COUNT) { "Invalid version format: $version" }
        return Triple(segments[0].toInt(), segments[1].toInt(), segments[2].toInt())
    }
}
