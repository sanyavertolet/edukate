package io.github.sanyavertolet.edukate.backend.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("books")
data class Book(
    @Id val id: Long? = null,
    val slug: String,
    val subject: String,
    val title: String,
    val citation: String,
    val description: String? = null,
)
