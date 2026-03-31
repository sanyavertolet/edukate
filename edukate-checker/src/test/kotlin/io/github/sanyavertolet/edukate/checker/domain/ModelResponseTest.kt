package io.github.sanyavertolet.edukate.checker.domain

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.sanyavertolet.edukate.checker.dtos.ModelResponse
import io.github.sanyavertolet.edukate.common.checks.CheckErrorType
import io.github.sanyavertolet.edukate.common.checks.CheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class ModelResponseTest {
    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `defaults are INTERNAL_ERROR`() {
        val response = ModelResponse()
        assertThat(response.status).isEqualTo(CheckStatus.INTERNAL_ERROR)
        assertThat(response.trustLevel).isEqualTo(0f)
        assertThat(response.errorType).isEqualTo(CheckErrorType.NONE)
        assertThat(response.explanation).isEqualTo("")
    }

    @Test
    fun `all fields survive Jackson round-trip`() {
        val original =
            ModelResponse(
                status = CheckStatus.MISTAKE,
                trustLevel = 0.75f,
                errorType = CheckErrorType.ALGEBRAIC,
                explanation = "Wrong sign",
            )
        val json = mapper.writeValueAsString(original)
        val result = mapper.readValue(json, ModelResponse::class.java)
        assertThat(result).isEqualTo(original)
    }

    @Test
    fun `missing fields fall back to defaults`() {
        val result = mapper.readValue("{}", ModelResponse::class.java)
        assertThat(result).isEqualTo(ModelResponse())
    }

    @Test
    fun `@JsonPropertyDescription is present on all fields`() {
        ModelResponse::class.memberProperties.forEach { prop ->
            val annotation = prop.javaField?.getAnnotation(JsonPropertyDescription::class.java)
            assertThat(annotation).withFailMessage("Missing @JsonPropertyDescription on field: %s", prop.name).isNotNull
            assertThat(annotation!!.value)
                .withFailMessage("Empty @JsonPropertyDescription on field: %s", prop.name)
                .isNotBlank()
        }
    }
}
