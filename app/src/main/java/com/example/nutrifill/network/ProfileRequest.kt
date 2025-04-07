package com.example.nutrifill.network

import com.google.gson.annotations.SerializedName

data class ProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int?,
    @SerializedName("gender") val gender: String,
    @SerializedName("height") val height: Float,
    @SerializedName("weight") val weight: Float,
    @SerializedName("activityLevel") val activityLevel: String,
    @SerializedName("isPregnant") val isPregnant: Boolean = false,
    @SerializedName("dietaryPreferences") val dietaryPreferences: List<String> = emptyList(),
    @SerializedName("allergies") val allergies: List<String> = emptyList()
) {
    companion object {
        const val GENDER_MALE = "male"
        const val GENDER_FEMALE = "female"
        const val GENDER_OTHER = "other"

        const val ACTIVITY_SEDENTARY = "Sedentary"
        const val ACTIVITY_LIGHTLY = "Lightly Active"
        const val ACTIVITY_MODERATELY = "Moderately Active"
        const val ACTIVITY_VERY = "Very Active"
        const val ACTIVITY_EXTREMELY = "Extremely Active"
    }
}