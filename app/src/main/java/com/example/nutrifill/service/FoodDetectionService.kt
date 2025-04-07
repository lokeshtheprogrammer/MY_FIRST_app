package com.example.nutrifill.service

import android.graphics.Bitmap
import android.util.Log
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.ImageContext
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class FoodDetectionService {
    private val TAG = "FoodDetectionService"
    private lateinit var imageAnnotatorClient: ImageAnnotatorClient

    init {
        try {
            imageAnnotatorClient = ImageAnnotatorClient.create()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Vision API client: ${e.message}")
        }
    }

    suspend fun detectFood(bitmap: Bitmap): List<FoodItem> = withContext(Dispatchers.IO) {
        try {
            // Convert bitmap to ByteString
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build()

            // Configure the vision API request
            val feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build()
            val context = ImageContext.newBuilder()
                .addLanguageHints("en")
                .build()

            val request = com.google.cloud.vision.v1.AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .setImageContext(context)
                .build()

            // Perform the detection
            val response = imageAnnotatorClient.batchAnnotateImages(
                listOf(request)
            )

            // Process results
            val foodItems = mutableListOf<FoodItem>()
            response.responsesList[0].labelAnnotationsList.forEach { label ->
                if (label.score >= 0.7 && isFoodRelated(label.description)) {
                    foodItems.add(
                        FoodItem(
                            name = label.description,
                            confidence = label.score
                        )
                    )
                }
            }

            foodItems
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting food: ${e.message}")
            emptyList()
        }
    }

    private fun isFoodRelated(description: String): Boolean {
        val foodKeywords = setOf(
            "food", "dish", "meal", "cuisine", "fruit", "vegetable",
            "meat", "dessert", "snack", "breakfast", "lunch", "dinner"
        )
        return foodKeywords.any { keyword ->
            description.toLowerCase().contains(keyword)
        }
    }

    fun cleanup() {
        try {
            imageAnnotatorClient.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Vision API client: ${e.message}")
        }
    }
}

data class FoodItem(
    val name: String,
    val confidence: Float,
    var portionSize: String = "medium",
    var calories: Int = 0,
    var nutrients: Map<String, Float> = emptyMap()
)