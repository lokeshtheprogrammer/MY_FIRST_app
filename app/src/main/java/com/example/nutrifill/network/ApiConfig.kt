package com.example.nutrifill.network

object ApiConfig {
    // Nutritionix API credentials
    const val NUTRITIONIX_APP_ID = "e0416a8e"
    const val NUTRITIONIX_APP_KEY = "74ab2660a754bc0d089df4a87d08aad2"
    
    // Edamam API credentials (existing)
    const val EDAMAM_APP_ID = "a177b041"
    const val EDAMAM_APP_KEY = "c0b740aa31ea23c945a1e451f7e3e974"
    
    // Server configuration
    const val SERVER_BASE_URL = "https://api.nutrifill.com/" // Update with your actual server URL
    
    // API Endpoints
    object Endpoints {
        // Nutritionix endpoints
        const val NUTRITIONIX_NUTRIENTS = "v2/natural/nutrients"
        const val NUTRITIONIX_SEARCH = "v2/search/instant"
        
        // Add other endpoints as needed
    }
}