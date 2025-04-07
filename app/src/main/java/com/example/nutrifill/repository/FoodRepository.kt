package com.example.nutrifill.repository

import android.content.Context
import com.example.nutrifill.api.*
import com.example.nutrifill.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.nutrifill.R
import com.example.nutrifill.models.FoodRecommendation
import com.example.nutrifill.model.FoodItem
import com.example.nutrifill.service.NutritionService
import com.example.nutrifill.data.FoodRecognitionHistory
import com.example.nutrifill.data.FoodHistoryManager

class FoodRepository(context: Context) {
    private val configManager by lazy { ConfigManager.getInstance(context.applicationContext) }
    private val foodDataService = RetrofitClient.foodDataService
    private val openFoodFactsService = RetrofitClient.openFoodFactsService
    private val openStreetMapService = RetrofitClient.openStreetMapService
    private val nutritionService = NutritionService()
    private val foodHistoryManager = FoodHistoryManager(context)

    suspend fun searchFoodByName(query: String): Result<List<FoodItem>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = configManager.getUsdaApiKey() ?: return@withContext Result.failure(
                IllegalStateException("USDA API key not found")
            )
            
            val response = foodDataService.searchFood(apiKey, query)
            if (response.isSuccessful && response.body() != null) {
                // Convert API FoodItem to model FoodItem
                val modelFoodItems = response.body()!!.foods.map { apiFoodItem ->
                    FoodItem(
                        id = apiFoodItem.fdcId,
                        name = apiFoodItem.description,
                        brand = apiFoodItem.brandOwner,
                        description = apiFoodItem.description,
                        servingSize = apiFoodItem.servingSize?.toFloat() ?: 0f,
                        servingUnit = apiFoodItem.servingSizeUnit ?: "serving",
                        ingredients = apiFoodItem.ingredients?.split(",")?.map { it.trim() },
                        // Map nutrients if available
                        calories = apiFoodItem.foodNutrients.find { it.nutrientName.contains("Energy") }?.value?.toFloat() ?: 0f,
                        protein = apiFoodItem.foodNutrients.find { it.nutrientName.contains("Protein") }?.value?.toFloat() ?: 0f,
                        carbohydrates = apiFoodItem.foodNutrients.find { it.nutrientName.contains("Carbohydrate") }?.value?.toFloat() ?: 0f,
                        fats = apiFoodItem.foodNutrients.find { it.nutrientName.contains("Total lipid") }?.value?.toFloat() ?: 0f
                    )
                }
                Result.success(modelFoodItems)
            } else {
                Result.failure(Exception("Failed to search food: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoodByBarcode(barcode: String): Result<ProductData> = withContext(Dispatchers.IO) {
        try {
            val response = openFoodFactsService.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.product)
            } else {
                Result.failure(Exception("Failed to get food by barcode: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAmmaUnavagam(query: String = "Amma Unavagam Chennai"): Result<List<Location>> = 
        withContext(Dispatchers.IO) {
            try {
                val response = openStreetMapService.searchLocations(query)
                if (response.isSuccessful && response.body() != null) {
                    val locations = response.body()!!.map { LocationMapper.fromResponse(it) }
                    Result.success(locations)
                } else {
                    Result.failure(Exception("Failed to search locations: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getAmmaUnavagamMenu(): List<FoodRecommendation> = withContext(Dispatchers.IO) {
        // For now, return a static menu. In the future, this could be fetched from a backend API
        listOf(
            FoodRecommendation("Idli", "2 pieces of soft steamed rice cakes", "₹1", portion = "2 pieces", imageResource = R.drawable.ic_food_placeholder),
            FoodRecommendation("Pongal", "Hot rice and lentil dish", "₹5", portion = "1 bowl", imageResource = R.drawable.ic_food_placeholder),
            FoodRecommendation("Sambar Rice", "Rice mixed with lentil-based vegetable stew", "₹5", portion = "1 plate", imageResource = R.drawable.ic_food_placeholder),
            FoodRecommendation("Curd Rice", "Rice mixed with yogurt", "₹3", portion = "1 plate", imageResource = R.drawable.ic_food_placeholder),
            FoodRecommendation("Chapati", "2 pieces with kurma", "₹3", portion = "2 pieces", imageResource = R.drawable.ic_food_placeholder)
        )
    }

    suspend fun saveFoodItem(foodItem: FoodItem): Result<FoodItem> = withContext(Dispatchers.IO) {
        try {
            // Get nutrition info for the food item
            val nutritionResult = nutritionService.getNutritionInfo(foodItem, "medium")
            
            if (nutritionResult.success) {
                // Update food item with nutrition info
                foodItem.apply {
                    calories = nutritionResult.nutritionInfo.calories.toFloat()
                    protein = nutritionResult.nutritionInfo.totalNutrients.protein?.quantity ?: 0f
                    carbohydrates = nutritionResult.nutritionInfo.totalNutrients.carbs?.quantity ?: 0f
                    fats = nutritionResult.nutritionInfo.totalNutrients.fat?.quantity ?: 0f
                    servingSize = nutritionResult.nutritionInfo.totalWeight
                    servingUnit = "g"
                }

                // Save to recognition history
                foodHistoryManager.saveHistory(FoodRecognitionHistory(
                    foodName = foodItem.name,
                    confidence = 1.0f, // Default confidence for manual entries
                    model = "manual"
                ))

                Result.success(foodItem)
            } else {
                Result.failure(Exception(nutritionResult.error ?: "Failed to get nutrition info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFoodHistory(): List<FoodRecognitionHistory> {
        return foodHistoryManager.getAllHistory()
    }

    fun clearFoodHistory() {
        foodHistoryManager.clearHistory()
    }
}