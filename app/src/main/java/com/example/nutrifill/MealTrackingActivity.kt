package com.example.nutrifill

import android.content.Intent
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrifill.databinding.ActivityMealTrackingBinding
import com.example.nutrifill.adapters.MealsAdapter
import com.example.nutrifill.viewmodels.MealTrackingViewModel
import com.example.nutrifill.models.CalorieProgress
import com.example.nutrifill.models.Meal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MealTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMealTrackingBinding
    private lateinit var mealsAdapter: MealsAdapter
    private val viewModel: MealTrackingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        mealsAdapter = MealsAdapter()
        binding.mealsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MealTrackingActivity)
            adapter = mealsAdapter
        }

        binding.fabAddMeal.setOnClickListener {
            showAddMealDialog()
        }
    }

    private fun observeData() {
        viewModel.dailyCalories.observe(this) { calories ->
            binding.caloriesProgress.progress = calories.percentProgress
            binding.caloriesText.text = "${calories.consumed} / ${calories.target} kcal"
        }

        viewModel.meals.observe(this) { meals ->
            mealsAdapter.submitList(meals)
        }
    }

    private fun showAddMealDialog() {
        val options = arrayOf("Scan Food", "Manual Entry", "Choose from Library")
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Meal")
            .setItems(options) { dialog: DialogInterface, which: Int ->
                when (which) {
                    0 -> startActivity(Intent(this, ScannerActivity::class.java))
                    1 -> showManualEntryDialog()
                    2 -> showFoodLibrary()
                }
            }
            .show()
    }

    private fun showManualEntryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manual_entry, null)
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Food Manually")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog: DialogInterface, _: Int ->
                val foodName = dialogView.findViewById<TextInputEditText>(R.id.food_name).text.toString()
                val calories = dialogView.findViewById<TextInputEditText>(R.id.calories).text.toString().toIntOrNull() ?: 0
                viewModel.addMeal(foodName, calories)
            }
            .setNegativeButton("Cancel") { _: DialogInterface, _: Int -> }
            .show()
    }

    private fun showFoodLibrary() {
        startActivity(Intent(this, FoodLibraryActivity::class.java))
    }
}