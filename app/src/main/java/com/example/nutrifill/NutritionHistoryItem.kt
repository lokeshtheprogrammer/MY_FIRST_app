package com.example.nutrifill

data class NutritionHistoryItem(
    val foodName: String,
    val calories: Int,
    val carbs: Double,
    val protein: Double,
    val fats: Double,
    val timestamp: String
)
