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
        assertThat(fileKey("users/alice/tmp/report.pdf"))
            .isInstanceOf(TempFileKey::class.java)
            .extracting({ (it as TempFileKey).userId }, { it.fileName })
            .containsExactly("alice", "report.pdf")

        assertThat(fileKey("users/alice/submissions/prob1/sub1/answer.zip"))
            .isInstanceOf(SubmissionFileKey::class.java)
            .satisfies({ key ->
                val k = key as SubmissionFileKey
                assertThat(k.userId).isEqualTo("alice")
                assertThat(k.problemId).isEqualTo("prob1")
                assertThat(k.submissionId).isEqualTo("sub1")
                assertThat(k.fileName).isEqualTo("answer.zip")
            })

        assertThat(fileKey("problems/prob1/statement.pdf"))
            .isInstanceOf(ProblemFileKey::class.java)
            .extracting({ (it as ProblemFileKey).problemId }, { it.fileName })
            .containsExactly("prob1", "statement.pdf")

        assertThat(fileKey("results/prob1/result.json"))
            .isInstanceOf(ResultFileKey::class.java)
            .extracting({ (it as ResultFileKey).problemId }, { it.fileName })
            .containsExactly("prob1", "result.json")
    }

    @Test
    fun `fileKey() normalises leading slash and double slashes`() {
        assertThat(fileKey("/users/alice/tmp/file.txt")).isInstanceOf(TempFileKey::class.java)
        assertThat(fileKey("problems//prob1//file.txt")).isInstanceOf(ProblemFileKey::class.java)
    }

    // Region 2 — fileKey() invalid paths
    @Test
    fun `fileKey() throws for invalid paths`() {
        assertThatIllegalArgumentException().isThrownBy { fileKey("") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("   ") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("users/alice/tmp") }
        assertThatIllegalArgumentException().isThrownBy { fileKey("unknown/prob1/file.txt") }
    }

    // Region 3 — toString() and round-trip
    @Test
    fun `toString() produces correct paths and round-trip via fileKey() preserves type and fields`() {
        assertThat(TempFileKey("alice", "file.txt").toString()).isEqualTo("users/alice/tmp/file.txt")
        assertThat(ProblemFileKey("prob1", "stmt.pdf").toString()).isEqualTo("problems/prob1/stmt.pdf")
        assertThat(ResultFileKey("prob1", "res.json").toString()).isEqualTo("results/prob1/res.json")
        assertThat(SubmissionFileKey("alice", "prob1", "sub1", "ans.zip").toString())
            .isEqualTo("users/alice/submissions/prob1/sub1/ans.zip")

        val original = SubmissionFileKey("bob", "prob2", "sub2", "code.py")
        val roundTripped = fileKey(original.toString())
        assertThat(roundTripped).isInstanceOf(SubmissionFileKey::class.java).isEqualTo(original)
    }

    // Region 4 — prefix()
    @Test
    fun `prefix() returns correct path prefix for each type`() {
        assertThat(TempFileKey.prefix("alice")).isEqualTo("users/alice/tmp/")
        assertThat(ProblemFileKey.prefix("prob1")).isEqualTo("problems/prob1/")
        assertThat(ResultFileKey.prefix("prob1")).isEqualTo("results/prob1/")
        assertThat(SubmissionFileKey.prefix("alice", "prob1", "sub1")).isEqualTo("users/alice/submissions/prob1/sub1/")
    }

    // Region 5 — FileKey.type() and FileKey.owner()
    @Test
    fun `type() and owner() return correct values for each subtype`() {
        assertThat(TempFileKey("alice", "f").type()).isEqualTo("tmp")
        assertThat(SubmissionFileKey("alice", "p", "s", "f").type()).isEqualTo("submission")
        assertThat(ProblemFileKey("prob1", "f").type()).isEqualTo("problem")
        assertThat(ResultFileKey("prob1", "f").type()).isEqualTo("result")

        assertThat(TempFileKey("alice", "f").owner()).isEqualTo("alice")
        assertThat(SubmissionFileKey("alice", "p", "s", "f").owner()).isEqualTo("alice")
        assertThat(ProblemFileKey("prob1", "f").owner()).isNull()
        assertThat(ResultFileKey("prob1", "f").owner()).isNull()
    }

    // Region 6 — equals and hashCode
    @Test
    fun `equals() and hashCode() include all fields`() {
        assertThat(ProblemFileKey("prob1", "a.pdf")).isEqualTo(ProblemFileKey("prob1", "a.pdf"))
        assertThat(ProblemFileKey("prob1", "a.pdf")).isNotEqualTo(ProblemFileKey("prob1", "b.pdf"))
        assertThat(ProblemFileKey("prob1", "a.pdf")).isNotEqualTo(ProblemFileKey("prob2", "a.pdf"))

        assertThat(TempFileKey("alice", "a.txt")).isEqualTo(TempFileKey("alice", "a.txt"))
        assertThat(TempFileKey("alice", "a.txt")).isNotEqualTo(TempFileKey("alice", "b.txt"))
        assertThat(TempFileKey("alice", "a.txt")).isNotEqualTo(TempFileKey("bob", "a.txt"))

        assertThat(SubmissionFileKey("u", "p", "s", "a")).isEqualTo(SubmissionFileKey("u", "p", "s", "a"))
        assertThat(SubmissionFileKey("u", "p", "s", "a")).isNotEqualTo(SubmissionFileKey("u", "p", "s", "b"))
        assertThat(SubmissionFileKey("u", "p", "s", "a")).isNotEqualTo(SubmissionFileKey("u", "p", "x", "a"))
    }

    // Region 7 — Jackson polymorphic serialization
    @Test
    fun `each subtype serializes with correct _type and survives polymorphic round-trip`() {
        val keys: List<FileKey> =
            listOf(
                TempFileKey("alice", "file.txt"),
                ProblemFileKey("prob1", "stmt.pdf"),
                ResultFileKey("prob1", "res.json"),
                SubmissionFileKey("alice", "prob1", "sub1", "ans.zip"),
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
