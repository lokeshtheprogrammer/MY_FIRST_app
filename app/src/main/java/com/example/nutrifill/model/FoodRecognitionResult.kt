package com.example.nutrifill.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.example.nutrifill.model.NutritionInfo

@Parcelize
data class FoodRecognitionResult(
    val foodName: String,
    val confidence: Float,
    val nutritionInfo: NutritionInfo = NutritionInfo(),
    val imageUrl: String? = null,
    val recognitionSource: String = "Google Vision",
    val alternativeFoods: List<String> = emptyList(),
    val portionEstimate: Float? = null,
    val portionUnit: String = "serving",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable