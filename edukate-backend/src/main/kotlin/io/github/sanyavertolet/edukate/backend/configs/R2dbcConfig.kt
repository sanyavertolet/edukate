package io.github.sanyavertolet.edukate.backend.configs

import io.github.sanyavertolet.edukate.backend.entities.Problem
import io.github.sanyavertolet.edukate.backend.entities.files.FileObjectMetadata
import io.github.sanyavertolet.edukate.common.users.UserRole
import io.github.sanyavertolet.edukate.storage.keys.FileKey
import io.r2dbc.postgresql.codec.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

@Configuration
@EnableR2dbcAuditing
class R2dbcConfig {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions =
        R2dbcCustomConversions.of(
            PostgresDialect.INSTANCE,
            listOf(
                JsonToStringListConverter(objectMapper),
                StringListToJsonConverter(objectMapper),
                JsonToSubtaskListConverter(objectMapper),
                SubtaskListToJsonConverter(objectMapper),
                JsonToUserRoleSetConverter(objectMapper),
                UserRoleSetToJsonConverter(objectMapper),
                JsonToUserRoleMapConverter(objectMapper),
                UserRoleMapToJsonConverter(objectMapper),
                JsonToLongSetConverter(objectMapper),
                LongSetToJsonConverter(objectMapper),
                JsonToFileKeyConverter(objectMapper),
                FileKeyToJsonConverter(objectMapper),
                JsonToFileObjectMetadataConverter(objectMapper),
                FileObjectMetadataToJsonConverter(objectMapper),
            ),
        )

    @ReadingConverter
    class JsonToStringListConverter(private val objectMapper: ObjectMapper) : Converter<Json, List<String>> {
        override fun convert(source: Json): List<String> = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class StringListToJsonConverter(private val objectMapper: ObjectMapper) : Converter<List<String>, Json> {
        override fun convert(source: List<String>): Json = Json.of(objectMapper.writeValueAsString(source))
    }

    @ReadingConverter
    class JsonToSubtaskListConverter(private val objectMapper: ObjectMapper) : Converter<Json, List<Problem.Subtask>> {
        override fun convert(source: Json): List<Problem.Subtask> = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class SubtaskListToJsonConverter(private val objectMapper: ObjectMapper) : Converter<List<Problem.Subtask>, Json> {
        override fun convert(source: List<Problem.Subtask>): Json = Json.of(objectMapper.writeValueAsString(source))
    }

    @ReadingConverter
    class JsonToUserRoleSetConverter(private val objectMapper: ObjectMapper) : Converter<Json, Set<UserRole>> {
        override fun convert(source: Json): Set<UserRole> = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class UserRoleSetToJsonConverter(private val objectMapper: ObjectMapper) : Converter<Set<UserRole>, Json> {
        override fun convert(source: Set<UserRole>): Json = Json.of(objectMapper.writeValueAsString(source))
    }

    @ReadingConverter
    class JsonToUserRoleMapConverter(private val objectMapper: ObjectMapper) : Converter<Json, Map<Long, UserRole>> {
        override fun convert(source: Json): Map<Long, UserRole> {
            val stringKeyMap: Map<String, UserRole> = objectMapper.readValue(source.asString())
            return stringKeyMap.mapKeys { (key, _) -> key.toLong() }
        }
    }

    @WritingConverter
    class UserRoleMapToJsonConverter(private val objectMapper: ObjectMapper) : Converter<Map<Long, UserRole>, Json> {
        override fun convert(source: Map<Long, UserRole>): Json {
            val stringKeyMap = source.mapKeys { (key, _) -> key.toString() }
            return Json.of(objectMapper.writeValueAsString(stringKeyMap))
        }
    }

    @ReadingConverter
    class JsonToLongSetConverter(private val objectMapper: ObjectMapper) : Converter<Json, Set<Long>> {
        override fun convert(source: Json): Set<Long> = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class LongSetToJsonConverter(private val objectMapper: ObjectMapper) : Converter<Set<Long>, Json> {
        override fun convert(source: Set<Long>): Json = Json.of(objectMapper.writeValueAsString(source))
    }

    @ReadingConverter
    class JsonToFileKeyConverter(private val objectMapper: ObjectMapper) : Converter<Json, FileKey> {
        override fun convert(source: Json): FileKey = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class FileKeyToJsonConverter(private val objectMapper: ObjectMapper) : Converter<FileKey, Json> {
        override fun convert(source: FileKey): Json = Json.of(objectMapper.writeValueAsString(source))
    }

    @ReadingConverter
    class JsonToFileObjectMetadataConverter(private val objectMapper: ObjectMapper) : Converter<Json, FileObjectMetadata> {
        override fun convert(source: Json): FileObjectMetadata = objectMapper.readValue(source.asString())
    }

    @WritingConverter
    class FileObjectMetadataToJsonConverter(private val objectMapper: ObjectMapper) : Converter<FileObjectMetadata, Json> {
        override fun convert(source: FileObjectMetadata): Json = Json.of(objectMapper.writeValueAsString(source))
    }
}
