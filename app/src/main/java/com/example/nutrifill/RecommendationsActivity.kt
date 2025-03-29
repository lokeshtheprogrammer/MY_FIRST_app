package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecommendationsActivity : AppCompatActivity() {
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var tvDeficiencies: TextView
    private lateinit var btnBackToHome: Button

    companion object {
        private const val TAG = "RecommendationsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendationsactivity)
        Log.d(TAG, "onCreate: RecommendationsActivity started")

        // Initialize views
        rvRecommendations = findViewById(R.id.rv_recommendations)
        tvDeficiencies = findViewById(R.id.tv_deficiencies)
        btnBackToHome = findViewById(R.id.btn_back_to_home)
        Log.d(TAG, "Views initialized")

        // Display deficiencies
        val deficienciesText = intent.getStringExtra("DEFICIENCIES") ?: "No deficiencies detected!"
        tvDeficiencies.text = deficienciesText
        Log.d(TAG, "Deficiencies set: $deficienciesText")

        // Generate recommendations based on deficiencies
        val recommendations = generateRecommendations(deficienciesText)
        rvRecommendations.layoutManager = LinearLayoutManager(this)
        rvRecommendations.adapter = RecommendationsAdapter(recommendations)
        Log.d(TAG, "RecyclerView set with ${recommendations.size} items")

        // Back to Home button
        btnBackToHome.setOnClickListener {
            Log.d(TAG, "Back to Home clicked")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Simple static food recommendation logic
    private fun generateRecommendations(deficienciesText: String): List<String> {
        val recommendations = mutableListOf<String>()
        if (deficienciesText.contains("Calories")) recommendations.add("Whole Grain Bread (200 kcal/slice)")
        if (deficienciesText.contains("Protein")) recommendations.add("Chicken Breast (26g protein/100g)")
        if (deficienciesText.contains("Carbs")) recommendations.add("Brown Rice (23g carbs/100g)")
        if (deficienciesText.contains("Fat")) recommendations.add("Avocado (15g fat/100g)")
        if (recommendations.isEmpty()) recommendations.add("Your nutrient intake is balanced!")
        return recommendations
    }
}

// RecyclerView Adapter for Recommendations
class RecommendationsAdapter(private val recommendationsList: List<String>) :
    RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecommendation: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvRecommendation.text = recommendationsList[position]
    }

    override fun getItemCount(): Int = recommendationsList.size
}