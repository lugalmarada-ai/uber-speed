package com.uberspeed.client.data.model

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Register Request
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("role") val role: String = "user"
)

// Auth Response (Matches Backend)
data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: UserDto? = null
)

// User DTO
data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String
) {
    // Helper helper for accessing token if it was inside user properly in older versions
    // or just convenience.
    val token: String? = null // Dummy to satisfy potential legacy code usage user.token?
}
