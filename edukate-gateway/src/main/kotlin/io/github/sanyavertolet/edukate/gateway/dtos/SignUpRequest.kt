package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignUpRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
    @field:NotBlank @field:Email val email: String,
) {
    override fun toString(): String = "SignUpRequest(username=$username, email=$email)"
}
