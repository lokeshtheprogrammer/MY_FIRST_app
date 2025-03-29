package com.example.nutrifill.models

data class FoodDetails(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val fiber: Float = 0f,
    val sugars: Float = 0f,
    val sodium: Int = 0,
    val vitamins: List<Vitamin> = emptyList(),
    val confidence: Float = 0f
)

data class Vitamin(
    val name: String,
    val amount: Float,
    val unit: String
)