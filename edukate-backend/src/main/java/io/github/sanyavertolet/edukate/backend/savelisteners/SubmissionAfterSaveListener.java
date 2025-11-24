package io.github.sanyavertolet.edukate.backend.savelisteners;

import com.mongodb.client.model.UpdateOptions;
import io.github.sanyavertolet.edukate.backend.entities.Submission;
import io.github.sanyavertolet.edukate.common.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class SubmissionAfterSaveListener extends AbstractMongoEventListener<Submission> {
    private static final String COLLECTION = "problem_status";
    private final ReactiveMongoTemplate template;

    @Override
    public void onAfterSave(@NonNull AfterSaveEvent<Submission> event) {
        Submission s = event.getSource();
        String userId = s.getUserId();
        String problemId = s.getProblemId();
        SubmissionStatus status = s.getStatus();
        String submissionId = s.getId();

        Instant createdAt = s.getCreatedAt();
        if (createdAt == null) {
            createdAt = Instant.now(Clock.systemUTC());
        }

        int newRank = switch (status) {
            case SUCCESS -> 2;
            case FAILED -> 1;
            case PENDING -> 0;
        };

        Document filter = new Document("userId", userId).append("problemId", problemId);

        List<Document> updatePipeline = List.of(
                new Document("$set", new Document("userId", userId)
                        .append("problemId", problemId)
                        .append("latestStatus", status)
                        .append("latestTime", createdAt)
                        .append("latestSubmissionId", submissionId)
                ),
                new Document("$set", new Document("_prevBestRank", new Document("$ifNull", List.of("$bestRank", -1)))),
                new Document("$set", new Document("_takeNewBest",
                        new Document("$or", List.of(
                                new Document("$gt", List.of(newRank, "$_prevBestRank")),
                                new Document("$and", List.of(
                                        new Document("$eq", List.of(newRank, "$_prevBestRank")),
                                        new Document("$lt", List.of(
                                                createdAt,
                                                new Document("$ifNull", List.of("$bestTime", createdAt))
                                        ))
                                ))
                        ))
                )),
                new Document("$set", new Document("bestRank",
                        new Document("$cond", List.of(
                                "$_takeNewBest",
                                newRank,
                                new Document("$ifNull", List.of("$bestRank", "$_prevBestRank"))
                        ))
                )),
                new Document("$set", new Document("bestStatus",
                        new Document("$cond", List.of(
                                "$_takeNewBest",
                                status,
                                new Document("$ifNull", List.of("$bestStatus", status))
                        ))
                )),
                new Document("$set", new Document("bestTime",
                        new Document("$cond", List.of(
                                "$_takeNewBest",
                                createdAt,
                                new Document("$ifNull", List.of("$bestTime", createdAt))
                        ))
                )),
                new Document("$set", new Document("bestSubmissionId",
                        new Document("$cond", List.of(
                                "$_takeNewBest",
                                submissionId,
                                new Document("$ifNull", List.of("$bestSubmissionId", submissionId))
                        ))
                )),
                new Document("$unset", List.of("_prevBestRank", "_takeNewBest"))
        );

        // todo: implement the duplicate-key retry
        template.getMongoDatabase()
            .flatMap(db -> Mono.from(
                db.getCollection(COLLECTION)
                    .updateOne(filter, updatePipeline, new UpdateOptions().upsert(true))
            ))
                .doOnSuccess(_ -> log.debug("problem_status upserted for submission {}", submissionId))
                .doOnError(ex -> log.error("problem_status upsert failed for submission {}", submissionId, ex))
                .subscribe();
    }
}
