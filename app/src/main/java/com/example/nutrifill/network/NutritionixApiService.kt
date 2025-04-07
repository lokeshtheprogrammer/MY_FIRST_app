package com.example.nutrifill.network

import retrofit2.Call
import retrofit2.http.*

data class NutritionixNutrientResponse(
    val foods: List<NutritionixFoodItem>
)

data class NutritionixFoodItem(
    val food_name: String,
    val serving_qty: Double,
    val serving_unit: String,
    val serving_weight_grams: Double,
    val nf_calories: Double,
    val nf_total_fat: Double,
    val nf_saturated_fat: Double,
    val nf_cholesterol: Double,
    val nf_sodium: Double,
    val nf_total_carbohydrate: Double,
    val nf_dietary_fiber: Double,
    val nf_sugars: Double,
    val nf_protein: Double,
    val nf_potassium: Double,
    val photo: PhotoInfo?
)

data class PhotoInfo(
    val thumb: String,
    val highres: String?
)

interface NutritionixApiService {
    @Headers(
        "x-app-id: YOUR_APP_ID",
        "x-app-key: YOUR_API_KEY",
        "x-remote-user-id: 0"
    )
    @GET("v2/natural/nutrients")
    fun getNutrients(
        @Query("query") query: String
    ): Call<NutritionixNutrientResponse>

    @Headers(
        "x-app-id: YOUR_APP_ID",
        "x-app-key: YOUR_API_KEY",
        "x-remote-user-id: 0"
    )
    @POST("v2/natural/nutrients")
    fun postNutrients(
        @Body query: Map<String, String>
    ): Call<NutritionixNutrientResponse>

    companion object {
        const val BASE_URL = "https://trackapi.nutritionix.com/"
    }
}