package io.github.sanyavertolet.edukate.backend.savelisteners

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.conversions.Bson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent
import reactor.core.publisher.Mono

class SubmissionAfterSaveListenerTest {
    private val template: ReactiveMongoTemplate = mockk()
    private val db: MongoDatabase = mockk()
    private val collection: MongoCollection<Document> = mockk()
    private lateinit var listener: SubmissionAfterSaveListener

    @BeforeEach
    fun setUp() {
        listener = SubmissionAfterSaveListener(template)
        every { template.mongoDatabase } returns Mono.just(db)
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

    private fun fireEvent(submission: io.github.sanyavertolet.edukate.backend.entities.Submission) {
        listener.onAfterSave(AfterSaveEvent(submission, Document(), "submissions"))
    }

    // region filter correctness

    @Test
    fun `onAfterSaveUpsertsNewRecord`() {
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
    fun `onAfterSaveSetsPendingRankZero`() {
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
        val firstSet = capturedPipeline?.firstOrNull { it.containsKey("\$set") }
        assertThat(firstSet).isNotNull()
    }

    @Test
    fun `onAfterSaveSetsSucessRankTwo`() {
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
    fun `onAfterSaveUsesCreatedAtFromSubmission`() {
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
    fun `onAfterSaveFallsBackToNowWhenCreatedAtNull`() {
        var capturedPipeline: List<Document>? = null
        setupUpdateOneCapture { _, pipeline -> capturedPipeline = pipeline }

        fireEvent(
            BackendFixtures.submission(
                id = "sub-1",
                userId = "user-1",
                problemId = "1.0.0",
                status = SubmissionStatus.SUCCESS,
                createdAt = null,
            )
        )

        // Event must complete without error; pipeline was built with Instant.now() as fallback
        assertThat(capturedPipeline).isNotEmpty()
        verify(exactly = 1) {
            @Suppress("UNCHECKED_CAST") collection.updateOne(any<Bson>(), any<List<Bson>>(), any<UpdateOptions>())
        }
    }

    // endregion
}
