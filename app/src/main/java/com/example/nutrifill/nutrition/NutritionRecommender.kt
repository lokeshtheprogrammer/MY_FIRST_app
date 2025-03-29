class NutritionRecommender {
    fun generateRecommendations(
        bmi: Float,
        dailyCalories: Double,
        consumedCalories: Double,
        macronutrients: Macronutrients
    ): NutritionRecommendations {
        val recommendations = mutableListOf<FoodRecommendation>()
        val deficiencies = mutableListOf<String>()

        // Calculate remaining calories
        val remainingCalories = dailyCalories - consumedCalories
        
        // BMI-based recommendations
        when {
            bmi < 18.5 -> {
                recommendations.add(FoodRecommendation("High-calorie foods", listOf(
                    "Nuts and seeds (600 kcal/100g)",
                    "Avocado (160 kcal/100g)",
                    "Whole grain pasta (350 kcal/100g)"
                )))
                deficiencies.add("Caloric intake")
            }
            bmi > 25 -> {
                recommendations.add(FoodRecommendation("Low-calorie, nutrient-dense foods", listOf(
                    "Leafy greens (25 kcal/100g)",
                    "Lean chicken breast (165 kcal/100g)",
                    "Fish (150 kcal/100g)"
                )))
            }
        }

        // Macronutrient recommendations
        val idealProteinRatio = 0.3 // 30% of calories from protein
        val idealCarbsRatio = 0.5 // 50% of calories from carbs
        val idealFatsRatio = 0.2 // 20% of calories from fats

        val currentProteinRatio = (macronutrients.protein * 4) / dailyCalories
        val currentCarbsRatio = (macronutrients.carbs * 4) / dailyCalories
        val currentFatsRatio = (macronutrients.fats * 9) / dailyCalories

        if (currentProteinRatio < idealProteinRatio) {
            recommendations.add(FoodRecommendation("Protein-rich foods", listOf(
                "Chicken breast (26g protein/100g)",
                "Greek yogurt (10g protein/100g)",
                "Eggs (13g protein/100g)"
            )))
            deficiencies.add("Protein")
        }

        if (currentCarbsRatio < idealCarbsRatio) {
            recommendations.add(FoodRecommendation("Complex carbohydrates", listOf(
                "Brown rice (23g carbs/100g)",
                "Sweet potatoes (20g carbs/100g)",
                "Quinoa (21g carbs/100g)"
            )))
            deficiencies.add("Carbohydrates")
        }

        if (currentFatsRatio < idealFatsRatio) {
            recommendations.add(FoodRecommendation("Healthy fats", listOf(
                "Avocado (15g fat/100g)",
                "Olive oil (100g fat/100g)",
                "Almonds (50g fat/100g)"
            )))
            deficiencies.add("Healthy fats")
        }

        return NutritionRecommendations(
            recommendations = recommendations,
            deficiencies = deficiencies,
            remainingCalories = remainingCalories.toInt()
        )
    }
}

data class NutritionRecommendations(
    val recommendations: List<FoodRecommendation>,
    val deficiencies: List<String>,
    val remainingCalories: Int
)

data class FoodRecommendation(
    val category: String,
    val foods: List<String>
)