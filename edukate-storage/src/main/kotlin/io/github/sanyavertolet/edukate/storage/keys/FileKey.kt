package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = SubmissionFileKey::class, name = "submission"),
    JsonSubTypes.Type(value = TempFileKey::class, name = "tmp"),
    JsonSubTypes.Type(value = ProblemFileKey::class, name = "problem"),
    JsonSubTypes.Type(value = ResultFileKey::class, name = "result"),
)
@JsonTypeName("base")
sealed class FileKey(val fileName: String) {
    companion object {
        private const val TMP_SEGMENT_COUNT = 4
        private const val SIMPLE_SEGMENT_COUNT = 3
        private const val SUBMISSION_SEGMENT_COUNT = 6
        private const val USER_ID_INDEX = 1
        private const val PATH_TYPE_INDEX = 2
        private const val TMP_FILE_NAME_INDEX = 3
        private const val SUBMISSION_PROBLEM_ID_INDEX = 3
        private const val SUBMISSION_SUBMISSION_ID_INDEX = 4
        private const val SUBMISSION_FILE_NAME_INDEX = 5

        @JvmStatic
        fun typeOf(key: FileKey) =
            when (key) {
                is TempFileKey -> "tmp"
                is SubmissionFileKey -> "submission"
                is ProblemFileKey -> "problem"
                is ResultFileKey -> "result"
            }

        @JvmStatic
        fun ownerOf(key: FileKey): String? =
            when (key) {
                is TempFileKey -> key.userId
                is SubmissionFileKey -> key.userId
                is ProblemFileKey -> null
                is ResultFileKey -> null
            }

        @JvmStatic
        fun of(rawKey: String): FileKey {
            require(rawKey.isNotBlank()) { "Key must not be blank" }
            var norm = rawKey.trim()
            if (norm.startsWith("/")) norm = norm.substring(1)
            norm = norm.replace(Regex("/+"), "/")
            val segments = norm.split("/").filter { it.isNotEmpty() }

            // users/{userId}/tmp/{fileName}
            if (segments.size == TMP_SEGMENT_COUNT && segments[0] == "users" && segments[PATH_TYPE_INDEX] == "tmp") {
                return TempFileKey.of(segments[USER_ID_INDEX], segments[TMP_FILE_NAME_INDEX])
            }

            // users/{userId}/submissions/{problemId}/{submissionId}/{fileName}
            if (
                segments.size == SUBMISSION_SEGMENT_COUNT &&
                    segments[0] == "users" &&
                    segments[PATH_TYPE_INDEX] == "submissions"
            ) {
                return SubmissionFileKey.of(
                    segments[USER_ID_INDEX],
                    segments[SUBMISSION_PROBLEM_ID_INDEX],
                    segments[SUBMISSION_SUBMISSION_ID_INDEX],
                    segments[SUBMISSION_FILE_NAME_INDEX],
                )
            }

            // problems/{problemId}/{fileName}
            if (segments.size == SIMPLE_SEGMENT_COUNT && segments[0] == "problems") {
                return ProblemFileKey.of(segments[1], segments[2])
            }

            // results/{problemId}/{fileName}
            if (segments.size == SIMPLE_SEGMENT_COUNT && segments[0] == "results") {
                return ResultFileKey.of(segments[1], segments[2])
            }

            throw IllegalArgumentException("Invalid key format: '$rawKey' (normalized: '${segments.joinToString("/")}')")
        }
    }
}
