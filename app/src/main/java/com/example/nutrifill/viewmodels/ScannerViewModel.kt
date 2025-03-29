package com.example.nutrifill.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrifill.models.FoodDetails

class ScannerViewModel : ViewModel() {
    private val _scanResult = MutableLiveData<FoodDetails>()
    val scanResult: LiveData<FoodDetails> = _scanResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun analyzeImage(imageBytes: ByteArray) {
        _isLoading.value = true
        // TODO: Implement image analysis
    }

    private fun handleError(message: String) {
        _error.value = message
        _isLoading.value = false
    }
}