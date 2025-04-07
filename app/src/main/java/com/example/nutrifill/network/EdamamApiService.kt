package com.example.nutrifill.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

data class EdamamNutritionResponse(
    val calories: Float,
    val totalWeight: Float,
    val totalNutrients: NutrientInfo
)

data class NutrientInfo(
    val ENERC_KCAL: Nutrient?,
    val PROCNT: Nutrient?,  // Protein
    val FAT: Nutrient?,     // Total lipid (fat)
    val CHOCDF: Nutrient?,  // Carbohydrate
    val FIBTG: Nutrient?,   // Fiber
    val SUGAR: Nutrient?,   // Sugars
    val NA: Nutrient?,      // Sodium
    val CA: Nutrient?,      // Calcium
    val FE: Nutrient?,      // Iron
    val VITC: Nutrient?,    // Vitamin C
    val VITD: Nutrient?,    // Vitamin D
    val TOCPHA: Nutrient?   // Vitamin E
)

data class Nutrient(
    val label: String,
    val quantity: Float,
    val unit: String
)

interface EdamamApiService {
    @POST("food-img/v3/analysis")
    @Headers("Content-Type: application/json")
    fun analyzeFoodImage(
        @Query("app_id") appId: String = "a177b041",
        @Query("app_key") appKey: String = "c0b740aa31ea23c945a1e451f7e3e974",
        @Body imageRequest: ImageAnalysisRequest
    ): Call<EdamamFoodRecognitionResponse>

    data class ImageAnalysisRequest(
        val base64: String
    )

    @GET("api/nutrition-data")
    fun getNutritionData(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("ingr") ingredient: String
    ): Call<EdamamNutritionResponse>
}