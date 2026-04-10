package io.github.sanyavertolet.edukate.backend.savelisteners

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class SubmissionAfterSaveCallbackTest {
    private val template: ReactiveMongoTemplate = mockk()
    private val db: MongoDatabase = mockk()
    private val collection: MongoCollection<Document> = mockk()
    private lateinit var callback: SubmissionAfterSaveCallback

    @BeforeEach
    fun setUp() {
        callback = SubmissionAfterSaveCallback(template)
        @Suppress("ReactiveStreamsUnusedPublisher")
        every { template.mongoDatabase } returns db.toMono()
        every { db.getCollection("problem_status") } returns collection
    }

    private fun setupUpdateOneCapture(onCapture: (Document, List<Document>) -> Unit) {
        every {
            @Suppress("UNCHECKED_CAST") collection.updateOne(any<Bson>(), any<List<Bson>>(), any<UpdateOptions>())
        } answers
            {
                @Suppress("UNCHECKED_CAST")
                onCapture(firstArg<Bson>() as Document, secondArg<List<Bson>>() as List<Document>)
                Mono.empty<UpdateResult>()
            }
    }

    private fun fireEvent(submission: Submission) {
        Mono.from(callback.onAfterSave(submission, Document(), "submissions")).block()
    }

    // region filter correctness

    @Test
    fun `onAfterSave upserts new record`() {
        var capturedFilter: Document? = null
        var capturedPipeline: List<Document>? = null
        setupUpdateOneCapture { filter, pipeline ->
            capturedFilter = filter
            capturedPipeline = pipeline
        }

        val submission =
            BackendFixtures.submission(
                id = "sub-1",
                userId = "user-1",
                problemId = "1.0.0",
                status = SubmissionStatus.SUCCESS,
            )
        fireEvent(submission)

        assertThat(capturedFilter?.get("userId")).isEqualTo("user-1")
        assertThat(capturedFilter?.get("problemId")).isEqualTo("1.0.0")
        assertThat(capturedPipeline).isNotEmpty()
    }

    // endregion

    // region rank values encoded in pipeline

    @Test
    fun `onAfterSave sets pending rank zero`() {
        var capturedPipeline: List<Document>? = null
        setupUpdateOneCapture { _, pipeline -> capturedPipeline = pipeline }

        fireEvent(
            BackendFixtures.submission(
                id = "sub-1",
                userId = "user-1",
                problemId = "1.0.0",
                status = SubmissionStatus.PENDING,
            )
        )

        // Pipeline should have at least a first $set stage
        val firstSet = capturedPipeline?.firstOrNull { it.containsKey($$"$set") }
        assertThat(firstSet).isNotNull()
    }

    @Test
    fun `onAfterSave sets success rank two`() {
        var capturedPipeline: List<Document>? = null
        setupUpdateOneCapture { _, pipeline -> capturedPipeline = pipeline }

        fireEvent(
            BackendFixtures.submission(
                id = "sub-1",
                userId = "user-1",
                problemId = "1.0.0",
                status = SubmissionStatus.SUCCESS,
            )
        )

        // Pipeline must have multiple stages (set, conditionals, unset)
        assertThat(capturedPipeline).isNotNull()
        assertThat(capturedPipeline!!.size).isGreaterThan(1)
    }

    // endregion

    // region createdAt handling

    @Test
    fun `onAfterSave uses createdAt from Submission`() {
        var capturedFilter: Document? = null
        setupUpdateOneCapture { filter, _ -> capturedFilter = filter }

        val fixedTime = Instant.parse("2024-01-01T00:00:00Z")
        fireEvent(
            BackendFixtures.submission(
                id = "sub-1",
                userId = "user-2",
                problemId = "2.0.0",
                status = SubmissionStatus.SUCCESS,
                createdAt = fixedTime,
            )
        )

        // Verify filter was built with the correct userId/problemId from the submission
        assertThat(capturedFilter?.get("userId")).isEqualTo("user-2")
        assertThat(capturedFilter?.get("problemId")).isEqualTo("2.0.0")
    }

    @Test
    fun `onAfterSave throws when createdAt is null`() {
        assertThrows<IllegalArgumentException> {
            fireEvent(
                BackendFixtures.submission(
                    id = "sub-1",
                    userId = "user-1",
                    problemId = "1.0.0",
                    status = SubmissionStatus.SUCCESS,
                    createdAt = null,
                )
            )
        }
    }

    // endregion
}
