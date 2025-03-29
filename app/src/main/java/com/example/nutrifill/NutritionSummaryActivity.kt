package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NutritionSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutritionsummaryactivity)

        // Get data from Intent
        val bmi = intent.getFloatExtra("BMI", 0f)
        val dailyCalories = intent.getDoubleExtra("DAILY_CALORIES", 0.0)
        val isPregnant = intent.getBooleanExtra("IS_PREGNANT", false)

        // Update UI
        findViewById<TextView>(R.id.tv_bmi).text = "%.1f".format(bmi)
        val caloriesText = findViewById<TextView>(R.id.tv_calories)
        caloriesText.text = "%.0f kcal".format(dailyCalories)
        if (isPregnant) {
            caloriesText.append(" (Pregnancy adjusted)")
        }

        // Continue to ScanActivity
        findViewById<Button>(R.id.btn_continue).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
    }
}