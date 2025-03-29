package com.example.nutrifill.feedback

import com.example.nutrifill.network.RetrofitClient
import com.example.nutrifill.models.FoodFeedbackRequest

class FoodFeedback {
    private val api = RetrofitClient.instance.create(FeedbackApi::class.java)

    suspend fun submitFeedback(feedback: FoodFeedbackRequest): Boolean {
        return try {
            val response = api.submitFeedback(feedback)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}