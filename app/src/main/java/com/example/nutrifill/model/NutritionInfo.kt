package com.example.nutrifill.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing nutrition information for a food item
 */
@Parcelize
data class NutritionInfo(
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val servingSize: Float = 1f,
    val servingUnit: String = "serving",
    val foodName: String = "",
    val ingredients: List<String> = emptyList(),
    val nutrients: List<Nutrient> = emptyList()
) : Parcelable {
    override fun toString(): String = "$foodName ($calories kcal per $servingSize $servingUnit)"
}

@Parcelize
data class Nutrient(
    val name: String,
    val value: Float,
    val unit: String
) : Parcelable