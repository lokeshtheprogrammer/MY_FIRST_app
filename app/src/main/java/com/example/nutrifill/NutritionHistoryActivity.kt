package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NutritionHistoryActivity : AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvNutrientSummary: TextView
    private lateinit var btnBackToHome: Button
    private val scanHistory = mutableListOf<NutrientData>()
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "NutriFillPrefs"
        private const val KEY_SCAN_HISTORY = "scan_history"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutritionhistoryactivity)

        // Initialize views
        rvHistory = findViewById(R.id.rv_history)
        tvNutrientSummary = findViewById(R.id.tv_nutrient_summary)
        btnBackToHome = findViewById(R.id.btn_back_to_home)

        // Set welcome message with username
        val username = intent.getStringExtra("NAME") ?: "User"
        findViewById<TextView>(R.id.history_heading).text = "Welcome, $username!"

        // Load saved scan history
        loadScanHistory()

        // Add new scan result if received
        val newScanResult = intent.getStringExtra("SCAN_RESULT")
        if (!newScanResult.isNullOrEmpty()) {
            val nutrientData = parseNutrientData(newScanResult)
            scanHistory.add(nutrientData)
            saveScanHistory()
        }

        // Update RecyclerView with scan history
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = HistoryAdapter(scanHistory)

        // Calculate and display total nutrients
        updateNutrientSummary()

        // Back to Home button
        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Parse scan result string into NutrientData
    private fun parseNutrientData(scanResult: String): NutrientData {
        val lines = scanResult.split("\n")
        var calories = 0
        var protein = 0f
        var carbs = 0f
        var fat = 0f

        lines.forEach { line ->
            when {
                line.contains("Calories") -> calories = "Calories: (\\d+) kcal".toRegex().find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                line.contains("Protein") -> protein = "Protein: (\\d+\\.?\\d*)g".toRegex().find(line)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                line.contains("Carbs") -> carbs = "Carbs: (\\d+\\.?\\d*)g".toRegex().find(line)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                line.contains("Fat") -> fat = "Fat: (\\d+\\.?\\d*)g".toRegex().find(line)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
            }
        }
        return NutrientData(calories, protein, carbs, fat)
    }

    // Update nutrient summary display
    private fun updateNutrientSummary() {
        val totalCalories = scanHistory.sumOf { it.calories }
        val totalProtein = scanHistory.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarbs = scanHistory.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = scanHistory.sumOf { it.fat.toDouble() }.toFloat()

        tvNutrientSummary.text = "Calories: $totalCalories kcal\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg".format(totalProtein, totalCarbs, totalFat)
    }

    // Save scan history to SharedPreferences
    private fun saveScanHistory() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = gson.toJson(scanHistory)
        prefs.edit().putString(KEY_SCAN_HISTORY, json).apply()
    }

    // Load scan history from SharedPreferences
    private fun loadScanHistory() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(KEY_SCAN_HISTORY, null)
        if (json != null) {
            val type = object : TypeToken<List<NutrientData>>() {}.type
            val savedHistory: List<NutrientData> = gson.fromJson(json, type)
            scanHistory.clear()
            scanHistory.addAll(savedHistory)
        }
    }
}

// Data class for nutrient data
data class NutrientData(
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float
) {
    override fun toString(): String {
        return "Calories: $calories kcal\nProtein: %.1fg\nCarbs: %.1fg\nFat: %.1fg".format(protein, carbs, fat)
    }
}

// RecyclerView Adapter
class HistoryAdapter(private val historyList: List<NutrientData>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMeal: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvMeal.text = historyList[position].toString()
    }

    override fun getItemCount(): Int = historyList.size
}