package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName

data class ScanHistoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("foodName") val foodName: String,
    @SerializedName("nutritionInfo") val nutritionInfo: NutritionInfo,
    @SerializedName("scanDate") val scanDate: String
) : java.io.Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}

data class NutritionInfo(
    @SerializedName("calories") val calories: Float,
    @SerializedName("protein") val protein: Float,
    @SerializedName("carbs") val carbs: Float,
    @SerializedName("fat") val fat: Float,
    @SerializedName("fiber") val fiber: Float?
) : java.io.Serializable