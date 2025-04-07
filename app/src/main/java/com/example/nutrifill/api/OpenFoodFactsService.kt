package com.example.nutrifill.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsService {
    @GET("product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<OpenFoodFactsResponse>
}

data class OpenFoodFactsResponse(
    val code: String,
    val product: ProductData,
    val status: Int,
    val status_verbose: String
)

data class ProductData(
    val product_name: String?,
    val generic_name: String?,
    val brands: String?,
    val categories: String?,
    val ingredients_text: String?,
    val nutriments: Nutriments,
    val serving_size: String?,
    val image_url: String?,
    val allergens: String?,
    val labels: String?,
    val stores: String?,
    val countries: String?,
    val quantity: String?
)

data class Nutriments(
    val energy_100g: Double?,
    val proteins_100g: Double?,
    val carbohydrates_100g: Double?,
    val fat_100g: Double?,
    val fiber_100g: Double?,
    val sodium_100g: Double?,
    val sugars_100g: Double?,
    val calcium_100g: Double?,
    val iron_100g: Double?,
    val vitamin_a_100g: Double?,
    val vitamin_c_100g: Double?,
    val saturated_fat_100g: Double?,
    val cholesterol_100g: Double?,
    val potassium_100g: Double?
)