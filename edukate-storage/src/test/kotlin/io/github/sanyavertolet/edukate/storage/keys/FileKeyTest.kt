package io.github.sanyavertolet.edukate.storage.keys

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class FileKeyTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    // Region 1 — FileKey.of() valid paths
    @Test
    fun `of() parses all valid path formats`() {
        assertThat(FileKey.of("users/alice/tmp/report.pdf"))
            .isInstanceOf(TempFileKey::class.java)
            .extracting({ (it as TempFileKey).userId }, { it.fileName })
            .containsExactly("alice", "report.pdf")

        assertThat(FileKey.of("users/alice/submissions/prob1/sub1/answer.zip"))
            .isInstanceOf(SubmissionFileKey::class.java)
            .satisfies({ key ->
                val k = key as SubmissionFileKey
                assertThat(k.userId).isEqualTo("alice")
                assertThat(k.problemId).isEqualTo("prob1")
                assertThat(k.submissionId).isEqualTo("sub1")
                assertThat(k.fileName).isEqualTo("answer.zip")
            })

        assertThat(FileKey.of("problems/prob1/statement.pdf"))
            .isInstanceOf(ProblemFileKey::class.java)
            .extracting({ (it as ProblemFileKey).problemId }, { it.fileName })
            .containsExactly("prob1", "statement.pdf")

        assertThat(FileKey.of("results/prob1/result.json"))
            .isInstanceOf(ResultFileKey::class.java)
            .extracting({ (it as ResultFileKey).problemId }, { it.fileName })
            .containsExactly("prob1", "result.json")
    }

    @Test
    fun `of() normalises leading slash and double slashes`() {
        assertThat(FileKey.of("/users/alice/tmp/file.txt")).isInstanceOf(TempFileKey::class.java)
        assertThat(FileKey.of("problems//prob1//file.txt")).isInstanceOf(ProblemFileKey::class.java)
    }

    // Region 2 — FileKey.of() invalid paths
    @Test
    fun `of() throws for invalid paths`() {
        assertThatIllegalArgumentException().isThrownBy { FileKey.of("") }
        assertThatIllegalArgumentException().isThrownBy { FileKey.of("   ") }
        assertThatIllegalArgumentException().isThrownBy { FileKey.of("users/alice/tmp") }
        assertThatIllegalArgumentException().isThrownBy { FileKey.of("unknown/prob1/file.txt") }
    }

    // Region 3 — toString() and round-trip
    @Test
    fun `toString() produces correct paths and round-trip via of() preserves type and fields`() {
        assertThat(TempFileKey.of("alice", "file.txt").toString()).isEqualTo("users/alice/tmp/file.txt")
        assertThat(ProblemFileKey.of("prob1", "stmt.pdf").toString()).isEqualTo("problems/prob1/stmt.pdf")
        assertThat(ResultFileKey.of("prob1", "res.json").toString()).isEqualTo("results/prob1/res.json")
        assertThat(SubmissionFileKey.of("alice", "prob1", "sub1", "ans.zip").toString())
            .isEqualTo("users/alice/submissions/prob1/sub1/ans.zip")

        val original = SubmissionFileKey.of("bob", "prob2", "sub2", "code.py")
        val roundTripped = FileKey.of(original.toString())
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

    // Region 5 — typeOf() and ownerOf()
    @Test
    fun `typeOf() and ownerOf() return correct values for each subtype`() {
        assertThat(FileKey.typeOf(TempFileKey.of("alice", "f"))).isEqualTo("tmp")
        assertThat(FileKey.typeOf(SubmissionFileKey.of("alice", "p", "s", "f"))).isEqualTo("submission")
        assertThat(FileKey.typeOf(ProblemFileKey.of("prob1", "f"))).isEqualTo("problem")
        assertThat(FileKey.typeOf(ResultFileKey.of("prob1", "f"))).isEqualTo("result")

        assertThat(FileKey.ownerOf(TempFileKey.of("alice", "f"))).isEqualTo("alice")
        assertThat(FileKey.ownerOf(SubmissionFileKey.of("alice", "p", "s", "f"))).isEqualTo("alice")
        assertThat(FileKey.ownerOf(ProblemFileKey.of("prob1", "f"))).isNull()
        assertThat(FileKey.ownerOf(ResultFileKey.of("prob1", "f"))).isNull()
    }

    // Region 6 — equals and hashCode
    @Test
    fun `equals() and hashCode() use identity fields only`() {
        assertThat(ProblemFileKey.of("prob1", "a.pdf")).isEqualTo(ProblemFileKey.of("prob1", "b.pdf"))
        assertThat(ProblemFileKey.of("prob1", "a.pdf")).hasSameHashCodeAs(ProblemFileKey.of("prob1", "b.pdf"))
        assertThat(ProblemFileKey.of("prob1", "a.pdf")).isNotEqualTo(ProblemFileKey.of("prob2", "a.pdf"))

        assertThat(TempFileKey.of("alice", "a.txt")).isEqualTo(TempFileKey.of("alice", "b.txt"))
        assertThat(TempFileKey.of("alice", "a.txt")).isNotEqualTo(TempFileKey.of("bob", "a.txt"))

        assertThat(SubmissionFileKey.of("u", "p", "s", "a")).isEqualTo(SubmissionFileKey.of("u", "p", "s", "b"))
        assertThat(SubmissionFileKey.of("u", "p", "s", "a")).isNotEqualTo(SubmissionFileKey.of("u", "p", "x", "a"))
    }

    // Region 7 — Jackson polymorphic serialization
    @Test
    fun `each subtype serializes with correct _type and survives polymorphic round-trip`() {
        val keys: List<FileKey> =
            listOf(
                TempFileKey.of("alice", "file.txt"),
                ProblemFileKey.of("prob1", "stmt.pdf"),
                ResultFileKey.of("prob1", "res.json"),
                SubmissionFileKey.of("alice", "prob1", "sub1", "ans.zip"),
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
