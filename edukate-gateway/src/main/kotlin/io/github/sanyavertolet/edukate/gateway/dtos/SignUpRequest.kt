package io.github.sanyavertolet.edukate.gateway.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 15)
    @field:Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_-]+[a-zA-Z0-9]$")
    val username: String,
    @field:NotBlank @field:Size(min = 6, max = 128) val password: String,
    @field:NotBlank @field:Email val email: String,
) {
    override fun toString(): String = "SignUpRequest(username=$username, email=$email)"
}
