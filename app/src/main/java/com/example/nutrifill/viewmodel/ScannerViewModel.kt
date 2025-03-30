package com.example.nutrifill.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrifill.models.FoodDetails
import com.example.nutrifill.repository.NutritionRepository
import kotlinx.coroutines.launch

class ScannerViewModel : ViewModel() {
    private val repository = NutritionRepository()
    
    private val _scanResult = MutableLiveData<FoodDetails>()
    val scanResult: LiveData<FoodDetails> = _scanResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun analyzeImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Implement your image analysis logic here using repository
                // For example:
                // val result = repository.analyzeFoodImage(imageBytes)
                // _scanResult.value = result
            } catch (e: Exception) {
                _error.value = "Failed to analyze image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}