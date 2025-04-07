package com.example.nutrifill

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import com.example.nutrifill.api.VisionApiService
import com.example.nutrifill.model.NutritionInfo
import com.example.nutrifill.model.Nutrient
import kotlinx.coroutines.*

/**
 * Exception thrown when food recognition fails
 */
class RecognitionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Data class representing a food recognition result
 */
data class FoodRecognitionResult(
    val foodName: String,
    val confidence: Float,
    val model: String = "Google Cloud Vision",
    var nutritionInfo: NutritionInfo? = null
) {
    override fun toString(): String = "$foodName (${(confidence * 100).toInt()}%) - $model"
}

/**
 * Service for recognizing food items from images using Google Cloud Vision API
 */
class FoodRecognitionService(private val context: Context) {
    companion object {
        private const val TAG = "FoodRecognitionService"
    }

    private val visionApiService = VisionApiService()
    private val resultCache = LruCache<String, List<FoodRecognitionResult>>(100) // Cache last 100 results
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private fun validateImage(bitmap: Bitmap) {
        if (bitmap.isRecycled) {
            throw IllegalArgumentException("Invalid image: Bitmap is recycled")
        }
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            throw IllegalArgumentException("Invalid image dimensions: ${bitmap.width}x${bitmap.height}")
        }
        if (bitmap.width * bitmap.height > 12_000_000) { // ~12MP limit
            throw IllegalArgumentException("Image resolution too high: ${bitmap.width}x${bitmap.height}")
        }
    }

    private fun generateCacheKey(bitmap: Bitmap): String {
        return bitmap.generateHash()
    }

    private fun Bitmap.generateHash(): String {
        return java.io.ByteArrayOutputStream().use { bytes ->
            this.compress(Bitmap.CompressFormat.PNG, 100, bytes)
            bytes.toByteArray().contentHashCode().toString()
        }
    }

    suspend fun recognizeFood(bitmap: Bitmap): List<FoodRecognitionResult> = withContext(Dispatchers.Default) {
        try {
            // Validate input image
            validateImage(bitmap)
            
            val cacheKey = generateCacheKey(bitmap)
            resultCache.get(cacheKey)?.let { cachedResult ->
                Log.d(TAG, "Returning cached result")
                return@withContext cachedResult
            }

            // Use Vision API to detect labels
            val visionResult = visionApiService.detectLabels(bitmap)
            
            val results = visionResult.getOrNull()?.map { label ->
                val result = FoodRecognitionResult(
                    foodName = label.description,
                    confidence = label.confidence
                )
                // Create nutrition info with default values
                val nutritionInfo = NutritionInfo(
                    calories = 0,
                    protein = 0f,
                    carbs = 0f,
                    fat = 0f,
                    servingSize = 100f,
                    servingUnit = "g",
                    foodName = label.description,
                    nutrients = listOf(
                        Nutrient("Protein", 0f, "g"),
                        Nutrient("Fat", 0f, "g"),
                        Nutrient("Carbohydrates", 0f, "g"),
                        Nutrient("Fiber", 0f, "g")
                    )
                )
                result.nutritionInfo = nutritionInfo
                result
            } ?: throw RecognitionException("Failed to get results from Vision API")

            if (results.isEmpty()) {
                throw RecognitionException("No results from Vision API")
            }

            resultCache.put(cacheKey, results)
            results

        } catch (e: Exception) {
            Log.e(TAG, "Error during food recognition: ${e.message}")
            throw RecognitionException("Failed to process image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid image format: ${e.message}")
            throw RecognitionException("Invalid image format: ${e.message}", e)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while processing image")
            throw RecognitionException("Image is too large to process", e)
        }
    }

    fun close() {
        serviceScope.cancel()
    }

    /**
     * Verifies that the Vision API service is properly configured and working
     * @return Pair<Boolean, String> where first is true if working, and second is status message
     */
    suspend fun verifyModels(): Pair<Boolean, String> = withContext(Dispatchers.Default) {
        try {
            if (visionApiService == null) {
                return@withContext Pair(false, "Vision API service not initialized")
            }
            Pair(true, "Vision API service ready")
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying models: ${e.message}")
            Pair(false, "Error: ${e.message}")
        }
    }
}

