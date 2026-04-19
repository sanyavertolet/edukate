package io.github.sanyavertolet.edukate.gateway.configs

import io.github.sanyavertolet.edukate.common.users.UserRole
import io.r2dbc.postgresql.codec.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

@Configuration
class R2dbcConfig {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions =
        R2dbcCustomConversions.of(
            PostgresDialect.INSTANCE,
            listOf(JsonToUserRoleSetConverter(objectMapper), UserRoleSetToJsonConverter(objectMapper)),
        )

    @ReadingConverter
    class JsonToUserRoleSetConverter(private val objectMapper: ObjectMapper) : Converter<Json, Set<UserRole>> {
        override fun convert(source: Json): Set<UserRole> = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class UserRoleSetToJsonConverter(private val objectMapper: ObjectMapper) : Converter<Set<UserRole>, Json> {
        override fun convert(source: Set<UserRole>): Json = Json.of(objectMapper.writeValueAsString(source))
    }
}
