package com.example.nutrifill.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nutrifill.data.entity.FoodEntity
import com.example.nutrifill.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FoodRepository(application)
    val allFoods: Flow<List<FoodEntity>> = repository.getAllFoods()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun insertFood(name: String, calories: Double, protein: Double, carbs: Double, fats: Double, imageUrl: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val food = FoodEntity(
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fats = fats,
                    imageUrl = imageUrl
                )
                repository.insertFood(food)
            } catch (e: Exception) {
                _error.value = "Failed to insert food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFood(food: FoodEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteFood(food)
            } catch (e: Exception) {
                _error.value = "Failed to delete food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _selectedFood = MutableLiveData<FoodEntity?>()
    val selectedFood: LiveData<FoodEntity?> = _selectedFood

    fun getFoodById(id: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _selectedFood.value = repository.getFoodById(id)
            } catch (e: Exception) {
                _error.value = "Failed to get food: ${e.message}"
                _selectedFood.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}