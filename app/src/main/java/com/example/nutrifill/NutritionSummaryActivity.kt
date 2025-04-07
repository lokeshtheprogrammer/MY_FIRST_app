package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class NutritionSummaryActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var foodNameText: TextView
    private lateinit var portionText: TextView
    private lateinit var caloriesText: TextView
    private lateinit var proteinText: TextView
    private lateinit var carbsText: TextView
    private lateinit var fatText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nutritionsummaryactivity)

        initializeViews()
        setupMapView()
        displayNutritionData()
        setupButtons()
    }

    private fun initializeViews() {
        mapView = findViewById(R.id.map_view)
        foodNameText = findViewById(R.id.tv_food_name)
        portionText = findViewById(R.id.tv_portion)
        caloriesText = findViewById(R.id.tv_calories)
        proteinText = findViewById(R.id.tv_protein)
        carbsText = findViewById(R.id.tv_carbs)
        fatText = findViewById(R.id.tv_fat)
    }

    private fun setupMapView() {
        Configuration.getInstance().userAgentValue = applicationContext.packageName
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)

        // Chennai center coordinates
        val chennaiCenter = GeoPoint(13.0827, 80.2707)
        mapView.controller.setCenter(chennaiCenter)

        // Add Amma Unavagam locations
        addAmmaUnavagamLocations()
    }

    private fun addAmmaUnavagamLocations() {
        val locations = listOf(
            Pair(GeoPoint(13.0827, 80.2707), "Amma Unavagam - Central"),
            Pair(GeoPoint(13.0850, 80.2750), "Amma Unavagam - North"),
            Pair(GeoPoint(13.0800, 80.2680), "Amma Unavagam - South")
        )

        locations.forEach { (location, markerTitle) ->
            val marker = Marker(mapView).apply {
                position = location
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = markerTitle
                snippet = "Affordable meals available"
            }
            mapView.overlays.add(marker)
        }
    }

    private fun displayNutritionData() {
        val foodName = intent.getStringExtra("FOOD_NAME") ?: "Unknown Food"
        val calories = intent.getIntExtra("CALORIES", 0)
        val protein = intent.getFloatExtra("PROTEIN", 0f)
        val carbs = intent.getFloatExtra("CARBS", 0f)
        val fat = intent.getFloatExtra("FAT", 0f)
        val portion = intent.getStringExtra("PORTION") ?: ""

        foodNameText.text = foodName
        portionText.text = "Portion: $portion"
        caloriesText.text = "Calories: $calories kcal"
        proteinText.text = "Protein: $protein g"
        carbsText.text = "Carbs: $carbs g"
        fatText.text = "Fat: $fat g"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_view_history).setOnClickListener {
            startActivity(Intent(this, NutritionHistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btn_new_entry).setOnClickListener {
            startActivity(Intent(this, FoodScannerActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}