package com.example.nutrifill.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Location(
    val lat: Double,
    val lon: Double,
    val displayName: String,
    val type: String
)

class LocationService {
    companion object {
        private const val BASE_URL = "https://nominatim.openstreetmap.org/search"
        private const val TAG = "LocationService"
    }

    suspend fun searchAmmaUnavagam(): List<Location> = withContext(Dispatchers.IO) {
        val query = "Amma+Unavagam+Chennai"
        val urlString = "$BASE_URL?q=$query&format=json"
        
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "NutriFill Android App")
            
            val response = StringBuilder()
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
            }
            
            val jsonArray = JSONArray(response.toString())
            val locations = mutableListOf<Location>()
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                locations.add(
                    Location(
                        lat = item.getDouble("lat"),
                        lon = item.getDouble("lon"),
                        displayName = item.getString("display_name"),
                        type = item.getString("type")
                    )
                )
            }
            
            locations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching locations: ${e.message}")
            emptyList()
        }
    }
}