package com.example.nutrifill.service

import com.example.nutrifill.R
import com.example.nutrifill.models.DailyNutritionGoals
import com.example.nutrifill.models.FoodRecommendation
import com.example.nutrifill.models.Nutrients

class RecommendationService {
    fun getRecommendations(currentNutrients: Nutrients, targetNutrients: Nutrients): List<FoodRecommendation> {
        val recommendations = mutableListOf<FoodRecommendation>()

        // Add protein-rich foods if protein is deficient
        if (currentNutrients.protein < targetNutrients.protein * 0.8) {
            recommendations.add(createChickenBreast())
            recommendations.add(createGreekYogurt())
            recommendations.add(createSalmon())
        }

        // Add carb-rich foods if carbs are deficient
        if (currentNutrients.carbs < targetNutrients.carbs * 0.8) {
            recommendations.add(createQuinoaBowl())
            recommendations.add(createSweetPotato())
            recommendations.add(createOatmeal())
        }

        return recommendations.takeIf { it.isNotEmpty() } ?: listOf(
            createQuinoaBowl(),
            createGreekYogurt(),
            createSalmon(),
            createSweetPotato(),
            createChickenBreast(),
            createOatmeal()
        )
    }

    private fun createQuinoaBowl() = FoodRecommendation(
        name = "Quinoa Bowl",
        description = "Protein-rich ancient grain with complete amino acids",
        price = "$4.99/lb",
        portion = "1 cup cooked",
        imageResource = R.drawable.quinoa_bowl
    )

    private fun createGreekYogurt() = FoodRecommendation(
        name = "Greek Yogurt",
        description = "High protein dairy with probiotics",
        price = "$1.99/cup",
        portion = "1 cup",
        imageResource = R.drawable.greek_yogurt
    )

    private fun createSalmon() = FoodRecommendation(
        name = "Grilled Salmon",
        description = "Rich in protein and healthy omega-3 fats",
        price = "$8.99/fillet",
        portion = "6 oz fillet",
        imageResource = R.drawable.salmon
    )

    private fun createSweetPotato() = FoodRecommendation(
        name = "Sweet Potato",
        description = "Complex carbs with vitamins and fiber",
        price = "$1.49/lb",
        portion = "1 medium",
        imageResource = R.drawable.sweet_potato
    )

    private fun createChickenBreast() = FoodRecommendation(
        name = "Grilled Chicken Breast",
        description = "Lean protein source, low in fat",
        price = "$5.99/lb",
        portion = "6 oz",
        imageResource = R.drawable.chicken_breast
    )

    private fun createOatmeal() = FoodRecommendation(
        name = "Oatmeal",
        description = "Heart-healthy whole grain breakfast",
        price = "$3.99/lb",
        portion = "1 cup cooked",
        imageResource = R.drawable.oatmeal
    )

    fun getMealPlanSuggestions(currentNutrients: Nutrients, dailyGoals: DailyNutritionGoals): List<String> {
        val suggestions = mutableListOf<String>()

        // Calculate remaining nutrients needed
        val remainingCalories = dailyGoals.targetNutrients.calories - currentNutrients.calories
        val remainingProtein = dailyGoals.targetNutrients.protein - currentNutrients.protein
        val remainingCarbs = dailyGoals.targetNutrients.carbs - currentNutrients.carbs

        if (remainingCalories > 0) {
            suggestions.add("You need ${remainingCalories.toInt()} more calories today")
        }
        if (remainingProtein > 0) {
            suggestions.add("Try to include ${remainingProtein.toInt()}g more protein in your meals")
        }
        if (remainingCarbs > 0) {
            suggestions.add("Add ${remainingCarbs.toInt()}g more healthy carbs to your diet")
        }

        return suggestions
    }
}