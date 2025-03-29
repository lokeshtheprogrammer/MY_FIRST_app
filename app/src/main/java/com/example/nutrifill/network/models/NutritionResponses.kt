data class NutritionDetailsResponse(
    val food: FoodDetails,
    val success: Boolean = true,
    val message: String? = null
)

data class FoodDetails(
    val name: String,
    val nutritionInfo: NutritionInfo,
    val servingSize: String? = null,
    val category: String? = null
)

data class NutritionInfo(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val fiber: Double? = null,
    val vitamins: List<Vitamin>? = null,
    val minerals: List<Mineral>? = null
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