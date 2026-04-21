package io.github.sanyavertolet.edukate.backend.dtos

import jakarta.validation.constraints.NotEmpty

data class ChangeProblemSetProblemsRequest(@field:NotEmpty val problemKeys: List<String>)
