package io.github.sanyavertolet.edukate.backend.savelisteners

import com.mongodb.client.model.UpdateOptions
import io.github.sanyavertolet.edukate.backend.entities.Submission
import io.github.sanyavertolet.edukate.common.SubmissionStatus
import java.time.Clock
import java.time.Instant
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SubmissionAfterSaveListener(private val template: ReactiveMongoTemplate) : AbstractMongoEventListener<Submission>() {
    override fun onAfterSave(event: AfterSaveEvent<Submission>) {
        val s = event.source
        val userId = s.userId
        val problemId = s.problemId
        val status = s.status
        val submissionId = requireNotNull(s.id) { "Submission ID must not be null" }
        val createdAt = s.createdAt ?: Instant.now(Clock.systemUTC())
        val newRank =
            when (status) {
                SubmissionStatus.SUCCESS -> RANK_SUCCESS
                SubmissionStatus.FAILED -> RANK_FAILED
                SubmissionStatus.PENDING -> RANK_PENDING
            }
        val filter = Document("userId", userId).append("problemId", problemId)
        val updatePipeline = buildUpdatePipeline(userId, problemId, submissionId, status, createdAt, newRank)
        // todo: implement the duplicate-key retry
        template.mongoDatabase
            .flatMap { db ->
                Mono.from(db.getCollection(COLLECTION).updateOne(filter, updatePipeline, UpdateOptions().upsert(true)))
            }
            .doOnSuccess { log.debug("problem_status upserted for submission {}", submissionId) }
            .doOnError { ex -> log.error("problem_status upsert failed for submission {}", submissionId, ex) }
            .subscribe()
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
        private val log = LoggerFactory.getLogger(SubmissionAfterSaveListener::class.java)
    }
}
