package io.github.sanyavertolet.edukate.backend.savelisteners

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.utils.SemVerUtils
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent
import org.springframework.stereotype.Component

@Component
class ProblemBeforeSaveListener : AbstractMongoEventListener<Problem>() {
    override fun onBeforeSave(event: BeforeSaveEvent<Problem>) {
        val entity = event.source
        val document = requireNotNull(event.document)

        val problemId = entity.id
        val (major, minor, patch) = SemVerUtils.parse(problemId)

        document[SemVerUtils.MAJOR_FIELD_NAME] = major
        document[SemVerUtils.MINOR_FIELD_NAME] = minor
        document[SemVerUtils.PATCH_FIELD_NAME] = patch
    }
}
