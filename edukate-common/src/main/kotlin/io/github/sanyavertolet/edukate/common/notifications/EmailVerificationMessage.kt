package io.github.sanyavertolet.edukate.common.notifications

import java.util.UUID

data class EmailVerificationMessage(override val toEmail: String, override val token: UUID, override val username: String) :
    BaseEmailMessage
