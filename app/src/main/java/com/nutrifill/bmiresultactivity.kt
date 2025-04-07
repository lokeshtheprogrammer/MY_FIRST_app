package com.nutrifill
import com.example.nutrifill.R

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrifill.HomeActivity

class BMIResultActivity : AppCompatActivity() {
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var tvDailyCalories: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvFats: TextView
    private lateinit var btnStartTracking: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_result)

        // Initialize views
        initializeViews()

        // Get data from intent
        val bmi = intent.getFloatExtra("bmi", 0f)
        val bmiCategory = intent.getStringExtra("bmiCategory") ?: ""
        val dailyCalories = intent.getIntExtra("dailyCalories", 0)
        val protein = intent.getIntExtra("protein", 0)
        val carbs = intent.getIntExtra("carbs", 0)
        val fats = intent.getIntExtra("fats", 0)

        // Display the results
        displayResults(bmi, bmiCategory, dailyCalories, protein, carbs, fats)

        // Set up button click listeners
        setupButtons()
    }

    private fun initializeViews() {
        tvBmiValue = findViewById(R.id.tv_bmi_value)
        tvBmiCategory = findViewById(R.id.tv_bmi_category)
        tvDailyCalories = findViewById(R.id.tv_daily_calories)
        tvProtein = findViewById(R.id.tv_protein)
        tvCarbs = findViewById(R.id.tv_carbs)
        tvFats = findViewById(R.id.tv_fats)
        btnStartTracking = findViewById(R.id.btn_start_tracking)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun displayResults(bmi: Float, bmiCategory: String, dailyCalories: Int, protein: Int, carbs: Int, fats: Int) {
        tvBmiValue.text = String.format("%.1f", bmi)
        tvBmiCategory.text = bmiCategory
        tvDailyCalories.text = "Daily Calories: $dailyCalories kcal"
        tvProtein.text = "Protein: ${protein}g"
        tvCarbs.text = "Carbs: ${carbs}g"
        tvFats.text = "Fats: ${fats}g"
    }

    private fun setupButtons() {
        btnStartTracking.setOnClickListener {
            // Navigate to tracking screen
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnBack.setOnClickListener {
            // Go back to user details
            onBackPressed()
        }
    }
}