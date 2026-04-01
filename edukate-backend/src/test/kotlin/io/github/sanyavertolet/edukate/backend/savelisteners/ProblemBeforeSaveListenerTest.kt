package io.github.sanyavertolet.edukate.backend.savelisteners

import io.github.sanyavertolet.edukate.backend.BackendFixtures
import io.github.sanyavertolet.edukate.backend.utils.SemVerUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent

class ProblemBeforeSaveListenerTest {
    private val listener = ProblemBeforeSaveListener()

    @Test
    fun `onBeforeSaveParsesAndPopulatesFields`() {
        val problem = BackendFixtures.problem(id = "2.3.4")
        val document = Document()
        val event = BeforeSaveEvent(problem, document, "problems")

        listener.onBeforeSave(event)

        assertThat(document[SemVerUtils.MAJOR_FIELD_NAME]).isEqualTo(2)
        assertThat(document[SemVerUtils.MINOR_FIELD_NAME]).isEqualTo(3)
        assertThat(document[SemVerUtils.PATCH_FIELD_NAME]).isEqualTo(4)
    }

    @Test
    fun `onBeforeSaveInvalidIdThrows`() {
        val problem = BackendFixtures.problem(id = "not-semver")
        val document = Document()
        val event = BeforeSaveEvent(problem, document, "problems")

        assertThatThrownBy { listener.onBeforeSave(event) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
