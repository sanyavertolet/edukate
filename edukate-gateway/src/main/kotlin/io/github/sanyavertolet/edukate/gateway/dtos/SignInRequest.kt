package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.NotBlank

data class SignInRequest(@field:NotBlank val username: String, @field:NotBlank val password: String) {
    override fun toString(): String = "SignInRequest(username=$username)"
}
