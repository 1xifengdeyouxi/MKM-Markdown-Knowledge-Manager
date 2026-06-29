package com.mkm.dto

import com.mkm.model.Document
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class DocumentRequest(
    @field:NotBlank val title: String,
    val content: String = "",
    val isPublic: Boolean = false
)

data class DocumentDto(
    val id: Long,
    val title: String,
    val content: String,
    val isPublic: Boolean,
    val ownerUsername: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(document: Document) = DocumentDto(
            id = document.id,
            title = document.title,
            content = document.content,
            isPublic = document.isPublic,
            ownerUsername = document.owner.username,
            createdAt = document.createdAt,
            updatedAt = document.updatedAt
        )
    }
}
