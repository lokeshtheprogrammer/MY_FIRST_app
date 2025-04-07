package com.example.nutrifill.models

import androidx.annotation.DrawableRes

data class FoodRecommendation(
    val name: String,
    val description: String,
    val price: String,
    val servingSize: String = "",
    val portion: String,
    @DrawableRes val imageResource: Int,
    val nutrients: Nutrients = Nutrients()
)