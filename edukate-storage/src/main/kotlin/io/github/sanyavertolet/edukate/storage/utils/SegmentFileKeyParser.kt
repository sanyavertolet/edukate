package io.github.sanyavertolet.edukate.storage.utils

import io.github.sanyavertolet.edukate.storage.keys.AnswerFileKey
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.github.sanyavertolet.edukate.storage.keys.ProblemFileKey
import io.github.sanyavertolet.edukate.storage.keys.SubmissionFileKey
import io.github.sanyavertolet.edukate.storage.keys.TempFileKey

object SegmentFileKeyParser {
    private const val TMP_SEGMENT_COUNT = 4
    private const val BOOK_SEGMENT_COUNT = 5
    private const val SUBMISSION_SEGMENT_COUNT = 6
    private const val USER_ID_INDEX = 1
    private const val PATH_TYPE_INDEX = 2
    private const val TMP_FILE_NAME_INDEX = 3
    private const val BOOK_SLUG_INDEX = 1
    private const val BOOK_ITEM_TYPE_INDEX = 2
    private const val BOOK_CODE_INDEX = 3
    private const val BOOK_FILE_NAME_INDEX = 4
    private const val SUBMISSION_PROBLEM_ID_INDEX = 3
    private const val SUBMISSION_SUBMISSION_ID_INDEX = 4
    private const val SUBMISSION_FILE_NAME_INDEX = 5

    fun parse(rawKey: String): FileKey {
        require(rawKey.isNotBlank()) { "Key must not be blank" }
        var norm = rawKey.trim()
        if (norm.startsWith("/")) norm = norm.substring(1)
        norm = norm.replace(Regex("/+"), "/")
        val segments = norm.split("/").filter { it.isNotEmpty() }

        return when (segments.firstOrNull()) {
            "users" -> parseUserKey(segments, rawKey)
            "books" -> parseBookKey(segments, rawKey)
            else ->
                throw IllegalArgumentException("Invalid key format: '$rawKey' (normalized: '${segments.joinToString("/")}')")
        }
    }

    private fun parseUserKey(segments: List<String>, rawKey: String): FileKey {
        // users/{userId}/tmp/{fileName}
        if (segments.size == TMP_SEGMENT_COUNT && segments[PATH_TYPE_INDEX] == "tmp") {
            return TempFileKey(segments[USER_ID_INDEX].toLong(), segments[TMP_FILE_NAME_INDEX])
        }
        // users/{userId}/submissions/{problemId}/{submissionId}/{fileName}
        if (segments.size == SUBMISSION_SEGMENT_COUNT && segments[PATH_TYPE_INDEX] == "submissions") {
            return SubmissionFileKey(
                segments[USER_ID_INDEX].toLong(),
                segments[SUBMISSION_PROBLEM_ID_INDEX].toLong(),
                segments[SUBMISSION_SUBMISSION_ID_INDEX].toLong(),
                segments[SUBMISSION_FILE_NAME_INDEX],
            )
        }
        throw IllegalArgumentException("Invalid key format: '$rawKey'")
    }

    private fun parseBookKey(segments: List<String>, rawKey: String): FileKey {
        if (segments.size != BOOK_SEGMENT_COUNT) {
            throw IllegalArgumentException("Invalid key format: '$rawKey'")
        }
        return when (segments[BOOK_ITEM_TYPE_INDEX]) {
            "problems" ->
                ProblemFileKey(segments[BOOK_SLUG_INDEX], segments[BOOK_CODE_INDEX], segments[BOOK_FILE_NAME_INDEX])
            "answers" -> AnswerFileKey(segments[BOOK_SLUG_INDEX], segments[BOOK_CODE_INDEX], segments[BOOK_FILE_NAME_INDEX])
            else -> throw IllegalArgumentException("Invalid key format: '$rawKey'")
        }
    }
}
