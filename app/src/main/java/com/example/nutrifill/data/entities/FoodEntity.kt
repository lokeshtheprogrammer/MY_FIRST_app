package com.example.nutrifill.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val imageUrl: String? = null,
    val confidence: Float? = null,
    val isSynced: Boolean = false
)