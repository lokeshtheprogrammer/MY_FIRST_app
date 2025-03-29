package com.example.nutrifill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrifill.data.entity.FoodEntity
import com.example.nutrifill.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository(application)
    val allFoods: Flow<List<FoodEntity>> = repository.getAllFoods()

    fun insertFood(name: String, calories: Double, protein: Double, carbs: Double, fats: Double, imageUrl: String? = null) {
        viewModelScope.launch {
            val food = FoodEntity(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                imageUrl = imageUrl
            )
            repository.insertFood(food)
        }
    }

    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch {
            repository.deleteFood(food)
        }
    }
}