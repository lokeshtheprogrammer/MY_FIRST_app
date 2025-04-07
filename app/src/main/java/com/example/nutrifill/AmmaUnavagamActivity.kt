package com.example.nutrifill

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.adapters.FoodRecommendationAdapter
import com.example.nutrifill.models.FoodRecommendation
import com.example.nutrifill.repository.FoodRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AmmaUnavagamActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var adapter: FoodRecommendationAdapter
    private lateinit var foodRepository: FoodRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_amma_unavagam)

            // Initialize repository
            foodRepository = FoodRepository(this)

            // Initialize views
            initializeViews()

            // Set up RecyclerView
            setupRecyclerView()

            // Load Amma Unavagam menu items
            loadMenuItems()
        } catch (e: Exception) {
            Log.e("AmmaUnavagamActivity", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Unable to initialize Amma Unavagam. Please try again.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rv_menu_items)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.tv_empty_view)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoodRecommendationAdapter { foodItem ->
            // Handle food item click
            showOrderDialog(foodItem)
        }
        recyclerView.adapter = adapter
    }

    private fun loadMenuItems() {
        if (!::progressBar.isInitialized || !::recyclerView.isInitialized || !::emptyView.isInitialized) {
            Log.e("AmmaUnavagamActivity", "Views not properly initialized")
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load menu items from repository
                val menuItems = withContext(Dispatchers.IO) {
                    try {
                        foodRepository.getAmmaUnavagamMenu()
                    } catch (e: Exception) {
                        Log.e("AmmaUnavagamActivity", "Error loading menu: ${e.message}")
                        null
                    }
                } ?: listOf(
                    FoodRecommendation(
                        name = "Idli",
                        description = "2 pieces of soft steamed rice cakes",
                        price = "₹1",
                        portion = "2 pieces",
                        imageResource = R.drawable.ic_food_placeholder
                    ),
                    FoodRecommendation(
                        name = "Pongal",
                        description = "Hot rice and lentil dish",
                        price = "₹5",
                        portion = "1 bowl",
                        imageResource = R.drawable.ic_food_placeholder
                    ),
                    FoodRecommendation(
                        name = "Sambar Rice",
                        description = "Rice mixed with lentil-based vegetable stew",
                        price = "₹5",
                        portion = "1 plate",
                        imageResource = R.drawable.ic_food_placeholder
                    ),
                    FoodRecommendation(
                        name = "Curd Rice",
                        description = "Rice mixed with yogurt",
                        price = "₹3",
                        portion = "1 plate",
                        imageResource = R.drawable.ic_food_placeholder
                    ),
                    FoodRecommendation(
                        name = "Chapati",
                        description = "2 pieces with kurma",
                        price = "₹3",
                        portion = "2 pieces",
                        imageResource = R.drawable.ic_food_placeholder
                    )
                )

                withContext(Dispatchers.Main) {
                    if (menuItems.isEmpty()) {
                        showEmptyView()
                    } else {
                        showMenuItems(menuItems)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to load menu items")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun showMenuItems(items: List<FoodRecommendation>) {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        adapter.submitList(items)
    }

    private fun showEmptyView() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = "No menu items available"
    }

    private fun showError(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showOrderDialog(foodItem: FoodRecommendation) {
        // TODO: Implement order dialog
        Snackbar.make(recyclerView, "Ordered ${foodItem.name}", Snackbar.LENGTH_SHORT).show()
    }
}