package com.example.nutrifill.network.models

data class UserRequest(
    val email: String,
    val password: String,
    val name: String? = null
)