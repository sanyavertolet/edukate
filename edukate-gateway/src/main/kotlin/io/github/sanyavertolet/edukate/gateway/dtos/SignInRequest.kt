package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignInRequest(
    @field:NotBlank val username: String,
    @field:NotBlank @field:Size(max = 1000) val password: String,
) {
    override fun toString(): String = "SignInRequest(username=$username)"
}
