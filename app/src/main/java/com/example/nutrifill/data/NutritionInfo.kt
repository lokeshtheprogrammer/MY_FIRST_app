data class NutritionInfo(
    val foodName: String,
    val calories: Double,
    val proteins: Double,
    val carbs: Double,
    val fats: Double,
    val servingSize: String,
    val vitamins: List<Vitamin> = emptyList(),
    val minerals: List<Mineral> = emptyList()
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