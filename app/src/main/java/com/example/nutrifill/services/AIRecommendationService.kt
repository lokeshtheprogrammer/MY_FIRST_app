package com.example.nutrifill.services

import android.content.Context
import android.content.SharedPreferences
import com.example.nutrifill.models.Nutrients
import com.example.nutrifill.models.ScanHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.abs

class AIRecommendationService(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("NutriFillPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Get personalized meal recommendations based on user's nutritional history and goals
    fun getPersonalizedMealRecommendations(): List<String> {
        val nutritionHistory = getNutritionHistory()
        val userPreferences = getUserPreferences()
        
        // Analyze nutrition patterns
        val averageNutrients = calculateAverageNutrients(nutritionHistory)
        val recommendedFoods = mutableListOf<String>()

        // Basic recommendation logic based on nutritional needs
        when {
            averageNutrients.protein < 50 -> recommendedFoods.add("High-protein foods like chicken, fish, or legumes")
            averageNutrients.carbs < 225 -> recommendedFoods.add("Complex carbohydrates like whole grains and sweet potatoes")
            averageNutrients.fat > 70 -> recommendedFoods.add("Low-fat alternatives and lean proteins")
        }

        return recommendedFoods
    }

    // Suggest smart portion sizes based on user's goals and history
    fun suggestPortionSize(foodName: String, baseCalories: Int): Double {
        val userBMI = getUserBMI()
        val activityLevel = getUserActivityLevel()
        
        // Basic portion adjustment logic
        var portionMultiplier = 1.0
        when {
            userBMI > 25 -> portionMultiplier = 0.8 // Reduce portions for weight loss
            userBMI < 18.5 -> portionMultiplier = 1.2 // Increase portions for weight gain
            activityLevel == "high" -> portionMultiplier = 1.3 // More food for active users
        }

        return portionMultiplier
    }

    // Analyze nutritional trends and provide insights
    fun analyzeNutritionalTrends(): Map<String, String> {
        val history = getNutritionHistory()
        val trends = mutableMapOf<String, String>()

        if (history.isNotEmpty()) {
            val caloriesTrend = analyzeTrend(history.map { it._nutrients.calories })
            val proteinTrend = analyzeTrend(history.map { it._nutrients.protein.toInt() })

            trends["calories"] = when {
                caloriesTrend > 0.1 -> "Your calorie intake is trending upward"
                caloriesTrend < -0.1 -> "Your calorie intake is trending downward"
                else -> "Your calorie intake is stable"
            }

            trends["protein"] = when {
                proteinTrend > 0.1 -> "Your protein intake is improving"
                proteinTrend < -0.1 -> "Consider increasing your protein intake"
                else -> "Your protein intake is consistent"
            }
        }

        return trends
    }

    // Suggest healthy food substitutions
    fun suggestHealthySubstitutions(foodName: String): List<String> {
        // Basic substitution suggestions based on food categories
        return when {
            foodName.contains("rice", ignoreCase = true) -> 
                listOf("Quinoa", "Cauliflower rice", "Brown rice")
            foodName.contains("pasta", ignoreCase = true) -> 
                listOf("Zucchini noodles", "Whole grain pasta", "Spaghetti squash")
            foodName.contains("bread", ignoreCase = true) -> 
                listOf("Whole grain bread", "Ezekiel bread", "Lettuce wrap")
            else -> listOf("No specific substitutions available")
        }
    }

    // Helper functions
    private fun getNutritionHistory(): List<ScanHistoryItem> {
        val historyJson = sharedPreferences.getString("nutrition_history", "[]")
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val historyList: List<Map<String, Any>> = gson.fromJson(historyJson, type) ?: listOf()

        return historyList.mapIndexed { index, item ->
            val nutrientsMap = item["nutrients"] as? Map<String, Any> ?: mapOf()
            ScanHistoryItem(
                id = index.toString(),
                foodName = item["foodName"] as String,
                _nutrients = Nutrients(
                    calories = (nutrientsMap["calories"] as? Double)?.toInt() ?: 0,
                    protein = (nutrientsMap["protein"] as? Double)?.toFloat() ?: 0f,
                    carbs = (nutrientsMap["carbs"] as? Double)?.toFloat() ?: 0f,
                    fat = (nutrientsMap["fat"] as? Double)?.toFloat() ?: 0f
                ),
                timestamp = (item["timestamp"] as? Double)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }

    private fun calculateAverageNutrients(history: List<ScanHistoryItem>): Nutrients {
        if (history.isEmpty()) return Nutrients(0, 0f, 0f, 0f)

        val totalNutrients = history.fold(Nutrients(0, 0f, 0f, 0f)) { acc, item ->
            Nutrients(
                calories = acc.calories + item._nutrients.calories,
                protein = acc.protein + item._nutrients.protein,
                carbs = acc.carbs + item._nutrients.carbs,
                fat = acc.fat + item._nutrients.fat
            )
        }

        return Nutrients(
            calories = totalNutrients.calories / history.size,
            protein = totalNutrients.protein / history.size,
            carbs = totalNutrients.carbs / history.size,
            fat = totalNutrients.fat / history.size
        )
    }

    private fun analyzeTrend(values: List<Int>): Double {
        if (values.size < 2) return 0.0
        val firstHalf = values.subList(0, values.size / 2).average()
        val secondHalf = values.subList(values.size / 2, values.size).average()
        return (secondHalf - firstHalf) / firstHalf
    }

    private fun getUserPreferences(): Map<String, Any> {
        // Placeholder for user preferences
        return mapOf(
            "dietaryRestrictions" to listOf<String>(),
            "allergies" to listOf<String>(),
            "preferredCuisines" to listOf<String>()
        )
    }

    private fun getUserBMI(): Double {
        return sharedPreferences.getFloat("user_bmi", 22.0f).toDouble()
    }

    private fun getUserActivityLevel(): String {
        return sharedPreferences.getString("activity_level", "moderate") ?: "moderate"
    }
}