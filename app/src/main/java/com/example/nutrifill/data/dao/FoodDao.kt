package com.example.nutrifill.data.dao

import androidx.room.*
import com.example.nutrifill.data.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert
    suspend fun insertFood(food: FoodEntity): Long

    @Query("SELECT * FROM foods ORDER BY timestamp DESC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :foodId")
    suspend fun getFoodById(foodId: Long): FoodEntity?

    @Delete
    suspend fun deleteFood(food: FoodEntity)
}