package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrifill.models.Nutrients
import com.example.nutrifill.models.ScanHistoryItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.AutoCompleteTextView

class ManualEntryActivity : AppCompatActivity() {
    private lateinit var foodNameInput: AutoCompleteTextView
    private lateinit var foodNameLayout: TextInputLayout
    private lateinit var caloriesInput: TextInputEditText
    private lateinit var caloriesLayout: TextInputLayout
    private lateinit var proteinInput: TextInputEditText
    private lateinit var proteinLayout: TextInputLayout
    private lateinit var carbsInput: TextInputEditText
    private lateinit var carbsLayout: TextInputLayout
    private lateinit var fatInput: TextInputEditText
    private lateinit var fatLayout: TextInputLayout
    private lateinit var portionSizeInput: TextInputEditText
    private lateinit var portionSizeLayout: TextInputLayout
    private lateinit var portionUnitSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var progressBar: ProgressBar
    private var portionSize: Float = 0f
    private var portionUnit: String = "serving"
    
    private val commonFoodItems = listOf(
        "Apple", "Banana", "Orange", "Chicken Breast", "Rice", "Bread",
        "Eggs", "Milk", "Yogurt", "Pasta", "Beef", "Fish", "Potato",
        "Carrot", "Broccoli", "Spinach", "Tomato", "Cheese"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        initializeViews()
        setupSpinner()
        setupSubmitButton()
    }

    private fun initializeViews() {
        foodNameInput = findViewById(R.id.food_name_input)
        foodNameLayout = findViewById(R.id.food_name_layout)
        caloriesInput = findViewById(R.id.calories_input)
        caloriesLayout = findViewById(R.id.calories_layout)
        proteinInput = findViewById(R.id.protein_input)
        proteinLayout = findViewById(R.id.protein_layout)
        carbsInput = findViewById(R.id.carbs_input)
        carbsLayout = findViewById(R.id.carbs_layout)
        fatInput = findViewById(R.id.fat_input)
        fatLayout = findViewById(R.id.fat_layout)
        portionSizeInput = findViewById(R.id.portion_size_input)
        portionSizeLayout = findViewById(R.id.portion_size_layout)
        portionUnitSpinner = findViewById(R.id.portion_unit_spinner)
        submitButton = findViewById(R.id.submit_button)
        progressBar = findViewById(R.id.progress_bar)
        
        setupTextWatchers()
        setupAutoComplete()
    }

    private fun setupSpinner() {
        val units = arrayOf("g", "ml", "oz", "cup", "tbsp", "tsp", "serving")
        ArrayAdapter(this, android.R.layout.simple_spinner_item, units).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            portionUnitSpinner.adapter = adapter
        }
        
        // Set default unit
        portionUnitSpinner.setSelection(units.indexOf("serving"))
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateInputs()) {
                saveNutritionData()
            }
        }
    }

    private fun setupTextWatchers() {
        foodNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateFoodName()
            }
        })

        portionSizeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePortionSize()
            }
        })

        caloriesInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateCalories()
            }
        })

        setupNutrientTextWatcher(proteinInput, proteinLayout, "protein")
        setupNutrientTextWatcher(carbsInput, carbsLayout, "carbs")
        setupNutrientTextWatcher(fatInput, fatLayout, "fat")
    }

    private fun setupNutrientTextWatcher(input: TextInputEditText, layout: TextInputLayout, nutrientName: String) {
        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString().toFloatOrNull()
                if (s.isNullOrBlank()) {
                    layout.error = "Please enter $nutrientName amount"
                } else if (value == null || value < 0) {
                    layout.error = "Please enter a valid $nutrientName amount"
                } else {
                    layout.error = null
                }
            }
        })
    }

    private fun setupAutoComplete() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, commonFoodItems)
        foodNameInput.setAdapter(adapter)
    }

    private fun validateInputs(): Boolean {
        return validateFoodName() &&
               validatePortionSize() &&
               validateCalories() &&
               validateNutrient(proteinInput, proteinLayout, "protein") &&
               validateNutrient(carbsInput, carbsLayout, "carbs") &&
               validateNutrient(fatInput, fatLayout, "fat")
    }

    private fun validateFoodName(): Boolean {
        return when {
            foodNameInput.text.isNullOrBlank() -> {
                foodNameLayout.error = "Please enter food name"
                false
            }
            else -> {
                foodNameLayout.error = null
                true
            }
        }
    }

    private fun validatePortionSize(): Boolean {
        val value = portionSizeInput.text.toString().toFloatOrNull()
        return when {
            portionSizeInput.text.isNullOrBlank() -> {
                portionSizeLayout.error = "Please enter portion size"
                false
            }
            value == null || value <= 0 -> {
                portionSizeLayout.error = "Please enter a valid portion size"
                false
            }
            else -> {
                portionSizeLayout.error = null
                true
            }
        }
    }

    private fun validateCalories(): Boolean {
        val value = caloriesInput.text.toString().toIntOrNull()
        return when {
            caloriesInput.text.isNullOrBlank() -> {
                caloriesLayout.error = "Please enter calories"
                false
            }
            value == null || value < 0 -> {
                caloriesLayout.error = "Please enter a valid calorie amount"
                false
            }
            else -> {
                caloriesLayout.error = null
                true
            }
        }
    }

    private fun validateNutrient(input: TextInputEditText, layout: TextInputLayout, nutrientName: String): Boolean {
        val value = input.text.toString().toFloatOrNull()
        return when {
            input.text.isNullOrBlank() -> {
                layout.error = "Please enter $nutrientName amount"
                false
            }
            value == null || value < 0 -> {
                layout.error = "Please enter a valid $nutrientName amount"
                false
            }
            else -> {
                layout.error = null
                true
            }
        }
    }

    private fun saveNutritionData() {
        progressBar.visibility = View.VISIBLE
        submitButton.isEnabled = false

        try {
            val foodName = foodNameInput.text.toString()
            val calories = caloriesInput.text.toString().toIntOrNull() ?: 0
            val protein = proteinInput.text.toString().toFloatOrNull() ?: 0f
            val carbs = carbsInput.text.toString().toFloatOrNull() ?: 0f
            val fat = fatInput.text.toString().toFloatOrNull() ?: 0f
            val portionSize = portionSizeInput.text.toString().toFloatOrNull() ?: 1f
            val portionUnit = portionUnitSpinner.selectedItem.toString()

            // Create nutrition data with portion size adjustment
            val nutrients = Nutrients(
                calories = (calories * portionSize).toInt(),
                protein = protein * portionSize,
                carbs = carbs * portionSize,
                fat = fat * portionSize
            )

            // Save to history and update nutrition tracking
            val scanHistoryItem = ScanHistoryItem(
                id = System.currentTimeMillis().toString(),
                foodName = foodName,
                _nutrients = nutrients,
                timestamp = System.currentTimeMillis()
            )
            
            // Save the scan history item
            saveToHistory(foodName, nutrients)

            // Return the food data to NutritionTrackingActivity
            val resultIntent = Intent().apply {
                putExtra("food_name", foodName)
                putExtra("calories", nutrients.calories)
                putExtra("protein", nutrients.protein)
                putExtra("carbohydrates", nutrients.carbs)
                putExtra("fat", nutrients.fat)
                putExtra("portion_size", portionSize)
                putExtra("portion_unit", portionUnit)
            }
            setResult(RESULT_OK, resultIntent)

            // Show success message
            Toast.makeText(this, "Food entry saved successfully", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            showError("Error saving data: ${e.message}")
            progressBar.visibility = View.GONE
            submitButton.isEnabled = true
        }
    }

    private fun saveToHistory(foodName: String, nutrients: Nutrients) {
        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
        val historyJson = sharedPref.getString("nutrition_history", "[]")
        val gson = Gson()
        val historyType = object : TypeToken<MutableList<Map<String, Any>>>() {}.type
        val historyList: MutableList<Map<String, Any>> = gson.fromJson(historyJson, historyType) ?: mutableListOf()

        // Add new entry with all required fields
        val newEntry = mapOf(
            "foodName" to foodName,
            "calories" to nutrients.calories.toDouble(),
            "protein" to nutrients.protein.toDouble(),
            "carbs" to nutrients.carbs.toDouble(),
            "fat" to nutrients.fat.toDouble(),
            "portionSize" to portionSize,
            "portionUnit" to portionUnit,
            "timestamp" to System.currentTimeMillis(),
            "nutrients" to mapOf(
                "calories" to nutrients.calories.toDouble(),
                "protein" to nutrients.protein.toDouble(),
                "carbs" to nutrients.carbs.toDouble(),
                "fat" to nutrients.fat.toDouble()
            )
        )
        historyList.add(0, newEntry)

        // Save updated history and update nutrition tracking
        sharedPref.edit().apply {
            putString("nutrition_history", gson.toJson(historyList))
            // Update daily nutrition tracking
            val currentProgress = sharedPref.getString("daily_nutrition_progress", null)
            if (currentProgress != null) {
                val progressType = object : TypeToken<Map<String, Double>>() {}.type
                val progress: MutableMap<String, Double> = gson.fromJson(currentProgress, progressType) ?: mutableMapOf()
                progress["calories"] = (progress["calories"] ?: 0.0) + nutrients.calories
                progress["protein"] = (progress["protein"] ?: 0.0) + nutrients.protein
                progress["carbs"] = (progress["carbs"] ?: 0.0) + nutrients.carbs
                progress["fat"] = (progress["fat"] ?: 0.0) + nutrients.fat
                putString("daily_nutrition_progress", gson.toJson(progress))
            }
            apply()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}