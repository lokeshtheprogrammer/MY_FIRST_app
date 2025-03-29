data class NutritionResponse(
    val foodName: String,
    val nutrients: NutrientInfo
)

data class NutrientInfo(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val fiber: Double?,
    val vitamins: List<VitaminInfo>?
)