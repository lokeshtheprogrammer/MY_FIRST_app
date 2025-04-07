package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("token") val token: String = "",
    @SerializedName("profile") val profile: UserProfile? = null,
    @SerializedName("message") val message: String = ""
)

