package com.example.nutrifill.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodDataService {
    @GET("food/search")
    suspend fun searchFood(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Response<FoodDataResponse>

    @GET("food/details")
    suspend fun getFoodDetails(
        @Query("api_key") apiKey: String,
        @Query("fdcId") fdcId: String
    ): Response<FoodDetailsResponse>
}

data class FoodDataResponse(
    val foods: List<FoodItem>,
    val totalHits: Int,
    val currentPage: Int,
    val totalPages: Int
)

data class FoodItem(
    val fdcId: String,
    val description: String,
    val brandOwner: String?,
    val ingredients: String?,
    val servingSize: Double?,
    val servingSizeUnit: String?,
    val foodNutrients: List<FoodNutrient>
)

data class FoodNutrient(
    val nutrientId: Int,
    val nutrientName: String,
    val value: Double,
    val unitName: String
)

data class FoodDetailsResponse(
    val fdcId: String,
    val description: String,
    val foodNutrients: List<FoodNutrient>,
    val ingredients: String?,
    val servingSize: Double?,
    val servingSizeUnit: String?
)