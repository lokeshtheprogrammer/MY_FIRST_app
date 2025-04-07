package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("name") val name: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("age") val age: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("height") val height: Float? = null,
    @SerializedName("weight") val weight: Float? = null,
    @SerializedName("activityLevel") val activityLevel: String? = null,
    @SerializedName("isPregnant") val isPregnant: Boolean = false,
    @SerializedName("dietaryPreferences") val dietaryPreferences: List<String> = emptyList(),
    @SerializedName("allergies") val allergies: List<String> = emptyList(),
    @SerializedName("lastUpdated") val lastUpdated: String? = null
)