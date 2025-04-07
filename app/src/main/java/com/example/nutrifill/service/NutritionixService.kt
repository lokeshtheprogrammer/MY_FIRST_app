package com.example.nutrifill.service

import com.example.nutrifill.network.RetrofitClient
import com.example.nutrifill.network.ApiConfig
import com.example.nutrifill.network.NutritionixNutrientResponse
import com.example.nutrifill.models.Nutrients
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NutritionixService {
    private val nutritionixApi = RetrofitClient.nutritionixService

    fun getNutritionInfo(
        query: String,
        onSuccess: (Nutrients) -> Unit,
        onError: (String) -> Unit
    ) {
        nutritionixApi.getNutrients(query).enqueue(object : Callback<NutritionixNutrientResponse> {
            override fun onResponse(
                call: Call<NutritionixNutrientResponse>,
                response: Response<NutritionixNutrientResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { nutritionResponse ->
                        val firstFood = nutritionResponse.foods.firstOrNull()
                        if (firstFood != null) {
                            val nutrients = Nutrients(
                                calories = firstFood.nf_calories.toInt(),
                                protein = firstFood.nf_protein.toFloat(),
                                carbs = firstFood.nf_total_carbohydrate.toFloat(),
                                fat = firstFood.nf_total_fat.toFloat()
                            )
                            onSuccess(nutrients)
                        } else {
                            onError("No nutrition information found")
                        }
                    } ?: onError("Empty response from server")
                } else {
                    onError("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NutritionixNutrientResponse>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }

    fun getNutritionInfoFromText(
        foodText: String,
        onSuccess: (Nutrients) -> Unit,
        onError: (String) -> Unit
    ) {
        val queryMap = mapOf("query" to foodText)
        nutritionixApi.postNutrients(queryMap).enqueue(object : Callback<NutritionixNutrientResponse> {
            override fun onResponse(
                call: Call<NutritionixNutrientResponse>,
                response: Response<NutritionixNutrientResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { nutritionResponse ->
                        val firstFood = nutritionResponse.foods.firstOrNull()
                        if (firstFood != null) {
                            val nutrients = Nutrients(
                                calories = firstFood.nf_calories.toInt(),
                                protein = firstFood.nf_protein.toFloat(),
                                carbs = firstFood.nf_total_carbohydrate.toFloat(),
                                fat = firstFood.nf_total_fat.toFloat()
                            )
                            onSuccess(nutrients)
                        } else {
                            onError("No nutrition information found")
                        }
                    } ?: onError("Empty response from server")
                } else {
                    onError("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<NutritionixNutrientResponse>, t: Throwable) {
                onError("Network error: ${t.message}")
            }
        })
    }
}