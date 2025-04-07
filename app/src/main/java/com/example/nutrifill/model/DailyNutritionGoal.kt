package com.example.nutrifill.model

data class DailyNutritionGoal(
    val calories: Float = 2000f,
    val protein: Float = 50f,
    val carbohydrates: Float = 250f,
    val fats: Float = 70f
)