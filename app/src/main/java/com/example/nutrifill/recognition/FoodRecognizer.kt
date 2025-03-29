class FoodRecognizer {
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )
    private val api = RetrofitClient.instance

    // Add food categories for better recognition
    private val foodCategories = setOf(
        "food", "fruit", "vegetable", "meat", "beverage",
        "dessert", "bread", "meal", "dish", "snack", "dairy"
    )

    suspend fun recognizeFood(imageBytes: ByteArray): FoodDetails {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromByteArray(
                    imageBytes,
                    480, 360, 0,
                    InputImage.IMAGE_FORMAT_JPEG
                )
                
                val labels = Tasks.await(imageLabeler.process(image))
                val foodLabel = labels.firstOrNull { label ->
                    label.confidence > 0.7 && isFoodCategory(label.text)
                }

                if (foodLabel != null) {
                    // Use your API key for nutrition data
                    val nutritionDetails = api.getNutritionDetails(foodLabel.text)
                    mapToFoodDetails(nutritionDetails)
                } else {
                    FoodDetails("No food detected", 0, 0f, 0f, 0f)
                }
            } catch (e: Exception) {
                Log.e("FoodRecognizer", "Error: ${e.message}")
                FoodDetails("Error scanning food", 0, 0f, 0f, 0f)
            }
        }
    }
}

data class FoodDetails(
    val name: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float
)