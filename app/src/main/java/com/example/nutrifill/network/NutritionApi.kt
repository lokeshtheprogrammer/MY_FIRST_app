package com.example.nutrifill.network

import com.example.nutrifill.models.FoodDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NutritionApi {
    @GET("nutrition/{foodId}")
    suspend fun getFoodNutrition(@Path("foodId") foodId: String): Response<FoodDetails>
}