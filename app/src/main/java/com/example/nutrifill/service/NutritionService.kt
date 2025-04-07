package com.example.nutrifill.service

import android.util.Log
import com.example.nutrifill.BuildConfig
import com.example.nutrifill.data.NutrientFoodDatabase
import com.example.nutrifill.model.FoodItem
import com.example.nutrifill.network.EdamamApiService
import com.example.nutrifill.network.EdamamNutritionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.await

data class NutritionInfo(
    val calories: Int = 0,
    val totalWeight: Float = 0f,
    val totalNutrients: Nutrients = Nutrients()
)

data class Nutrients(
    val protein: Nutrient? = null,
    val fat: Nutrient? = null,
    val carbs: Nutrient? = null,
    val fiber: Nutrient? = null
)

data class Nutrient(
    val label: String = "",
    val quantity: Float = 0f,
    val unit: String = ""
)

data class NutritionResult(
    val success: Boolean,
    val nutritionInfo: NutritionInfo,
    val error: String? = null
)

class NutritionService {
    private val TAG = "NutritionService"
    private val BASE_URL = "https://api.edamam.com/api/nutrition-data/"
    private val APP_ID = BuildConfig.EDAMAM_APP_ID
    private val APP_KEY = BuildConfig.EDAMAM_APP_KEY

    fun getFoodsByNutrient(nutrient: String): List<FoodItem> {
        return NutrientFoodDatabase.nutrientFoodMap[nutrient.lowercase()] ?: emptyList()
    }

    private val edamamApi: EdamamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EdamamApiService::class.java)
    }

    suspend fun getNutritionInfo(foodItem: FoodItem, portion: String): NutritionResult = withContext(Dispatchers.IO) {
        try {
            val response = edamamApi.getNutritionData(
                appId = APP_ID,
                appKey = APP_KEY,
                ingredient = "${getPortionWeight(portion)} ${foodItem.name}"
            ).await()

            val nutritionInfo = NutritionInfo(
                calories = response.calories.toInt(),
                totalWeight = response.totalWeight,
                totalNutrients = Nutrients(
                    protein = response.totalNutrients.PROCNT?.let { Nutrient(it.label, it.quantity, it.unit) },
                    fat = response.totalNutrients.FAT?.let { Nutrient(it.label, it.quantity, it.unit) },
                    carbs = response.totalNutrients.CHOCDF?.let { Nutrient(it.label, it.quantity, it.unit) },
                    fiber = response.totalNutrients.FIBTG?.let { Nutrient(it.label, it.quantity, it.unit) }
                )
            )

            foodItem.calories = nutritionInfo.calories.toFloat()
            foodItem.protein = nutritionInfo.totalNutrients.protein?.quantity ?: 0f
            foodItem.carbohydrates = nutritionInfo.totalNutrients.carbs?.quantity ?: 0f
            foodItem.fats = nutritionInfo.totalNutrients.fat?.quantity ?: 0f
            foodItem.servingSize = getPortionWeight(portion).toFloat()
            foodItem.servingUnit = "g"

            NutritionResult(success = true, nutritionInfo = nutritionInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nutrition info: ${e.message}")
            NutritionResult(
                success = false,
                nutritionInfo = NutritionInfo(),
                error = e.message ?: "Unknown error occurred"
            )
        }
    }

    private fun getPortionWeight(portion: String): Int {
        return when (portion.lowercase()) {
            "small" -> 100
            "medium" -> 200
            "large" -> 300
            else -> 200
        }
    }
}