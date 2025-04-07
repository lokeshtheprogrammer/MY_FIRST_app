package com.example.nutrifill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nutrifill.model.DailyNutritionGoal
import com.example.nutrifill.model.FoodItem
import com.example.nutrifill.model.NutritionProgress
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrifill.adapters.FoodItemAdapter

class NutritionTrackingActivity : AppCompatActivity() {
    private lateinit var tvCaloriesProgress: TextView
    private lateinit var tvProteinProgress: TextView
    private lateinit var tvCarbsProgress: TextView
    private lateinit var tvFatsProgress: TextView
    private lateinit var progressBarCalories: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var progressBarProtein: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var progressBarCarbs: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var progressBarFats: com.google.android.material.progressindicator.LinearProgressIndicator
    private lateinit var btnScanFood: ExtendedFloatingActionButton
    private lateinit var rvFoodItems: RecyclerView
    private lateinit var tvDeficiencies: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var foodItemAdapter: FoodItemAdapter

    private var dailyGoal: DailyNutritionGoal? = null
    private var nutritionProgress = NutritionProgress()
    private var userId: String? = null
    private val CAMERA_PERMISSION_CODE = 100
    private val FOOD_SCANNER_REQUEST_CODE = 101
    private val MANUAL_ENTRY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_tracking)

        initializeViews()
        setupRecyclerView()

        findViewById<FloatingActionButton>(R.id.fab_manual_entry).setOnClickListener {
            startManualEntry()
        }

        intent.extras?.let {
            dailyGoal = DailyNutritionGoal(
                calories = it.getFloat("daily_calories", 2000f),
                protein = it.getFloat("daily_protein", 50f),
                carbohydrates = it.getFloat("daily_carbs", 250f),
                fats = it.getFloat("daily_fats", 70f)
            )
        }

        btnScanFood.setOnClickListener {
            if (checkCameraPermission()) {
                startFoodScanner()
            } else {
                requestCameraPermission()
            }
        }

        updateNutritionDisplay()
        loadNutritionProgress()
    }

    private fun initializeViews() {
        tvCaloriesProgress = findViewById(R.id.tv_calories_progress)
        tvProteinProgress = findViewById(R.id.tv_protein_progress)
        tvCarbsProgress = findViewById(R.id.tv_carbs_progress)
        tvFatsProgress = findViewById(R.id.tv_fats_progress)
        progressBarCalories = findViewById(R.id.progress_bar_calories)
        progressBarProtein = findViewById(R.id.progress_bar_protein)
        progressBarCarbs = findViewById(R.id.progress_bar_carbs)
        progressBarFats = findViewById(R.id.progress_bar_fats)
        btnScanFood = findViewById(R.id.btn_scan_food)
        rvFoodItems = findViewById(R.id.rv_food_items)
        tvDeficiencies = findViewById(R.id.tv_deficiencies)
        loadingIndicator = findViewById(R.id.loading_indicator)
    }

    private fun setupRecyclerView() {
        foodItemAdapter = FoodItemAdapter { foodItem ->
            // Handle food item click
            val intent = Intent(this, FoodDetailsActivity::class.java)
            intent.putExtra("FOOD_ID", foodItem.id)
            startActivity(intent)
        }
        rvFoodItems.apply {
            layoutManager = LinearLayoutManager(this@NutritionTrackingActivity)
            adapter = foodItemAdapter
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun startFoodScanner() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, FOOD_SCANNER_REQUEST_CODE)
    }

    private fun startManualEntry() {
        val intent = Intent(this, ManualEntryActivity::class.java)
        startActivityForResult(intent, MANUAL_ENTRY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                FOOD_SCANNER_REQUEST_CODE -> {
                    data?.let { handleScannerResult(it) }
                }

                MANUAL_ENTRY_REQUEST_CODE -> {
                    data?.let { handleManualEntryResult(it) }
                }
            }
            // Refresh UI after any successful result
            updateNutritionDisplay()
            loadNutritionProgress()
        }
    }

    private fun handleScannerResult(data: Intent) {
        val foodName = data.getStringExtra("food_name") ?: return
        val calories = data.getFloatExtra("calories", 0f)
        val protein = data.getFloatExtra("protein", 0f)
        val carbs = data.getFloatExtra("carbohydrates", 0f)
        val fats = data.getFloatExtra("fat", 0f)
        val portionSize = data.getFloatExtra("portion_size", 1f)
        val portionUnit = data.getStringExtra("portion_unit") ?: "serving"

        // Add food item to the list
        val foodItem = FoodItem(
            id = System.currentTimeMillis().toString(),
            name = foodName,
            calories = calories,
            protein = protein,
            carbohydrates = carbs,
            fats = fats,
            servingSize = portionSize,
            servingUnit = portionUnit,
            timestamp = System.currentTimeMillis().toString()
        )

        // Update the adapter with the new food item
        val currentList = foodItemAdapter.currentList.toMutableList()
        currentList.add(0, foodItem)
        foodItemAdapter.submitList(currentList)

        // Update nutrition progress
        nutritionProgress.consumedCalories += calories
        nutritionProgress.consumedProtein += protein
        nutritionProgress.consumedCarbohydrates += carbs
        nutritionProgress.consumedFats += fats

        // Save to shared preferences
        saveNutritionProgress()
        updateNutritionDisplay()
    }

    private fun handleManualEntryResult(data: Intent) {
        val foodName = data.getStringExtra("food_name") ?: return
        val calories = data.getFloatExtra("calories", 0f)
        val protein = data.getFloatExtra("protein", 0f)
        val carbs = data.getFloatExtra("carbohydrates", 0f)
        val fats = data.getFloatExtra("fat", 0f)
        val portionSize = data.getFloatExtra("portion_size", 1f)
        val portionUnit = data.getStringExtra("portion_unit") ?: "serving"

        // Add food item to the list
        val foodItem = FoodItem(
            id = System.currentTimeMillis().toString(),
            name = foodName,
            calories = calories,
            protein = protein,
            carbohydrates = carbs,
            fats = fats,
            servingSize = portionSize,
            servingUnit = portionUnit,
            timestamp = System.currentTimeMillis().toString()
        )

        // Update the adapter with the new food item
        val currentList = foodItemAdapter.currentList.toMutableList()
        currentList.add(0, foodItem)
        foodItemAdapter.submitList(currentList)

        // Update nutrition progress
        nutritionProgress.consumedCalories += calories
        nutritionProgress.consumedProtein += protein
        nutritionProgress.consumedCarbohydrates += carbs
        nutritionProgress.consumedFats += fats

        // Save to shared preferences
        saveNutritionProgress()
        updateNutritionDisplay()
    }

    private fun saveNutritionProgress() {
        try {
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            val foodItems = foodItemAdapter.currentList
            val historyList = foodItems.map { item ->
                mapOf(
                    "id" to item.id,
                    "foodName" to item.name,
                    "calories" to item.calories,
                    "protein" to item.protein,
                    "carbohydrates" to item.carbohydrates,
                    "fats" to item.fats,
                    "servingSize" to item.servingSize,
                    "servingUnit" to item.servingUnit,
                    "timestamp" to item.timestamp
                )
            }

            with(sharedPref.edit()) {
                putString("nutrition_history", Gson().toJson(historyList))
                commit()
            }
            
            // Update UI immediately after saving
            updateNutritionDisplay()
        } catch (e: Exception) {
            Log.e("NutritionTracking", "Error saving nutrition progress: ${e.message}")
            Toast.makeText(this, "Failed to save nutrition data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNutritionDisplay() {
        dailyGoal?.let { goal ->
            val caloriesPercentage =
                (nutritionProgress.consumedCalories / goal.calories * 100).toInt()
            val proteinPercentage =
                (nutritionProgress.consumedProtein / goal.protein * 100).toInt()
            val carbsPercentage =
                (nutritionProgress.consumedCarbohydrates / goal.carbohydrates * 100).toInt()
            val fatsPercentage = (nutritionProgress.consumedFats / goal.fats * 100).toInt()

            progressBarCalories.progress = caloriesPercentage
            progressBarProtein.progress = proteinPercentage
            progressBarCarbs.progress = carbsPercentage
            progressBarFats.progress = fatsPercentage

            tvCaloriesProgress.text =
                "${nutritionProgress.consumedCalories.toInt()}/${goal.calories.toInt()} kcal ($caloriesPercentage%)"
            tvProteinProgress.text =
                "${nutritionProgress.consumedProtein.toInt()}/${goal.protein.toInt()}g ($proteinPercentage%)"
            tvCarbsProgress.text =
                "${nutritionProgress.consumedCarbohydrates.toInt()}/${goal.carbohydrates.toInt()}g ($carbsPercentage%)"
            tvFatsProgress.text =
                "${nutritionProgress.consumedFats.toInt()}/${goal.fats.toInt()}g ($fatsPercentage%)"

            val deficiencies = mutableListOf<String>()
            if (nutritionProgress.consumedCalories < goal.calories * 0.8) deficiencies.add("Calories")
            if (nutritionProgress.consumedProtein < goal.protein * 0.8) deficiencies.add("Protein")
            if (nutritionProgress.consumedCarbohydrates < goal.carbohydrates * 0.8) deficiencies.add(
                "Carbohydrates"
            )
            if (nutritionProgress.consumedFats < goal.fats * 0.8) deficiencies.add("Fats")

            if (deficiencies.isNotEmpty()) {
                tvDeficiencies.visibility = View.VISIBLE
                tvDeficiencies.text = "Deficient in: ${deficiencies.joinToString(", ")}"
            } else {
                tvDeficiencies.visibility = View.GONE
            }
        }

        foodItemAdapter.updateItems(nutritionProgress.foodItems)
    }

    private fun loadNutritionProgress() {
        try {
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            val historyJson = sharedPref.getString("nutrition_history", "[]") ?: "[]"
            val gson = Gson()
            val historyType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val historyList: List<Map<String, Any>> = gson.fromJson(historyJson, historyType)

            // Convert history items to FoodItems
            val foodItems = historyList.map { item ->
                FoodItem(
                    id = item["id"]?.toString() ?: System.currentTimeMillis().toString(),
                    name = item["foodName"] as? String ?: "",
                    calories = (item["calories"] as? Double)?.toFloat() ?: 0f,
                    protein = (item["protein"] as? Double)?.toFloat() ?: 0f,
                    carbohydrates = (item["carbohydrates"] as? Double)?.toFloat() ?: 0f,
                    fats = (item["fats"] as? Double)?.toFloat() ?: 0f,
                    servingSize = (item["servingSize"] as? Double)?.toFloat() ?: 1f,
                    servingUnit = item["servingUnit"] as? String ?: "serving",
                    timestamp = item["timestamp"]?.toString() ?: System.currentTimeMillis().toString()
                )
            }

            // Update the adapter
            foodItemAdapter.submitList(foodItems)

            // Calculate total nutrition values
            nutritionProgress.apply {
                consumedCalories = foodItems.sumOf { it.calories.toDouble() }.toFloat()
                consumedProtein = foodItems.sumOf { it.protein.toDouble() }.toFloat()
                consumedCarbohydrates = foodItems.sumOf { it.carbohydrates.toDouble() }.toFloat()
                consumedFats = foodItems.sumOf { it.fats.toDouble() }.toFloat()
            }

            updateNutritionDisplay()
        } catch (e: Exception) {
            Log.e("NutritionTracking", "Error loading nutrition progress: ${e.message}")
            Toast.makeText(this, "Failed to load nutrition data", Toast.LENGTH_SHORT).show()
            nutritionProgress = NutritionProgress() // Reset to default if loading fails
            foodItemAdapter.submitList(emptyList())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFoodScanner()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to scan food items",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveNutritionHistoryItem(foodItem: FoodItem) {
        // Create a nutrition history item
    }
}