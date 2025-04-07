package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.adapters.ScanHistoryAdapter
import com.example.nutrifill.models.Nutrients
import com.example.nutrifill.models.ScanHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import com.example.nutrifill.network.ScanHistoryResponse

class NutritionHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ScanHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutritionhistoryactivity)

        recyclerView = findViewById(R.id.recycler_view)
        emptyStateView = findViewById(R.id.tv_empty_state)
        progressBar = findViewById(R.id.progress_bar)

        setupRecyclerView()
        loadNutritionHistory()
    }

    private fun setupRecyclerView() {
        adapter = ScanHistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadNutritionHistory() {
        progressBar.visibility = View.VISIBLE
        
        try {
            // Get nutrition history from SharedPreferences
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            val historyJson = sharedPref.getString("nutrition_history", "[]") ?: "[]"
            val gson = Gson()
            val historyType = object : TypeToken<List<Map<String, Any>>>() {}.type
            val historyList: List<Map<String, Any>> = gson.fromJson(historyJson, historyType) ?: listOf()
            
            // Convert to ScanHistoryItem objects
            val scanHistoryItems = historyList.mapIndexed { index, item ->
                val nutrientsMap = item["nutrients"] as? Map<String, Any> ?: mapOf()
                ScanHistoryItem(
                    id = index.toString(),
                    foodName = item["foodName"] as String,
                    _nutrients = Nutrients(
                        calories = (nutrientsMap["calories"] as? Double)?.toInt() ?: 0,
                        protein = (nutrientsMap["protein"] as? Double)?.toFloat() ?: 0f,
                        carbs = (nutrientsMap["carbs"] as? Double)?.toFloat() ?: 0f,
                        fat = (nutrientsMap["fat"] as? Double)?.toFloat() ?: 0f
                    ),
                    timestamp = System.currentTimeMillis() - (index * 3600000)
                )
            }
            
            updateHistoryDisplay(scanHistoryItems)
        } catch (e: Exception) {
            Log.e("NutritionHistory", "Error loading history: ${e.message}")
            Toast.makeText(
                this@NutritionHistoryActivity,
                "Error loading history: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            progressBar.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        }
    }

    private fun updateHistoryDisplay(scanHistory: List<ScanHistoryItem>) {
        progressBar.visibility = View.GONE
        
        if (scanHistory.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            adapter.submitList(scanHistory)
        }
    }

    fun showNutritionDetails(item: ScanHistoryItem) {
        val intent = Intent(this, NutritionSummaryActivity::class.java)
        intent.putExtra("SCAN_DATA", item)
        startActivity(intent)
    }
}