package com.example.nutrifill.models

data class Meal(
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val timestamp: Long = System.currentTimeMillis()
)