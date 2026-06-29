package com.mkm.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val content: String,
    val isPublic: Boolean,
    val ownerUsername: String,
    val updatedAt: String
)
