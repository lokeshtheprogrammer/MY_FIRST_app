package com.example.nutrifill.models

data class DailyNutritionGoals(
    val targetNutrients: Nutrients,
    var currentNutrients: Nutrients
) {
    val remainingCalories: Int
        get() = targetNutrients.calories - currentNutrients.calories
    
    val remainingProtein: Float
        get() = targetNutrients.protein - currentNutrients.protein
        
    val remainingCarbs: Float
        get() = targetNutrients.carbs - currentNutrients.carbs
        
    val remainingFat: Float
        get() = targetNutrients.fat - currentNutrients.fat
}