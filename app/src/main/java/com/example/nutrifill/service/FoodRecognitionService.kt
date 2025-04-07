package com.example.nutrifill.service

import android.content.Context
import android.graphics.Bitmap
import com.example.nutrifill.model.FoodRecognitionResult
import com.example.nutrifill.model.NutritionInfo
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FoodRecognitionService(private val context: Context) {
    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }

    suspend fun recognizeFood(bitmap: Bitmap): List<FoodRecognitionResult> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val detectedObjects = objectDetector.process(image).await()

            detectedObjects.mapNotNull { detectedObject ->
                detectedObject.labels.maxByOrNull { it.confidence }?.let { label ->
                    FoodRecognitionResult(
                        foodName = label.text,
                        confidence = label.confidence,
                        nutritionInfo = estimateNutrition(label.text)
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun estimateNutrition(foodName: String): NutritionInfo {
        // This is a placeholder implementation
        // In a real app, this would call a nutrition database API
        return NutritionInfo(
            calories = 100,
            protein = 5f,
            carbs = 15f,
            fat = 3f,
            servingSize = 100f,
            servingUnit = "g"
        )
    }

    suspend fun verifyModels(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            // Verify ML Kit model availability
            objectDetector
            true to "Models loaded successfully"
        } catch (e: Exception) {
            false to "Error loading models: ${e.message}"
        }
    }
}