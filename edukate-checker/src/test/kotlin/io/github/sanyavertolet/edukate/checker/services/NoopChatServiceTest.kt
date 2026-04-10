package io.github.sanyavertolet.edukate.checker.services

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.github.sanyavertolet.edukate.checker.CheckerFixtures
import io.github.sanyavertolet.edukate.checker.domain.RequestContext
import io.github.sanyavertolet.edukate.checker.services.impl.NoopChatService
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import reactor.test.StepVerifier

class NoopChatServiceTest {
    private val svc = NoopChatService()
    private val ctx =
        RequestContext("Solve x^2 = 4", listOf(CheckerFixtures.mockMedia()), listOf(CheckerFixtures.mockMedia()))

    @Test
    fun `makeRequest emits exactly one item`() {
        StepVerifier.create(svc.makeRequest(ctx)).expectNextCount(1).verifyComplete()
    }

    @Test
    fun `returned status is SUCCESS`() {
        StepVerifier.create(svc.makeRequest(ctx))
            .assertNext { response -> assertThat(response.status).isEqualTo(CheckStatus.SUCCESS) }
            .verifyComplete()
    }

    @Test
    fun `trustLevel is in range 0 to 1`() {
        StepVerifier.create(svc.makeRequest(ctx))
            .assertNext { response -> assertThat(response.trustLevel).isBetween(0f, 1f) }
            .verifyComplete()
    }

    @Test
    fun `warningPostConstruct emits WARN log`() {
        val logger = LoggerFactory.getLogger(NoopChatService::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        try {
            svc.warningPostConstruct()
            val warnLogs = listAppender.list.filter { it.level == Level.WARN }
            assertThat(warnLogs).isNotEmpty
        } finally {
            logger.detachAppender(listAppender)
        }
    }
}
