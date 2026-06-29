package com.mkm.android.model

import com.google.gson.annotations.SerializedName

data class Document(
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val isPublic: Boolean = false,
    val ownerUsername: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val email: String? = null)
data class TokenResponse(val token: String, val username: String)
data class DocumentRequest(val title: String, val content: String, val isPublic: Boolean = false)
