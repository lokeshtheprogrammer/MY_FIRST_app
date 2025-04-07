package com.example.nutrifill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.R
import com.example.nutrifill.adapters.ScanHistoryAdapter
import com.example.nutrifill.databinding.HomeactivityBinding
import com.example.nutrifill.models.Nutrients
import com.example.nutrifill.models.ScanHistoryItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeactivityBinding   
    private lateinit var adapter: ScanHistoryAdapter
    private lateinit var aiRecommendationService: AIRecommendationService
    
    private val manualEntryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh the recent scans after manual entry
            setupRecentScans()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUserProfile()
        setupRecentScans()
        setupQuickActions()
        
        // Initialize AI recommendation service
        aiRecommendationService = AIRecommendationService(this)
        setupAIRecommendations()
    }

    private fun setupQuickActions() {
        binding.scanCard.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        binding.historyCard.setOnClickListener {
            startActivity(Intent(this, NutritionHistoryActivity::class.java))
        }

        binding.manualEntryCard.setOnClickListener {
            openManualEntry(it)
        }

        binding.recommendationsCard.setOnClickListener {
            try {
                val intent = Intent(this, RecommendationsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error opening Recommendations: ${e.message}")
                Toast.makeText(this, "Unable to open Recommendations. Please try again.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    fun openManualEntry(view: View) {
        try {
            // Navigate to manual entry screen with result
            val intent = Intent(this, ManualEntryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            manualEntryLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error opening Manual Entry: ${e.message}")
            Toast.makeText(this, "Unable to open Manual Entry. Please try again.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun setupUserProfile() {
        val userName = intent.getStringExtra("NAME") ?: "User"
        binding.tvWelcome.text = "Welcome, $userName!"
        
        // Set up BMI card click listener
        binding.bmiCard.setOnClickListener {
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("NAME", userName)
            intent.putExtra("OPEN_BMI", true)
            startActivity(intent)
        }
    }



    private fun setupRecentScans() {
        binding.recentScansRecycler.layoutManager = LinearLayoutManager(this)
        adapter = ScanHistoryAdapter()
        binding.recentScansRecycler.adapter = adapter

        // Add item decoration for better spacing
        binding.recentScansRecycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        loadRecentScans()
    }





    private fun loadRecentScans() {
        // Load recent scans from SharedPreferences
        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
        val historyJson = sharedPref.getString("nutrition_history", "[]") ?: "[]"
        val gson = Gson()
        val historyType = object : TypeToken<List<Map<String, Any>>>() {}.type
        @Suppress("UNCHECKED_CAST")
        val historyList: List<Map<String, Any>> = gson.fromJson(historyJson, historyType) ?: listOf()

        // Convert to ScanHistoryItem objects and take only recent 5 items
        val recentScans = historyList.take(5).mapIndexed { index, item ->
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
                timestamp = (item["timestamp"] as? Double)?.toLong() ?: System.currentTimeMillis()
            )
        }

        adapter.submitList(recentScans)
    }



    private fun handleLogout() {
        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        startActivity(Intent(this, FstActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }



    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            showOverflowMenu(it)
        }
    }

    private fun showOverflowMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.overflow_menu, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_nutrition_tracking -> {
                    startActivity(Intent(this, NutritionTrackingActivity::class.java))
                    true
                }
                R.id.menu_scan_food -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    true
                }
                R.id.menu_history -> {
                    startActivity(Intent(this, NutritionHistoryActivity::class.java))
                    true
                }
                R.id.menu_edit_profile -> {
                    val intent = Intent(this, UserDetailsActivity::class.java)
                    intent.putExtra("NAME", intent.getStringExtra("NAME"))
                    startActivity(intent)
                    true
                }
                R.id.menu_bmi_calculator -> {
                    val intent = Intent(this, UserDetailsActivity::class.java)
                    intent.putExtra("NAME", intent.getStringExtra("NAME"))
                    intent.putExtra("OPEN_BMI", true)
                    startActivity(intent)
                    true
                }
                R.id.menu_amma_unavagam -> {
                    try {
                        val intent = Intent(this, AmmaUnavagamActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error opening Amma Unavagam: ${e.message}")
                        Toast.makeText(this, "Unable to open Amma Unavagam. Please try again.", Toast.LENGTH_SHORT).show()
                        // Report error to analytics or crash reporting service
                        e.printStackTrace()
                    }
                    true
                }
                R.id.menu_settings -> {
                    try {
                        val settingsIntent = Intent(this, SettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        startActivity(settingsIntent)
                    } catch (e: Exception) {
                        Log.e("HomeActivity", "Error opening Settings: ${e.message}")
                        Toast.makeText(this, "Unable to open Settings. Please try again.", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                    true
                }
                R.id.menu_logout -> {
                    handleLogout()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }



    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Already on home
                    true
                }
                R.id.navigation_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this, NutritionHistoryActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, UserDetailsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        // Set home as selected
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun updateItems(items: List<ScanHistoryItem>) {
        // Update the adapter with new items
        adapter.submitList(items)
        
        // Update visibility based on list content
        binding.apply {
            if (items.isEmpty()) {
                recentScansTitle.visibility = View.GONE
                recentScansRecycler.visibility = View.GONE
            } else {
                recentScansTitle.visibility = View.VISIBLE
                recentScansRecycler.visibility = View.VISIBLE
            }
        }
    }

    private fun setupAIRecommendations() {
        // Get personalized recommendations
        val recommendations = aiRecommendationService.getPersonalizedMealRecommendations()
        if (recommendations.isNotEmpty()) {
            binding.recommendationsCard.visibility = View.VISIBLE
            binding.recommendationsCard.setOnClickListener {
                val intent = Intent(this, RecommendationsActivity::class.java)
                intent.putStringArrayListExtra("recommendations", ArrayList(recommendations))
                startActivity(intent)
            }
        }

        // Analyze nutritional trends
        val trends = aiRecommendationService.analyzeNutritionalTrends()
        trends["calories"]?.let { caloriesTrend ->
            // Update UI with trends
            binding.tvNutritionInsight.text = caloriesTrend
            binding.tvNutritionInsight.visibility = View.VISIBLE
        }
    }
}