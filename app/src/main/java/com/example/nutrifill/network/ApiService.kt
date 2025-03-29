package com.example.nutrifill.network

import com.example.nutrifill.network.models.*
import com.example.nutrifill.network.models.NutritionDetailsResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Keep only the interface definition
    @POST("auth/login")
    suspend fun login(@Body request: UserRequest): UserResponse

    @POST("auth/register")
    suspend fun register(@Body request: UserRequest): UserResponse

    @GET("api/food/nutrition")
    suspend fun getFoodNutrition(
        @Query("food") foodName: String
    ): NutritionResponse

    @GET("food/ingredients/search")
    suspend fun searchFood(
        @Query("query") foodName: String
    ): FoodSearchResponse

    @POST("api/food/analyze")
    suspend fun analyzeFoodImage(
        @Body request: FoodAnalysisRequest
    ): FoodAnalysisResponse

    @GET("api/nutrition/details")
    suspend fun getNutritionDetails(
        @Query("food") foodName: String
    ): NutritionDetailsResponse

    @POST("meal-plan/create")
    suspend fun createMealPlan(@Body mealPlan: MealPlan): Response<MealPlan>

    @GET("meal-plan/{date}")
    suspend fun getMealPlan(@Path("date") date: String): Response<MealPlan>

    @POST("water-intake/record")
    suspend fun recordWaterIntake(@Body waterIntake: WaterIntake): Response<WaterIntake>

    @GET("nutrition/report")
    suspend fun getNutritionReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<NutritionReport>

    @POST("food/recognize-multiple")
    suspend fun recognizeMultipleFoods(
        @Body imageData: FoodImageRequest
    ): Response<List<FoodRecognitionResponse>>
}

// Data classes should be in separate files
data class FoodImageRequest(val image: String)

data class FoodRecognitionResponse(
    val success: Boolean,
    val foodName: String?,
    val confidence: Float?
)

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

data class UserRequest(
    val email: String,
    val password: String,
    val name: String? = null
)

data class UserResponse(
    val message: String,
    val token: String? = null,
    val profile: User? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

// Add missing response types if not already defined
data class FoodSearchResponse(
    val foods: List<FoodItem>
)

data class FoodItem(
    val name: String,
    val category: String,
    val nutrients: NutrientInfo
)

data class FoodAnalysisRequest(
    val image: String,
    val includeNutrition: Boolean = true
)

data class FoodAnalysisResponse(
    val success: Boolean,
    val foodName: String?,
    val nutritionInfo: NutrientInfo?
)
