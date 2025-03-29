package com.example.nutrifill.repository

import android.content.Context
import com.example.nutrifill.data.AppDatabase
import com.example.nutrifill.data.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(context: Context) {
    private val foodDao = AppDatabase.getInstance(context).foodDao()

    suspend fun insertFood(food: FoodEntity): Long {
        return foodDao.insertFood(food)
    }

    fun getAllFoods(): Flow<List<FoodEntity>> {
        return foodDao.getAllFoods()
    }

    suspend fun getFoodById(foodId: Long): FoodEntity? {
        return foodDao.getFoodById(foodId)
    }

    suspend fun deleteFood(food: FoodEntity) {
        foodDao.deleteFood(food)
    }
}