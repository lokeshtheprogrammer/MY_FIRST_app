package com.example.nutrifill.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.example.nutrifill.model.Nutrient

@Parcelize
data class DailyNutritionGoals(
    val targetCalories: Int = 2000,
    val targetProtein: Float = 50f,
    val targetCarbs: Float = 250f,
    val targetFat: Float = 70f,
    val currentCalories: Int = 0,
    val currentProtein: Float = 0f,
    val currentCarbs: Float = 0f,
    val currentFat: Float = 0f,
    val nutrients: List<Nutrient> = listOf(
        Nutrient("Protein", targetProtein, "g"),
        Nutrient("Carbohydrates", targetCarbs, "g"),
        Nutrient("Fat", targetFat, "g")
    )
) : Parcelable {
    val remainingCalories: Int
        get() = targetCalories - currentCalories
    
    val remainingProtein: Float
        get() = targetProtein - currentProtein
        
    val remainingCarbs: Float
        get() = targetCarbs - currentCarbs
        
    val remainingFat: Float
        get() = targetFat - currentFat

    fun isGoalMet(nutrientType: String? = null): Boolean {
        return when (nutrientType?.toLowerCase()) {
            null -> currentCalories >= targetCalories &&
                   currentProtein >= targetProtein &&
                   currentCarbs >= targetCarbs &&
                   currentFat >= targetFat
            "calories" -> currentCalories >= targetCalories
            "protein" -> currentProtein >= targetProtein
            "carbs" -> currentCarbs >= targetCarbs
            "fat" -> currentFat >= targetFat
            else -> false
        }
    }

    fun getProgressPercentage(nutrientType: String? = null): Map<String, Float> {
        return when (nutrientType?.toLowerCase()) {
            null -> mapOf(
                "calories" to (currentCalories.toFloat() / targetCalories * 100),
                "protein" to (currentProtein / targetProtein * 100),
                "carbs" to (currentCarbs / targetCarbs * 100),
                "fat" to (currentFat / targetFat * 100)
            )
            "calories" -> mapOf("calories" to (currentCalories.toFloat() / targetCalories * 100))
            "protein" -> mapOf("protein" to (currentProtein / targetProtein * 100))
            "carbs" -> mapOf("carbs" to (currentCarbs / targetCarbs * 100))
            "fat" -> mapOf("fat" to (currentFat / targetFat * 100))
            else -> emptyMap()
        }
    }
}