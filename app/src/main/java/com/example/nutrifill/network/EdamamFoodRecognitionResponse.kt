package com.example.nutrifill.network

data class EdamamFoodRecognitionResponse(
    val hints: List<FoodHint>
)

data class FoodHint(
    val food: Food
)

data class Food(
    val label: String,
    val category: String,
    val foodId: String,
    val nutrients: FoodNutrients
)

data class FoodNutrients(
    val ENERC_KCAL: Float = 0f,
    val PROCNT: Float = 0f,
    val FAT: Float = 0f,
    val CHOCDF: Float = 0f
)