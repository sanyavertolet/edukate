package io.github.sanyavertolet.edukate.backend.savelisteners

import com.mongodb.client.model.UpdateOptions
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import org.bson.Document
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.event.ReactiveAfterSaveCallback
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry

@Component
class SubmissionAfterSaveCallback(@Lazy private val template: ReactiveMongoTemplate) :
    ReactiveAfterSaveCallback<Submission> {
    override fun onAfterSave(entity: Submission, document: Document, collection: String): Publisher<Submission> =
        doUpsert(entity).thenReturn(entity)

    private fun doUpsert(submission: Submission): Mono<Void> {
        val submissionId = requireNotNull(submission.id) { "Submission ID must not be null after save" }
        val createdAt = requireNotNull(submission.createdAt) { "Submission createdAt must not be null after save" }
        val newRank =
            when (submission.status) {
                SubmissionStatus.SUCCESS -> RANK_SUCCESS
                SubmissionStatus.FAILED -> RANK_FAILED
                SubmissionStatus.PENDING -> RANK_PENDING
            }
        val filter = Document("userId", submission.userId).append("problemId", submission.problemId)
        val update =
            buildUpdatePipeline(submission.userId, submission.problemId, submissionId, submission.status, createdAt, newRank)
        // todo: implement the duplicate-key retry

        return template.mongoDatabase
            .flatMap { db ->
                Mono.from(db.getCollection(COLLECTION).updateOne(filter, update, UpdateOptions().upsert(true)))
            }
            .retryWhen(retryBackoff)
            .doOnNext { log.debug("problem_status upserted for submission {}", submissionId) }
            .doOnError { ex -> log.error("problem_status upsert failed for submission {}", submissionId, ex) }
            .then()
    }

    @Suppress("LongMethod")
    private fun buildUpdatePipeline(
        userId: String,
        problemId: String,
        submissionId: String,
        status: SubmissionStatus,
        createdAt: Instant,
        newRank: Int,
    ): List<Document> =
        listOf(
            Document(
                $$"$set",
                Document("userId", userId)
                    .append("problemId", problemId)
                    .append("latestStatus", status)
                    .append("latestTime", createdAt)
                    .append("latestSubmissionId", submissionId),
            ),
            Document($$"$set", Document("_prevBestRank", Document($$"$ifNull", listOf($$"$bestRank", -1)))),
            Document(
                $$"$set",
                Document(
                    "_takeNewBest",
                    Document(
                        $$"$or",
                        listOf(
                            Document($$"$gt", listOf(newRank, $$"$_prevBestRank")),
                            Document(
                                $$"$and",
                                listOf(
                                    Document($$"$eq", listOf(newRank, $$"$_prevBestRank")),
                                    Document(
                                        $$"$lt",
                                        listOf(createdAt, Document($$"$ifNull", listOf($$"$bestTime", createdAt))),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            Document(
                $$"$set",
                Document(
                    "bestRank",
                    Document(
                        $$"$cond",
                        listOf($$"$_takeNewBest", newRank, Document($$"$ifNull", listOf($$"$bestRank", $$"$_prevBestRank"))),
                    ),
                ),
            ),
            Document(
                $$"$set",
                Document(
                    "bestStatus",
                    Document(
                        $$"$cond",
                        listOf($$"$_takeNewBest", status, Document($$"$ifNull", listOf($$"$bestStatus", status))),
                    ),
                ),
            ),
            Document(
                $$"$set",
                Document(
                    "bestTime",
                    Document(
                        $$"$cond",
                        listOf($$"$_takeNewBest", createdAt, Document($$"$ifNull", listOf($$"$bestTime", createdAt))),
                    ),
                ),
            ),
            Document(
                $$"$set",
                Document(
                    "bestSubmissionId",
                    Document(
                        $$"$cond",
                        listOf(
                            $$"$_takeNewBest",
                            submissionId,
                            Document($$"$ifNull", listOf($$"$bestSubmissionId", submissionId)),
                        ),
                    ),
                ),
            ),
            Document($$"$unset", listOf("_prevBestRank", "_takeNewBest")),
        )

    companion object {
        private const val COLLECTION = "problem_status"
        private const val RANK_SUCCESS = 2
        private const val RANK_FAILED = 1
        private const val RANK_PENDING = 0
        private val retryBackoff = Retry.backoff(3, 100.milliseconds.toJavaDuration())
        private val log = LoggerFactory.getLogger(SubmissionAfterSaveCallback::class.java)
    }
}
