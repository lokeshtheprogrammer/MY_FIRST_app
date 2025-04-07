package com.example.nutrifill.network

import retrofit2.Call
import retrofit2.http.*

data class GoogleVisionRequest(
    val requests: List<GoogleVisionImageRequest>
)

data class GoogleVisionImageRequest(
    val image: GoogleVisionImage,
    val features: List<GoogleVisionFeature>
)

data class GoogleVisionImage(
    val content: String  // Base64 encoded image
)

data class GoogleVisionFeature(
    val type: String = "LABEL_DETECTION",
    val maxResults: Int = 10
)

data class GoogleVisionResponse(
    val responses: List<GoogleVisionAnnotation>
)

data class GoogleVisionAnnotation(
    val labelAnnotations: List<GoogleVisionLabel>
)

data class GoogleVisionLabel(
    val description: String,
    val score: Float
)

data class OpenFoodFactsResponse(
    val product: OpenFoodFactsProduct
)

data class OpenFoodFactsProduct(
    val product_name: String,
    val nutriments: OpenFoodFactsNutriments
)

data class OpenFoodFactsNutriments(
    val energy_100g: Float?,
    val proteins_100g: Float?,
    val carbohydrates_100g: Float?,
    val fat_100g: Float?,
    val fiber_100g: Float?
)

interface FoodRecognitionApiService {
    @POST("https://vision.googleapis.com/v1/images:annotate")
    fun detectFood(
        @Query("key") apiKey: String,
        @Body request: GoogleVisionRequest
    ): Call<GoogleVisionResponse>

    @GET("https://world.openfoodfacts.org/api/v0/product/{barcode}.json")
    fun getFoodInfo(
        @Path("barcode") barcode: String
    ): Call<OpenFoodFactsResponse>

    @GET("https://world.openfoodfacts.org/cgi/search.pl")
    fun searchFood(
        @Query("search_terms") searchTerms: String,
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 1
    ): Call<OpenFoodFactsResponse>
}