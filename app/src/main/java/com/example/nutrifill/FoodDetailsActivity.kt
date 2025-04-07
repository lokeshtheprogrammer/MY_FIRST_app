package com.example.nutrifill

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class FoodDetailsActivity : AppCompatActivity() {
    private lateinit var foodNameTextView: TextView
    private lateinit var foodDescriptionTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var nutritionFactsContainer: View
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.food_details_activity)

        // Initialize views
        foodNameTextView = findViewById(R.id.tv_food_name)
        foodDescriptionTextView = findViewById(R.id.tv_food_description)
        ingredientsTextView = findViewById(R.id.tv_ingredients)
        nutritionFactsContainer = findViewById(R.id.nutrition_facts_container)
        loadingIndicator = findViewById(R.id.loading_indicator)
        mapView = findViewById(R.id.map_view)

        // Initialize OpenStreetMap
        Configuration.getInstance().userAgentValue = applicationContext.packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)

        // Get food data from intent
        val foodId = intent.getStringExtra("FOOD_ID") ?: return
        loadFoodDetails(foodId)
    }

    private fun loadFoodDetails(foodId: String) {
        loadingIndicator.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Store foodId for future API implementation
                val apiEndpoint = "api/foods/$foodId"
                // TODO: Implement API calls to USDA FoodData and OpenFoodFacts using foodId
                // For now, using placeholder data
                val foodData = FoodData(
                    name = "Sample Food #$foodId",
                    description = "A delicious and nutritious food item",
                    calories = 250,
                    protein = 10.0,
                    carbs = 30.0,
                    fat = 8.0,
                    ingredients = "Ingredient 1, Ingredient 2, Ingredient 3"
                )

                displayFoodData(foodData)
                displayAmmaUnavagamLocations()
            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error loading food details: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            } finally {
                loadingIndicator.visibility = View.GONE
            }
        }
    }

    private fun displayFoodData(foodData: FoodData) {
        foodNameTextView.text = foodData.name
        foodDescriptionTextView.text = foodData.description
        ingredientsTextView.text = "Ingredients: ${foodData.ingredients}"

        // Add nutrition facts dynamically
        val nutritionItems = listOf(
            "Calories: ${foodData.calories} kcal",
            "Protein: ${foodData.protein}g",
            "Carbohydrates: ${foodData.carbs}g",
            "Fat: ${foodData.fat}g"
        )

        nutritionItems.forEach { item ->
            val textView = TextView(this).apply {
                text = item
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            (nutritionFactsContainer as android.widget.LinearLayout).addView(textView)
        }
    }

    private fun displayAmmaUnavagamLocations() {
        // Sample Amma Unavagam location (Chennai)
        val chennaiLocation = GeoPoint(13.0827, 80.2707)
        mapView.controller.setCenter(chennaiLocation)

        // Add markers for Amma Unavagam locations
        val marker = Marker(mapView).apply {
            position = chennaiLocation
            title = "Amma Unavagam"
            snippet = "Affordable meals available here"
        }
        mapView.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    data class FoodData(
        val name: String,
        val description: String,
        val calories: Int,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val ingredients: String
    )
}