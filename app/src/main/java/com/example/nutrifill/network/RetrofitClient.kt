package com.example.nutrifill.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log
import android.net.TrafficStats
import com.example.nutrifill.api.FoodDataService
import com.example.nutrifill.api.OpenFoodFactsService
import com.example.nutrifill.api.OpenStreetMapService

object RetrofitClient {
    private const val BASE_URL = AppConfig.SERVER_BASE_URL
    private const val EDAMAM_BASE_URL = "https://api.edamam.com/"
    private const val RETRY_DELAY_MS = 1000L
    private const val USDA_BASE_URL = "https://api.nal.usda.gov/fdc/v1/"
    private const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/api/v2/"
    private const val OPEN_STREET_MAP_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val NUTRITIONIX_BASE_URL = "https://trackapi.nutritionix.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        // Only log request/response lines and headers, not bodies to prevent stream closure issues
    }

    private val dispatcher = okhttp3.Dispatcher().apply {
        maxRequests = 64 // Increased from default 64
        maxRequestsPerHost = 16 // Increased from default 5
    }

    private val connectionPool = okhttp3.ConnectionPool(
        maxIdleConnections = 10,
        keepAliveDuration = 5,
        timeUnit = TimeUnit.MINUTES
    )

    private val okHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectionPool(connectionPool)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Tag socket for network stats tracking
            TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
            val request = chain.request()
            Log.d("API", "Request URL: ${request.url}")

            var tryCount = 0
            var response: okhttp3.Response? = null
            var lastException: Exception? = null

            while (tryCount < 3 && response == null) {
                try {
                    if (tryCount > 0) {
                        val backoffDuration = (1000L * (1 shl (tryCount - 1))).coerceAtMost(10000L)
                        Thread.sleep(backoffDuration)
                        Log.d("API", "Retrying request (attempt ${tryCount + 1}) for URL: ${request.url}")
                    }
                    
                    // Create a new request with a fresh body for each attempt
                    val newRequest = if (tryCount > 0) request.newBuilder().build() else request
                    response = chain.proceed(newRequest)
                    
                    // Check if response is successful
                    if (!response.isSuccessful) {
                        val responseBody = response.peekBody(Long.MAX_VALUE).string()
                        response.close()
                        
                        if (response.code in 500..599) {
                            throw java.io.IOException("Server error: ${response.code}, Body: $responseBody")
                        }
                        // For other error codes, return the response to let the caller handle it
                        response = chain.proceed(newRequest)
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.e("API", "Request attempt ${tryCount + 1} failed for URL: ${request.url}, Error: ${e.message}")
                    tryCount++
                    if (tryCount == 3) break
                    
                    // Close response on error to prevent resource leaks
                    response?.close()
                    response = null
                    
                    // Add delay before retry
                    Thread.sleep(RETRY_DELAY_MS)
                }
            }

            try {
                response ?: throw lastException ?: java.io.IOException("Request failed after 3 attempts")
            } finally {
                // Clear socket tag after request
                TrafficStats.clearThreadStatsTag()
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Main API service instance
    val instance: ApiService = retrofit.create(ApiService::class.java)

    // Food and nutrition services
    val edamamService: EdamamApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EDAMAM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EdamamApiService::class.java)
    }

    val foodDataService: FoodDataService by lazy {
        Retrofit.Builder()
            .baseUrl(USDA_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodDataService::class.java)
    }

    val openFoodFactsService: OpenFoodFactsService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_FOOD_FACTS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsService::class.java)
    }

    // Location service
    val openStreetMapService: OpenStreetMapService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_STREET_MAP_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenStreetMapService::class.java)
    }

    // Nutritionix service for detailed nutrition information
    val nutritionixService: NutritionixApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NUTRITIONIX_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NutritionixApiService::class.java)
    }
}
