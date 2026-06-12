package io.github.sanyavertolet.edukate.backend.dtos

import io.github.sanyavertolet.edukate.common.users.UserRole
import jakarta.validation.constraints.NotNull

data class SetMemberRoleRequest(@field:NotNull val role: UserRole)
