@file:Suppress("ReactiveStreamsUnusedPublisher")

package io.github.sanyavertolet.edukate.backend.mappers

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.configs.LanguageWebFilter
import io.github.sanyavertolet.edukate.backend.repositories.AnswerLocalizationRepository
import io.github.sanyavertolet.edukate.backend.services.files.FileManager
import io.github.sanyavertolet.edukate.common.ContentLanguage
import io.github.sanyavertolet.edukate.storage.keys.AnswerFileKey
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.context.Context

class AnswerMapperTest {
    private val answerLocalizationRepository: AnswerLocalizationRepository = mockk()
    private val fileManager: FileManager = mockk()
    private lateinit var mapper: AnswerMapper

    @BeforeEach
    fun setUp() {
        mapper = AnswerMapper(answerLocalizationRepository, fileManager)
    }

    @Test
    fun `toDto resolves Russian localization with images`() {
        val answer = BackendFixtures.answer(id = 1L, problemId = 1L, images = listOf("ans.png"))
        val loc = BackendFixtures.answerLocalization(answerId = 1L, text = "\$v = 200\$ м/с", notes = "На юг")

        every { answerLocalizationRepository.findByAnswerIdAndLanguage(1L, ContentLanguage.RU) } returns Mono.just(loc)
        every { fileManager.getPresignedUrl(AnswerFileKey("savchenko", "1.1.1", "ans.png")) } returns
            Mono.just("https://s3/ans.png")

        StepVerifier.create(
                mapper
                    .toDto(answer, "savchenko/1.1.1")
                    .contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.RU))
            )
            .assertNext { dto ->
                assertThat(dto.text).isEqualTo("\$v = 200\$ м/с")
                assertThat(dto.notes).isEqualTo("На юг")
                assertThat(dto.images).containsExactly("https://s3/ans.png")
                assertThat(dto.language).isEqualTo(ContentLanguage.RU)
            }
            .verifyComplete()
    }

    @Test
    fun `toDto resolves English localization`() {
        val answer = BackendFixtures.answer(id = 2L, problemId = 1L)
        val enLoc = BackendFixtures.answerLocalization(answerId = 2L, language = ContentLanguage.EN, text = "200 m/s")

        every { answerLocalizationRepository.findByAnswerIdAndLanguage(2L, ContentLanguage.EN) } returns Mono.just(enLoc)

        StepVerifier.create(
                mapper
                    .toDto(answer, "savchenko/1.1.1")
                    .contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.EN))
            )
            .assertNext { dto ->
                assertThat(dto.text).isEqualTo("200 m/s")
                assertThat(dto.language).isEqualTo(ContentLanguage.EN)
            }
            .verifyComplete()
    }

    @Test
    fun `toDto falls back to any language when requested is missing`() {
        val answer = BackendFixtures.answer(id = 3L, problemId = 1L)
        val ruLoc = BackendFixtures.answerLocalization(answerId = 3L, text = "Ответ: 42")

        every { answerLocalizationRepository.findByAnswerIdAndLanguage(3L, ContentLanguage.EN) } returns Mono.empty()
        every { answerLocalizationRepository.findFirstByAnswerId(3L) } returns Mono.just(ruLoc)

        StepVerifier.create(
                mapper
                    .toDto(answer, "savchenko/1.1.1")
                    .contextWrite(Context.of(LanguageWebFilter.LANGUAGE_KEY, ContentLanguage.EN))
            )
            .assertNext { dto ->
                assertThat(dto.text).isEqualTo("Ответ: 42")
                assertThat(dto.language).isEqualTo(ContentLanguage.RU)
            }
            .verifyComplete()
    }
}
