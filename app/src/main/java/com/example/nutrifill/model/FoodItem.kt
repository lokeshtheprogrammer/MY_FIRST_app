package com.example.nutrifill.model

data class FoodItem(
    val id: String = "",
    val name: String,
    var calories: Float = 0f,
    var protein: Float = 0f,
    var carbohydrates: Float = 0f,
    var fats: Float = 0f,
    var fiber: Float = 0f,
    var servingSize: Float = 0f,
    var servingUnit: String = "serving",
    val imageUrl: String? = null,
    val description: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val ingredients: List<String>? = null,
    val allergens: List<String>? = null,
    val timestamp: String? = null
)