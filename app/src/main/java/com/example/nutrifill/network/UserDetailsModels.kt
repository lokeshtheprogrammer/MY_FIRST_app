package com.example.nutrifill.network

data class UserDetailsRequest(
    val userId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val activityLevel: String,
    val isPregnant: Boolean?
)

data class UserDetailsResponse(
    val success: Boolean,
    val message: String,
    val userDetails: UserDetails?
)

data class UserDetails(
    val id: String,
    val userId: String,
    val bmi: Float,
    val dailyCalories: Double
)