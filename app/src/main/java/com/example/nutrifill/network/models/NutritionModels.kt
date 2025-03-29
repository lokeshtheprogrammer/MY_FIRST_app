package com.example.nutrifill.network.models

data class NutritionScanRequest(
    val userId: String,
    val imageBase64: String
)

data class NutritionScanResponse(
    val success: Boolean,
    val message: String,
    val nutritionInfo: NutritionInfo? = null
)

data class NutritionInfo(
    val foodName: String,
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val fats: Double,
    val vitamins: List<Vitamin>,
    val minerals: List<Mineral>
)

data class Vitamin(
    val name: String,
    val amount: Double,
    val unit: String
)

data class Mineral(
    val name: String,
    val amount: Double,
    val unit: String
)

data class NutritionSummaryResponse(
    val dailyCalories: Double,
    val consumedCalories: Double,
    val remainingCalories: Double,
    val meals: List<Meal>,
    val macronutrients: Macronutrients
)

data class Meal(
    val id: String,
    val name: String,
    val calories: Double,
    val timeConsumed: String,
    val nutrients: Macronutrients
)

data class Macronutrients(
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val fiber: Double
)

// Add these new data classes
data class MealPlan(
    val id: String,
    val userId: String,
    val date: String,
    val meals: List<PlannedMeal>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double
)

data class PlannedMeal(
    val timeSlot: String,
    val foods: List<FoodItem>,
    val totalCalories: Double
)

data class WaterIntake(
    val date: String,
    val amount: Double,
    val unit: String
)

data class NutritionReport(
    val startDate: String,
    val endDate: String,
    val averageCalories: Double,
    val averageProtein: Double,
    val averageCarbs: Double,
    val averageFat: Double,
    val waterIntake: List<WaterIntake>
)