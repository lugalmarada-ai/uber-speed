package com.uberspeed.client.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)
