package com.example.nutrifill.network

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val profile: UserProfile? = null
)

