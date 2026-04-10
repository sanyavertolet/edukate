package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.sanyavertolet.edukate.storage.utils.SegmentFileKeyParser

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmissionFileKey::class, name = "submission"),
    JsonSubTypes.Type(value = TempFileKey::class, name = "tmp"),
    JsonSubTypes.Type(value = ProblemFileKey::class, name = "problem"),
    JsonSubTypes.Type(value = ResultFileKey::class, name = "result"),
)
@JsonTypeName("base")
sealed interface FileKey {
    val fileName: String

    fun type(): String

    fun owner(): String?
}

fun fileKey(rawKey: String) = SegmentFileKeyParser.parse(rawKey)
