package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName
import com.example.nutrifill.models.Nutrients

data class FoodAnalysisResponse(
    @SerializedName("foodName") val foodName: String = "",
    @SerializedName("nutrients") val nutrients: Nutrients = Nutrients(),
    @SerializedName("message") val message: String = ""
)