package com.example.nutrifill.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.nutrifill.R

class FoodEntryActivity : AppCompatActivity() {
    private lateinit var foodNameInput: TextInputEditText
    private lateinit var portionSizeInput: TextInputEditText
    private lateinit var portionUnitSpinner: Spinner
    private lateinit var caloriesInput: TextInputEditText
    private lateinit var proteinInput: TextInputEditText
    private lateinit var carbsInput: TextInputEditText
    private lateinit var fatInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        initializeViews()
        setupPortionUnitSpinner()
    }

    private fun initializeViews() {
        foodNameInput = findViewById(R.id.food_name_input)
        portionSizeInput = findViewById(R.id.portion_size_input)
        portionUnitSpinner = findViewById(R.id.portion_unit_spinner)
        caloriesInput = findViewById(R.id.calories_input)
        proteinInput = findViewById(R.id.protein_input)
        carbsInput = findViewById(R.id.carbs_input)
        fatInput = findViewById(R.id.fat_input)
    }

    private fun setupPortionUnitSpinner() {
        val units = arrayOf("g", "ml", "oz", "cups")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        portionUnitSpinner.adapter = adapter
    }

    fun onScanButtonClick(view: View) {
        // TODO: Implement scanner functionality
        // This will be integrated with the existing scanner implementation
    }

    fun onSaveButtonClick(view: View) {
        if (validateInputs()) {
            saveNutritionalInfo()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (foodNameInput.text.isNullOrBlank()) {
            (foodNameInput.parent.parent as TextInputLayout).error = "Food name is required"
            isValid = false
        }

        if (portionSizeInput.text.isNullOrBlank()) {
            (portionSizeInput.parent.parent as TextInputLayout).error = "Portion size is required"
            isValid = false
        }

        return isValid
    }

    private fun saveNutritionalInfo() {
        // TODO: Implement saving logic
        // This will save the nutritional information to the database
        // and handle both manual entry and scanned data
    }

    companion object {
        const val SCANNER_REQUEST_CODE = 100
    }
}