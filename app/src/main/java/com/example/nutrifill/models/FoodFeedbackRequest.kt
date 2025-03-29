package com.example.nutrifill.models

data class FoodFeedbackRequest(
    val foodId: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)