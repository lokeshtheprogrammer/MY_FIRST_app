package com.example.nutrifill.model

data class MealPlan(
    val id: Int,
    val name: String,
    val foods: List<FoodItem>,
    val totalNutrients: Map<String, Double>
)