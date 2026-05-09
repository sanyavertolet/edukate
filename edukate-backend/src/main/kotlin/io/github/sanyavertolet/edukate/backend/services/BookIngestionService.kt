package io.github.sanyavertolet.edukate.backend.services

import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookIngestionRequest
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.BookIngestionResponse
import io.github.sanyavertolet.edukate.backend.dtos.ingestion.ProblemIngestionEntry
import io.github.sanyavertolet.edukate.backend.entities.Answer
import io.github.sanyavertolet.edukate.backend.entities.AnswerLocalization
import io.github.sanyavertolet.edukate.backend.entities.Book
import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.ProblemLocalization
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.AnswerRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemLocalizationRepository
import io.github.sanyavertolet.edukate.backend.repositories.ProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class BookIngestionService(
    private val bookService: BookService,
    private val problemRepository: ProblemRepository,
    private val answerRepository: AnswerRepository,
    private val problemLocalizationRepository: ProblemLocalizationRepository,
    private val answerLocalizationRepository: AnswerLocalizationRepository,
) {
    @Transactional
    fun ingest(request: BookIngestionRequest): Mono<BookIngestionResponse> =
        upsertBook(request).flatMap { book ->
            val bookId = requireNotNull(book.id)
            val slug = book.slug
            var problemCount = 0
            var answerCount = 0
            var problemLocCount = 0
            var answerLocCount = 0

            request.problems
                .fold(Mono.just(Unit)) { chain, entry ->
                    chain.then(
                        ingestProblem(entry, bookId, slug)
                            .doOnNext { counts ->
                                problemCount++
                                problemLocCount += counts.first
                                if (counts.second > 0) answerCount++
                                answerLocCount += counts.second
                            }
                            .then(Mono.just(Unit))
                    )
                }
                .thenReturn(
                    BookIngestionResponse(
                        bookSlug = slug,
                        problemCount = problemCount,
                        answerCount = answerCount,
                        problemLocalizationCount = problemLocCount,
                        answerLocalizationCount = answerLocCount,
                    )
                )
        }

    private fun upsertBook(request: BookIngestionRequest): Mono<Book> {
        val data = request.book
        return bookService
            .findBySlug(data.slug)
            .flatMap { existing ->
                bookService.save(
                    existing.copy(
                        subject = data.subject,
                        title = data.title,
                        citation = data.citation,
                        description = data.description,
                    )
                )
            }
            .switchIfEmpty(
                bookService.save(
                    Book(
                        slug = data.slug,
                        subject = data.subject,
                        title = data.title,
                        citation = data.citation,
                        description = data.description,
                    )
                )
            )
    }

    private fun ingestProblem(entry: ProblemIngestionEntry, bookId: Long, bookSlug: String): Mono<Pair<Int, Int>> {
        val key = "$bookSlug/${entry.code}"
        return upsertProblem(entry, bookId, key).flatMap { problem ->
            val problemId = requireNotNull(problem.id)
            val locMono = upsertProblemLocalizations(problemId, entry)
            val answerMono =
                entry.answer?.let { answerEntry ->
                    upsertAnswer(problemId, answerEntry).flatMap { answer ->
                        upsertAnswerLocalizations(requireNotNull(answer.id), answerEntry)
                    }
                } ?: Mono.just(0)

            Mono.zip(locMono, answerMono) { locCount, ansLocCount -> locCount to ansLocCount }
        }
    }

    private fun upsertProblem(entry: ProblemIngestionEntry, bookId: Long, key: String): Mono<Problem> =
        problemRepository
            .findByKey(key)
            .flatMap { existing -> problemRepository.save(existing.copy(isHard = entry.isHard, images = entry.images)) }
            .switchIfEmpty(
                problemRepository.save(
                    Problem(bookId = bookId, code = entry.code, key = key, isHard = entry.isHard, images = entry.images)
                )
            )

    private fun upsertProblemLocalizations(problemId: Long, entry: ProblemIngestionEntry): Mono<Int> =
        entry.localizations.entries.fold(Mono.just(0)) { chain, (language, data) ->
            chain.flatMap { count ->
                problemLocalizationRepository
                    .findByProblemIdAndLanguage(problemId, language)
                    .flatMap { existing ->
                        problemLocalizationRepository.save(
                            existing.copy(text = data.text, tags = data.tags, subproblems = data.subproblems)
                        )
                    }
                    .switchIfEmpty(
                        problemLocalizationRepository.save(
                            ProblemLocalization(
                                problemId = problemId,
                                language = language,
                                text = data.text,
                                tags = data.tags,
                                subproblems = data.subproblems,
                            )
                        )
                    )
                    .map { count + 1 }
            }
        }

    private fun upsertAnswer(
        problemId: Long,
        answerEntry: io.github.sanyavertolet.edukate.backend.dtos.ingestion.AnswerIngestionEntry,
    ): Mono<Answer> =
        answerRepository
            .findByProblemId(problemId)
            .flatMap { existing -> answerRepository.save(existing.copy(images = answerEntry.images)) }
            .switchIfEmpty(answerRepository.save(Answer(problemId = problemId, images = answerEntry.images)))

    private fun upsertAnswerLocalizations(
        answerId: Long,
        answerEntry: io.github.sanyavertolet.edukate.backend.dtos.ingestion.AnswerIngestionEntry,
    ): Mono<Int> =
        answerEntry.localizations.entries.fold(Mono.just(0)) { chain, (language, data) ->
            chain.flatMap { count ->
                answerLocalizationRepository
                    .findByAnswerIdAndLanguage(answerId, language)
                    .flatMap { existing ->
                        answerLocalizationRepository.save(existing.copy(text = data.text, notes = data.notes))
                    }
                    .switchIfEmpty(
                        answerLocalizationRepository.save(
                            AnswerLocalization(
                                answerId = answerId,
                                language = language,
                                text = data.text,
                                notes = data.notes,
                            )
                        )
                    )
                    .map { count + 1 }
            }
        }
}
