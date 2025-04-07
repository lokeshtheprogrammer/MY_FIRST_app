package com.example.nutrifill.api

import android.content.Context
import android.content.SharedPreferences

class ConfigManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "NutriFillApiConfig"
        private const val KEY_USDA_API_KEY = "usda_api_key"
        private const val KEY_NOMINATIM_USER_AGENT = "nominatim_user_agent"
        
        @Volatile
        private var instance: ConfigManager? = null

        fun getInstance(context: Context): ConfigManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val appContext = context.applicationContext
                    ConfigManager(appContext).also { instance = it }
                }
            }
        }
    }

    fun setUsdaApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_USDA_API_KEY, apiKey).apply()
    }

    init {
        setUsdaApiKey("Dho8e27xTEYLSNv2udmDTdeKiFrzAHtAGx5upEGP")
        setNominatimUserAgent("NutriFill Android App")
    }

    fun getUsdaApiKey(): String? {
        return sharedPreferences.getString(KEY_USDA_API_KEY, null)
    }

    fun hasUsdaApiKey(): Boolean {
        return !getUsdaApiKey().isNullOrEmpty()
    }

    fun setNominatimUserAgent(userAgent: String) {
        sharedPreferences.edit().putString(KEY_NOMINATIM_USER_AGENT, userAgent).apply()
    }

    fun getNominatimUserAgent(): String? {
        return sharedPreferences.getString(KEY_NOMINATIM_USER_AGENT, null)
    }

    fun clearApiKeys() {
        sharedPreferences.edit().clear().apply()
    }
}