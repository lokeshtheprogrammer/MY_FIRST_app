package com.example.nutrifill.data

import com.example.nutrifill.models.FoodDetails
import com.example.nutrifill.network.NutritionApi
import com.example.nutrifill.network.RetrofitClient

class NutritionDatabase {
    private val nutritionApi = RetrofitClient.instance.create(NutritionApi::class.java)
    private val localDatabase = mutableListOf<FoodDetails>()

    suspend fun getFoodDetails(foodId: String): FoodDetails? {
        return try {
            val response = nutritionApi.getFoodNutrition(foodId)
            if (response.isSuccessful) {
                response.body()?.also { localDatabase.add(it) }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}