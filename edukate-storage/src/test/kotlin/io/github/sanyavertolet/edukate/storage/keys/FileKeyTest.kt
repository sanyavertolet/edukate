package io.github.sanyavertolet.edukate.storage.keys

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class FileKeyTest {
    private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    // Region 1 — fileKey() valid paths
    @Test
    fun `fileKey() parses all valid path formats`() {
        assertThat(fileKey("users/1/tmp/report.pdf"))
            .isInstanceOf(TempFileKey::class.java)
            .extracting({ (it as TempFileKey).userId }, { it.fileName })
            .containsExactly(1L, "report.pdf")

        assertThat(fileKey("users/1/submissions/10/100/answer.zip"))
            .isInstanceOf(SubmissionFileKey::class.java)
            .satisfies({ key ->
                val k = key as SubmissionFileKey
                assertThat(k.userId).isEqualTo(1L)
                assertThat(k.problemId).isEqualTo(10L)
                assertThat(k.submissionId).isEqualTo(100L)
                assertThat(k.fileName).isEqualTo("answer.zip")
            })

        assertThat(fileKey("books/savchenko/problems/1.1.1/statement.pdf"))
            .isInstanceOf(ProblemFileKey::class.java)
            .satisfies({ key ->
                val k = key as ProblemFileKey
                assertThat(k.bookSlug).isEqualTo("savchenko")
                assertThat(k.problemCode).isEqualTo("1.1.1")
                assertThat(k.fileName).isEqualTo("statement.pdf")
            })

        assertThat(fileKey("books/savchenko/answers/1.1.1/result.json"))
            .isInstanceOf(AnswerFileKey::class.java)
            .satisfies({ key ->
                val k = key as AnswerFileKey
                assertThat(k.bookSlug).isEqualTo("savchenko")
                assertThat(k.problemCode).isEqualTo("1.1.1")
                assertThat(k.fileName).isEqualTo("result.json")
            })
    }

    @Test
    fun `fileKey() normalises leading slash and double slashes`() {
        assertThat(fileKey("/users/1/tmp/file.txt")).isInstanceOf(TempFileKey::class.java)
        assertThat(fileKey("books//savchenko//problems//1.1.1//file.txt")).isInstanceOf(ProblemFileKey::class.java)
    }

    // Region 2 — fileKey() invalid paths
    @Test
    fun `fileKey() throws for invalid paths`() {
        assertThatIllegalArgumentException().isThrownBy { fileKey("") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("   ") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("users/1/tmp") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("unknown/savchenko/problems/1.1.1/file.txt") }
    }

    // Region 3 — toString() and round-trip
    @Test
    fun `toString() produces correct paths and round-trip via fileKey() preserves type and fields`() {
        assertThat(TempFileKey(1L, "file.txt").toString()).isEqualTo("users/1/tmp/file.txt")
        assertThat(ProblemFileKey("savchenko", "1.1.1", "stmt.pdf").toString())
            .isEqualTo("books/savchenko/problems/1.1.1/stmt.pdf")
        assertThat(AnswerFileKey("savchenko", "1.1.1", "res.json").toString())
            .isEqualTo("books/savchenko/answers/1.1.1/res.json")
        assertThat(SubmissionFileKey(1L, 10L, 100L, "ans.zip").toString()).isEqualTo("users/1/submissions/10/100/ans.zip")

        val original = SubmissionFileKey(2L, 20L, 200L, "code.py")
        val roundTripped = fileKey(original.toString())
        assertThat(roundTripped).isInstanceOf(SubmissionFileKey::class.java).isEqualTo(original)

        val problemKey = ProblemFileKey("savchenko", "2.3.4", "img.jpg")
        assertThat(fileKey(problemKey.toString())).isEqualTo(problemKey)

        val answerKey = AnswerFileKey("savchenko", "2.3.4", "ans.jpg")
        assertThat(fileKey(answerKey.toString())).isEqualTo(answerKey)
    }

    // Region 4 — prefix()
    @Test
    fun `prefix() returns correct path prefix for each type`() {
        assertThat(TempFileKey.prefix(1L)).isEqualTo("users/1/tmp/")
        assertThat(ProblemFileKey.prefix("savchenko", "1.1.1")).isEqualTo("books/savchenko/problems/1.1.1/")
        assertThat(ProblemFileKey.bookPrefix("savchenko")).isEqualTo("books/savchenko/problems/")
        assertThat(AnswerFileKey.prefix("savchenko", "1.1.1")).isEqualTo("books/savchenko/answers/1.1.1/")
        assertThat(SubmissionFileKey.prefix(1L, 10L, 100L)).isEqualTo("users/1/submissions/10/100/")
    }

    // Region 5 — FileKey.type() and FileKey.owner()
    @Test
    fun `type() and owner() return correct values for each subtype`() {
        assertThat(TempFileKey(1L, "f").type()).isEqualTo("tmp")
        assertThat(SubmissionFileKey(1L, 10L, 100L, "f").type()).isEqualTo("submission")
        assertThat(ProblemFileKey("savchenko", "1.1.1", "f").type()).isEqualTo("problem")
        assertThat(AnswerFileKey("savchenko", "1.1.1", "f").type()).isEqualTo("answer")

        assertThat(TempFileKey(1L, "f").owner()).isEqualTo(1L)
        assertThat(SubmissionFileKey(1L, 10L, 100L, "f").owner()).isEqualTo(1L)
        assertThat(ProblemFileKey("savchenko", "1.1.1", "f").owner()).isNull()
        assertThat(AnswerFileKey("savchenko", "1.1.1", "f").owner()).isNull()
    }

    // Region 6 — equals and hashCode
    @Test
    fun `equals() and hashCode() include all fields`() {
        assertThat(ProblemFileKey("savchenko", "1.1.1", "a.pdf")).isEqualTo(ProblemFileKey("savchenko", "1.1.1", "a.pdf"))
        assertThat(ProblemFileKey("savchenko", "1.1.1", "a.pdf")).isNotEqualTo(ProblemFileKey("savchenko", "1.1.1", "b.pdf"))
        assertThat(ProblemFileKey("savchenko", "1.1.1", "a.pdf")).isNotEqualTo(ProblemFileKey("savchenko", "1.1.2", "a.pdf"))
        assertThat(ProblemFileKey("savchenko", "1.1.1", "a.pdf")).isNotEqualTo(ProblemFileKey("other", "1.1.1", "a.pdf"))

        assertThat(TempFileKey(1L, "a.txt")).isEqualTo(TempFileKey(1L, "a.txt"))
        assertThat(TempFileKey(1L, "a.txt")).isNotEqualTo(TempFileKey(1L, "b.txt"))
        assertThat(TempFileKey(1L, "a.txt")).isNotEqualTo(TempFileKey(2L, "a.txt"))

        assertThat(SubmissionFileKey(1L, 10L, 100L, "a")).isEqualTo(SubmissionFileKey(1L, 10L, 100L, "a"))
        assertThat(SubmissionFileKey(1L, 10L, 100L, "a")).isNotEqualTo(SubmissionFileKey(1L, 10L, 100L, "b"))
        assertThat(SubmissionFileKey(1L, 10L, 100L, "a")).isNotEqualTo(SubmissionFileKey(1L, 10L, 200L, "a"))
    }

    // Region 7 — Jackson polymorphic serialization
    @Test
    fun `each subtype serializes with correct _type and survives polymorphic round-trip`() {
        val keys: List<FileKey> =
            listOf(
                TempFileKey(1L, "file.txt"),
                ProblemFileKey("savchenko", "1.1.1", "stmt.pdf"),
                AnswerFileKey("savchenko", "1.1.1", "res.json"),
                SubmissionFileKey(1L, 10L, 100L, "ans.zip"),
            )
        val expectedTypes = listOf("tmp", "problem", "answer", "submission")

        keys.zip(expectedTypes).forEach { (key, expectedType) ->
            val json = objectMapper.writeValueAsString(key)
            assertThat(json).contains("\"_type\":\"$expectedType\"")

            val deserialized = objectMapper.readValue(json, FileKey::class.java)
            assertThat(deserialized).isEqualTo(key)
            assertThat(deserialized::class).isEqualTo(key::class)
        }
    }
}
