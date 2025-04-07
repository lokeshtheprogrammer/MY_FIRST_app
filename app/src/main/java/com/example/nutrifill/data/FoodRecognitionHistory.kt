package com.example.nutrifill.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FoodRecognitionHistory(
    val foodName: String,
    val confidence: Float,
    val model: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FoodHistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("food_history", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveHistory(history: FoodRecognitionHistory) {
        val historyList = getAllHistory().toMutableList()
        historyList.add(0, history)
        
        prefs.edit().putString("history", gson.toJson(historyList)).apply()
    }

    fun getAllHistory(): List<FoodRecognitionHistory> {
        val json = prefs.getString("history", "[]")
        val type = object : TypeToken<List<FoodRecognitionHistory>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearHistory() {
        prefs.edit().remove("history").apply()
    }
}