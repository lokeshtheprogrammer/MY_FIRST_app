package com.example.nutrifill.network.models

data class UserResponse(
    val message: String,
    val token: String? = null,
    val profile: User? = null
)