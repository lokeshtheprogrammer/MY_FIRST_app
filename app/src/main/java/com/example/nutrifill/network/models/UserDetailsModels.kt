package com.example.nutrifill.network.models

data class UserDetailsRequest(
    val userId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val activityLevel: String,
    val isPregnant: Boolean? = null
)

data class UserDetailsResponse(
    val success: Boolean,
    val message: String,
    val userDetails: UserDetails? = null
)

data class UserDetails(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val activityLevel: String,
    val bmi: Float,
    val dailyCalories: Double,
    val isPregnant: Boolean? = null
)