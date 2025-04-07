package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName

data class UserRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String? = null,  // Made name optional with default value null
    @SerializedName("role") val role: String? = "user"
)
