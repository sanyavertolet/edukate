package io.github.sanyavertolet.edukate.storage.utils

import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.github.sanyavertolet.edukate.storage.keys.ResultFileKey
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey

object SegmentFileKeyParser {
    private const val TMP_SEGMENT_COUNT = 4
    private const val SIMPLE_SEGMENT_COUNT = 3
    private const val SUBMISSION_SEGMENT_COUNT = 6
    private const val USER_ID_INDEX = 1
    private const val PATH_TYPE_INDEX = 2
    private const val TMP_FILE_NAME_INDEX = 3
    private const val SUBMISSION_PROBLEM_ID_INDEX = 3
    private const val SUBMISSION_SUBMISSION_ID_INDEX = 4
    private const val SUBMISSION_FILE_NAME_INDEX = 5

    fun parse(rawKey: String): FileKey {
        require(rawKey.isNotBlank()) { "Key must not be blank" }
        var norm = rawKey.trim()
        if (norm.startsWith("/")) norm = norm.substring(1)
        norm = norm.replace(Regex("/+"), "/")
        val segments = norm.split("/").filter { it.isNotEmpty() }

        // users/{userId}/tmp/{fileName}
        if (segments.size == TMP_SEGMENT_COUNT && segments[0] == "users" && segments[PATH_TYPE_INDEX] == "tmp") {
            return TempFileKey(segments[USER_ID_INDEX], segments[TMP_FILE_NAME_INDEX])
        }

        // users/{userId}/submissions/{problemId}/{submissionId}/{fileName}
        if (
            segments.size == SUBMISSION_SEGMENT_COUNT && segments[0] == "users" && segments[PATH_TYPE_INDEX] == "submissions"
        ) {
            return SubmissionFileKey(
                segments[USER_ID_INDEX],
                segments[SUBMISSION_PROBLEM_ID_INDEX],
                segments[SUBMISSION_SUBMISSION_ID_INDEX],
                segments[SUBMISSION_FILE_NAME_INDEX],
            )
        }

        // problems/{problemId}/{fileName}
        if (segments.size == SIMPLE_SEGMENT_COUNT && segments[0] == "problems") {
            return ProblemFileKey(segments[1], segments[2])
        }

        // results/{problemId}/{fileName}
        if (segments.size == SIMPLE_SEGMENT_COUNT && segments[0] == "results") {
            return ResultFileKey(segments[1], segments[2])
        }

        throw IllegalArgumentException("Invalid key format: '$rawKey' (normalized: '${segments.joinToString("/")}')")
    }
}
