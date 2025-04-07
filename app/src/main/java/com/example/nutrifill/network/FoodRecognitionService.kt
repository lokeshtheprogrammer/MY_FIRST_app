package com.example.nutrifill.network

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.nutrifill.BuildConfig
import com.example.nutrifill.BuildConfig.GOOGLE_VISION_API_KEY
import com.example.nutrifill.models.Nutrients
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class FoodRecognitionService(private val context: Context) {
    companion object {
        private const val TAG = "FoodRecognitionService"
        private const val GOOGLE_VISION_BASE_URL = "https://vision.googleapis.com/"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 10000L
        private const val RETRY_DELAY_MS = 1000L

        @JvmStatic
        fun getApiKey(context: Context): String {
            return BuildConfig.GOOGLE_VISION_API_KEY
        }
    }

    private val googleVisionService: FoodRecognitionApiService by lazy {
        createRetrofitService(GOOGLE_VISION_BASE_URL)
    }

    private val openFoodFactsService: FoodRecognitionApiService by lazy {
        createRetrofitService("https://world.openfoodfacts.org/")
    }

    private fun createRetrofitService(baseUrl: String): FoodRecognitionApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(FoodRecognitionApiService::class.java)
    }

    sealed class RecognitionResult<out T> {
        data class Success<T>(val data: T) : RecognitionResult<T>()
        data class Error(val exception: Exception) : RecognitionResult<Nothing>()
    }

    suspend fun recognizeFood(bitmap: Bitmap): RecognitionResult<List<String>> = withContext(Dispatchers.IO) {
        var retryCount = 0
        var lastException: Exception? = null
        var response: retrofit2.Response<GoogleVisionResponse>? = null
        var currentDelay = INITIAL_RETRY_DELAY_MS

        while (retryCount < MAX_RETRIES && isActive) {
            try {
                ensureActive() // More idiomatic way to check coroutine status

                // Convert bitmap to base64
                val base64Image = bitmapToBase64(bitmap)
                Log.d(TAG, "Image converted to base64, starting API request")

                // Prepare Google Vision API request
                val request = GoogleVisionRequest(
                    requests = listOf(
                        GoogleVisionImageRequest(
                            image = GoogleVisionImage(content = base64Image),
                            features = listOf(GoogleVisionFeature())
                        )
                    )
                )

                // Make API call with proper resource management
                response = googleVisionService.detectFood(GOOGLE_VISION_API_KEY, request).execute()
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    throw Exception("API call failed with code: ${response.code()}, Error: $errorBody")
                }

                // Process response safely
                val responseBody = response.body()
                if (responseBody == null) {
                    throw Exception("Response body is null")
                }

                // Extract and process annotations
                val labels = responseBody.responses
                    .firstOrNull()?.labelAnnotations
                    ?.filter { it.score > 0.7 }  // Filter high confidence predictions
                    ?.map { it.description }
                    ?: emptyList()

                Log.d(TAG, "Successfully processed ${labels.size} food labels")
                return@withContext RecognitionResult.Success(labels)

            } catch (e: CancellationException) {
                Log.w(TAG, "Food recognition was cancelled: ${e.message}")
                throw RecognitionException("Job was cancelled during image processing", e)

            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Error recognizing food (attempt ${retryCount + 1}): ${e.message}")

                if (retryCount < MAX_RETRIES - 1 && isActive) {
                    retryCount++
                    val backoffDelay = RETRY_DELAY_MS * (1L shl (retryCount - 1))
                    Log.d(TAG, "Retrying after $backoffDelay ms")
                    delay(timeMillis = backoffDelay)
                    continue
                }
                break
            } finally {
                // Clean up resources
                response?.errorBody()?.close()
            }
        }

        val errorMessage = "Failed to recognize food after $MAX_RETRIES attempts"
        Log.e(TAG, errorMessage, lastException)
        return@withContext RecognitionResult.Error(RecognitionException(errorMessage, lastException))
    }

    suspend fun getNutritionInfo(foodName: String, portionSizeGrams: Float = 100f): RecognitionResult<Nutrients> = withContext(Dispatchers.IO) {
        var retryCount = 0
        var lastException: Exception? = null
        var response: retrofit2.Response<OpenFoodFactsResponse>? = null
        var currentDelay = INITIAL_RETRY_DELAY_MS

        while (retryCount < MAX_RETRIES && isActive) {
            try {
                ensureActive() // Check if coroutine is still active
                Log.d(TAG, "Searching nutrition info for: $foodName (attempt ${retryCount + 1})")

                // Search for food in OpenFoodFacts database
                response = openFoodFactsService.searchFood(foodName).execute()
                
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    throw Exception("API call failed with code: ${response.code()}, Error: $errorBody")
                }

                val responseBody = response.body()
                if (responseBody == null) {
                    throw Exception("Response body is null")
                }

                val nutriments = responseBody.product?.nutriments
                    ?: throw Exception("No nutrition information found for $foodName")

                // Calculate nutrition values based on portion size
                val portionMultiplier = portionSizeGrams / 100f
                val nutrients = Nutrients(
                    calories = ((nutriments.energy_100g ?: 0f) * portionMultiplier).toInt(),
                    protein = (nutriments.proteins_100g ?: 0f) * portionMultiplier,
                    carbs = (nutriments.carbohydrates_100g ?: 0f) * portionMultiplier,
                    fat = (nutriments.fat_100g ?: 0f) * portionMultiplier
                )

                Log.d(TAG, "Successfully fetched nutrition info for $foodName")
                return@withContext RecognitionResult.Success(nutrients)

            } catch (e: CancellationException) {
                Log.w(TAG, "Nutrition info fetch was cancelled: ${e.message}")
                throw Exception("Job was cancelled while fetching nutrition info", e)

            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Error fetching nutrition info (attempt ${retryCount + 1}): ${e.message}")

                if (retryCount < MAX_RETRIES - 1 && isActive) {
                    retryCount++
                    val backoffDelay = RETRY_DELAY_MS * (1L shl (retryCount - 1))
                    Log.d(TAG, "Retrying after $backoffDelay ms")
                    delay(timeMillis = backoffDelay)
                    continue
                }
                break
            } finally {
                // Clean up resources
                response?.errorBody()?.close()
            }
        }

        val errorMessage = "Failed to fetch nutrition info after $MAX_RETRIES attempts"
        Log.e(TAG, errorMessage, lastException)
        return@withContext RecognitionResult.Error(Exception(errorMessage, lastException))
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}