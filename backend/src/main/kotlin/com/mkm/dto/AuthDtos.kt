package com.mkm.dto

import jakarta.validation.constraints.NotBlank

// Auth DTOs
data class RegisterRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String,
    val email: String? = null
)

data class LoginRequest(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)

data class TokenResponse(
    val token: String,
    val username: String
)
