package com.example.nutrifill.service

import android.graphics.Bitmap
import android.util.Log
import com.example.nutrifill.model.FoodItem
import kotlin.math.max
import kotlin.math.min

/**
 * Service for estimating portion sizes of food items from images
 */
class PortionEstimationService {
    companion object {
        private const val TAG = "PortionEstimationService"
        
        // Default portion sizes in grams for different categories
        private val DEFAULT_PORTIONS = mapOf(
            "small" to 100f,
            "medium" to 200f,
            "large" to 300f
        )
    }

    /**
     * Estimates the portion size of a food item from an image
     * @param bitmap The food image
     * @param foodItem The detected food item
     * @return Estimated portion size in grams
     */
    fun estimatePortionSize(bitmap: Bitmap, foodItem: FoodItem): Float {
        try {
            // Get the relative size of the food in the image (0.0 to 1.0)
            val relativeSize = calculateRelativeSize(bitmap)
            
            // Map the relative size to a portion category
            val portionCategory = when {
                relativeSize < 0.3 -> "small"
                relativeSize < 0.7 -> "medium"
                else -> "large"
            }
            
            // Get the default portion size for this category
            return DEFAULT_PORTIONS[portionCategory] ?: DEFAULT_PORTIONS["medium"]!!
        } catch (e: Exception) {
            Log.e(TAG, "Error estimating portion size: ${e.message}")
            return DEFAULT_PORTIONS["medium"]!! // Return medium portion size as fallback
        }
    }

    /**
     * Calculates the relative size of the food in the image
     * @param bitmap The food image
     * @return A value between 0.0 and 1.0 representing the relative size
     */
    private fun calculateRelativeSize(bitmap: Bitmap): Float {
        // Simple implementation using image dimensions
        // In a real app, this would use more sophisticated computer vision techniques
        val imageArea = bitmap.width * bitmap.height
        val maxDimension = max(bitmap.width, bitmap.height)
        val minDimension = min(bitmap.width, bitmap.height)
        
        // Calculate a relative size based on aspect ratio and total area
        val aspectRatio = maxDimension.toFloat() / minDimension.toFloat()
        val normalizedArea = (imageArea / (maxDimension * maxDimension).toFloat())
        
        // Combine metrics into a single relative size value
        return (normalizedArea * (1.0f / aspectRatio)).coerceIn(0.0f, 1.0f)
    }

    /**
     * Adjusts the portion size based on user feedback
     * @param currentSize Current portion size in grams
     * @param adjustment Adjustment factor (e.g., 0.8 for 20% smaller, 1.2 for 20% larger)
     * @return Adjusted portion size in grams
     */
    fun adjustPortionSize(currentSize: Float, adjustment: Float): Float {
        return (currentSize * adjustment).coerceIn(50f, 500f) // Limit to reasonable range
    }
}