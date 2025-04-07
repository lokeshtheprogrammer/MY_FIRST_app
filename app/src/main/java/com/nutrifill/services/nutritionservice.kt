package com.nutrifill.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import org.json.JSONArray

class NutrixNutritionService(private val context: Context) {

    companion object {
        private const val TAG = "NutritionService"
        private const val BASE_URL = "https://trackapi.nutritionix.com/v2/natural/nutrients"
        private const val APP_ID = "e0416a8e"
        private const val APP_KEY = "74ab2660a754bc0d089df4a87d08aad2"
    }

    data class NutritionInfo(
        val foodName: String,
        val calories: Double,
        val protein: Double,
        val fat: Double,
        val carbs: Double
    )

    interface NutritionCallback {
        fun onSuccess(nutritionList: List<NutritionInfo>)
        fun onError(error: String)
    }

    fun getNutritionInfo(query: String, callback: NutritionCallback) {
        val requestBody = JSONObject().apply {
            put("query", query)
            put("timezone", "Asia/Kolkata")
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST,
            BASE_URL,
            requestBody,
            Response.Listener { response ->
                try {
                    val foods = response.getJSONArray("foods")
                    val nutritionList = mutableListOf<NutritionInfo>()

                    for (i in 0 until foods.length()) {
                        val foodItem = foods.getJSONObject(i)
                        nutritionList.add(
                            NutritionInfo(
                                foodName = foodItem.getString("food_name"),
                                calories = foodItem.getDouble("nf_calories"),
                                protein = foodItem.getDouble("nf_protein"),
                                fat = foodItem.getDouble("nf_total_fat"),
                                carbs = foodItem.getDouble("nf_total_carbohydrate")
                            )
                        )
                    }
                    callback.onSuccess(nutritionList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    callback.onError("Error processing nutrition data")
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "API Error: ${error.message}")
                callback.onError(error.message ?: "Unknown error occurred")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "x-app-id" to APP_ID,
                    "x-app-key" to APP_KEY,
                    "Content-Type" to "application/json"
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}