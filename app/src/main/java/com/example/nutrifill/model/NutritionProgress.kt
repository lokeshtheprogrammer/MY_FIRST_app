package com.example.nutrifill.model

class NutritionProgress {
    var consumedCalories: Float = 0f
    var consumedProtein: Float = 0f
    var consumedCarbohydrates: Float = 0f
    var consumedFats: Float = 0f
    val foodItems = mutableListOf<FoodItem>()

    fun addFoodItem(item: FoodItem) {
        foodItems.add(item)
        consumedCalories += item.calories
        consumedProtein += item.protein
        consumedCarbohydrates += item.carbohydrates
        consumedFats += item.fats
    }

    fun calculateDeficiency(goal: DailyNutritionGoal): Map<String, Float> {
        return mapOf(
            "calories" to maxOf(0f, goal.calories - consumedCalories),
            "protein" to maxOf(0f, goal.protein - consumedProtein),
            "carbohydrates" to maxOf(0f, goal.carbohydrates - consumedCarbohydrates),
            "fats" to maxOf(0f, goal.fats - consumedFats)
        )
    }
}