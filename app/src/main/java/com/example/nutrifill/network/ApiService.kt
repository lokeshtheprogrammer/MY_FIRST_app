package com.example.nutrifill.network

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("api/auth/signin")  // Updated to match backend route in auth.js
    fun login(@Body request: UserRequest): Call<UserResponse>

    @Headers("Content-Type: application/json")
    @POST("api/auth/signup")  // Updated to match backend route in auth.js
    fun register(@Body request: UserRequest): Call<UserResponse>
    
    @Headers("Content-Type: application/json")
    @PUT("api/auth/profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest
    ): Call<UserResponse>

    @Headers("Content-Type: application/json")
    @GET("api/scan/history")
    fun getScanHistory(
        @Header("Authorization") token: String
    ): Call<List<ScanHistoryResponse>>
}
