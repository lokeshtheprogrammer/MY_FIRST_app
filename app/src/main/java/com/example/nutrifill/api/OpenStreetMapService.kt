package com.example.nutrifill.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenStreetMapService {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10
    ): Response<List<LocationResponse>>
}

data class LocationResponse(
    val place_id: Long,
    val licence: String,
    val osm_type: String,
    val osm_id: Long,
    val lat: String,
    val lon: String,
    val display_name: String,
    val type: String,
    val importance: Double,
    val icon: String?
)

object LocationMapper {
    fun fromResponse(response: LocationResponse): Location {
        return Location(
            lat = response.lat.toDoubleOrNull() ?: 0.0,
            lon = response.lon.toDoubleOrNull() ?: 0.0,
            displayName = response.display_name,
            type = response.type
        )
    }
}