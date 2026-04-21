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

        assertThat(fileKey("problems/10/statement.pdf"))
            .isInstanceOf(ProblemFileKey::class.java)
            .extracting({ (it as ProblemFileKey).problemId }, { it.fileName })
            .containsExactly(10L, "statement.pdf")

        assertThat(fileKey("results/10/result.json"))
            .isInstanceOf(ResultFileKey::class.java)
            .extracting({ (it as ResultFileKey).problemId }, { it.fileName })
            .containsExactly(10L, "result.json")
    }

    @Test
    fun `fileKey() normalises leading slash and double slashes`() {
        assertThat(fileKey("/users/1/tmp/file.txt")).isInstanceOf(TempFileKey::class.java)
        assertThat(fileKey("problems//10//file.txt")).isInstanceOf(ProblemFileKey::class.java)
    }

    // Region 2 — fileKey() invalid paths
    @Test
    fun `fileKey() throws for invalid paths`() {
        assertThatIllegalArgumentException().isThrownBy { fileKey("") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("   ") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("users/1/tmp") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("unknown/10/file.txt") }
    }

    // Region 3 — toString() and round-trip
    @Test
    fun `toString() produces correct paths and round-trip via fileKey() preserves type and fields`() {
        assertThat(TempFileKey(1L, "file.txt").toString()).isEqualTo("users/1/tmp/file.txt")
        assertThat(ProblemFileKey(10L, "stmt.pdf").toString()).isEqualTo("problems/10/stmt.pdf")
        assertThat(ResultFileKey(10L, "res.json").toString()).isEqualTo("results/10/res.json")
        assertThat(SubmissionFileKey(1L, 10L, 100L, "ans.zip").toString()).isEqualTo("users/1/submissions/10/100/ans.zip")

        val original = SubmissionFileKey(2L, 20L, 200L, "code.py")
        val roundTripped = fileKey(original.toString())
        assertThat(roundTripped).isInstanceOf(SubmissionFileKey::class.java).isEqualTo(original)
    }

    // Region 4 — prefix()
    @Test
    fun `prefix() returns correct path prefix for each type`() {
        assertThat(TempFileKey.prefix(1L)).isEqualTo("users/1/tmp/")
        assertThat(ProblemFileKey.prefix(10L)).isEqualTo("problems/10/")
        assertThat(ResultFileKey.prefix(10L)).isEqualTo("results/10/")
        assertThat(SubmissionFileKey.prefix(1L, 10L, 100L)).isEqualTo("users/1/submissions/10/100/")
    }

    // Region 5 — FileKey.type() and FileKey.owner()
    @Test
    fun `type() and owner() return correct values for each subtype`() {
        assertThat(TempFileKey(1L, "f").type()).isEqualTo("tmp")
        assertThat(SubmissionFileKey(1L, 10L, 100L, "f").type()).isEqualTo("submission")
        assertThat(ProblemFileKey(10L, "f").type()).isEqualTo("problem")
        assertThat(ResultFileKey(10L, "f").type()).isEqualTo("result")

        assertThat(TempFileKey(1L, "f").owner()).isEqualTo(1L)
        assertThat(SubmissionFileKey(1L, 10L, 100L, "f").owner()).isEqualTo(1L)
        assertThat(ProblemFileKey(10L, "f").owner()).isNull()
        assertThat(ResultFileKey(10L, "f").owner()).isNull()
    }

    // Region 6 — equals and hashCode
    @Test
    fun `equals() and hashCode() include all fields`() {
        assertThat(ProblemFileKey(10L, "a.pdf")).isEqualTo(ProblemFileKey(10L, "a.pdf"))
        assertThat(ProblemFileKey(10L, "a.pdf")).isNotEqualTo(ProblemFileKey(10L, "b.pdf"))
        assertThat(ProblemFileKey(10L, "a.pdf")).isNotEqualTo(ProblemFileKey(20L, "a.pdf"))

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
                ProblemFileKey(10L, "stmt.pdf"),
                ResultFileKey(10L, "res.json"),
                SubmissionFileKey(1L, 10L, 100L, "ans.zip"),
            )
        val expectedTypes = listOf("tmp", "problem", "result", "submission")

        keys.zip(expectedTypes).forEach { (key, expectedType) ->
            val json = objectMapper.writeValueAsString(key)
            assertThat(json).contains("\"_type\":\"$expectedType\"")

            val deserialized = objectMapper.readValue(json, FileKey::class.java)
            assertThat(deserialized).isEqualTo(key)
            assertThat(deserialized::class).isEqualTo(key::class)
        }
    }
}
