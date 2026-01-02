package com.uberspeed.client.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val token: String? = null
)
