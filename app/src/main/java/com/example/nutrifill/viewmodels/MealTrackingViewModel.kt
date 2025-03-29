package com.example.nutrifill.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nutrifill.models.CalorieProgress
import com.example.nutrifill.models.Meal

class MealTrackingViewModel : ViewModel() {
    private val _dailyCalories = MutableLiveData<CalorieProgress>()
    val dailyCalories: LiveData<CalorieProgress> = _dailyCalories

    private val _meals = MutableLiveData<List<Meal>>()
    val meals: LiveData<List<Meal>> = _meals

    fun addMeal(name: String, calories: Int) {
        // TODO: Implement meal addition logic
    }
}