package com.example.nutrifill

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.adapters.FoodRecommendationAdapter
import com.example.nutrifill.models.Nutrients
import com.example.nutrifill.models.ScanHistoryItem
import com.example.nutrifill.models.DailyNutritionGoals
import com.example.nutrifill.models.FoodRecommendation
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.example.nutrifill.service.RecommendationService

class NutritionDashboardActivity : AppCompatActivity() {
    // Add recommendationService initialization
    private val recommendationService = RecommendationService()
    
    private lateinit var caloriesProgress: LinearProgressIndicator
    private lateinit var proteinProgress: LinearProgressIndicator
    private lateinit var carbsProgress: LinearProgressIndicator
    private lateinit var fatsProgress: LinearProgressIndicator
    
    private lateinit var caloriesValue: TextView
    private lateinit var proteinValue: TextView
    private lateinit var carbsValue: TextView
    private lateinit var fatsValue: TextView
    
    private lateinit var caloriesGoal: TextView
    private lateinit var proteinGoal: TextView
    private lateinit var carbsGoal: TextView
    private lateinit var fatsGoal: TextView
    
    private lateinit var deficiencyCard: CardView
    private lateinit var deficiencyText: TextView
    private lateinit var recommendationsRecyclerView: RecyclerView
    private lateinit var recommendationsAdapter: FoodRecommendationAdapter

    // Default daily goals (can be customized based on user profile)
    private val dailyGoals = DailyNutritionGoals(
        targetNutrients = Nutrients(
            calories = 2000,
            protein = 50f,
            carbs = 275f,
            fat = 55f
        ),
        currentNutrients = Nutrients()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_dashboard)
        
        initializeViews()
        setupRecyclerView()
        
        // Get scanned food data from intent
        val scannedFood = intent.getParcelableExtra("SCANNED_FOOD", ScanHistoryItem::class.java)
        scannedFood?.let {
            updateNutritionDisplay(it.nutrients)
            analyzeDeficiencies(it.nutrients)
        }
    }

    private fun initializeViews() {
        caloriesProgress = findViewById(R.id.calories_progress)
        proteinProgress = findViewById(R.id.protein_progress)
        carbsProgress = findViewById(R.id.carbs_progress)
        fatsProgress = findViewById(R.id.fats_progress)
        
        caloriesValue = findViewById(R.id.calories_value)
        proteinValue = findViewById(R.id.protein_value)
        carbsValue = findViewById(R.id.carbs_value)
        fatsValue = findViewById(R.id.fats_value)
        
        caloriesGoal = findViewById(R.id.calories_goal)
        proteinGoal = findViewById(R.id.protein_goal)
        carbsGoal = findViewById(R.id.carbs_goal)
        fatsGoal = findViewById(R.id.fats_goal)
        
        deficiencyCard = findViewById(R.id.deficiency_card)
        deficiencyText = findViewById(R.id.deficiency_text)
        
        // Set goals text
        caloriesGoal.text = "/${dailyGoals.targetNutrients.calories} kcal"
        proteinGoal.text = "/${dailyGoals.targetNutrients.protein}g"
        carbsGoal.text = "/${dailyGoals.targetNutrients.carbs}g"
        fatsGoal.text = "/${dailyGoals.targetNutrients.fat}g"
    }

    private fun setupRecyclerView() {
        recommendationsRecyclerView = findViewById(R.id.recommendations_recycler_view)
        recommendationsRecyclerView.layoutManager = LinearLayoutManager(this)
        recommendationsAdapter = FoodRecommendationAdapter { recommendation ->
            // Handle recommendation click
        }
        recommendationsRecyclerView.adapter = recommendationsAdapter
    }

    private fun updateNutritionDisplay(nutrients: Nutrients) {
        // Update progress bars
        caloriesProgress.progress = (nutrients.calories / dailyGoals.targetNutrients.calories * 100).toInt()
        proteinProgress.progress = (nutrients.protein / dailyGoals.targetNutrients.protein * 100).toInt()
        carbsProgress.progress = (nutrients.carbs / dailyGoals.targetNutrients.carbs * 100).toInt()
        fatsProgress.progress = (nutrients.fat / dailyGoals.targetNutrients.fat * 100).toInt()
        
        // Update values
        caloriesValue.text = "${nutrients.calories}"
        proteinValue.text = "${nutrients.protein}g"
        carbsValue.text = "${nutrients.carbs}g"
        fatsValue.text = "${nutrients.fat}g"
    }

    private fun analyzeDeficiencies(nutrients: Nutrients) {
        val deficiencies = mutableListOf<String>()
        
        if (nutrients.protein < dailyGoals.targetNutrients.protein * 0.8) {
            deficiencies.add("Protein")
        }
        if (nutrients.carbs < dailyGoals.targetNutrients.carbs * 0.8) {
            deficiencies.add("Carbohydrates")
        }
        if (nutrients.fat < dailyGoals.targetNutrients.fat * 0.8) {
            deficiencies.add("Fats")
        }
        
        if (deficiencies.isNotEmpty()) {
            deficiencyCard.visibility = View.VISIBLE
            deficiencyText.text = "You may be low in: ${deficiencies.joinToString(", ")}"
            recommendFoods(deficiencies)
        } else {
            deficiencyCard.visibility = View.GONE
        }
    }

    private fun recommendFoods(deficiencies: List<String>) {
        val currentNutrients = Nutrients(
            calories = caloriesProgress.progress * dailyGoals.targetNutrients.calories / 100,
            protein = proteinProgress.progress * dailyGoals.targetNutrients.protein / 100,
            carbs = carbsProgress.progress * dailyGoals.targetNutrients.carbs / 100,
            fat = fatsProgress.progress * dailyGoals.targetNutrients.fat / 100
        )

        val recommendations = recommendationService.getRecommendations(
            currentNutrients,
            dailyGoals.targetNutrients
        )

        // Get meal plan suggestions
        val suggestions = recommendationService.getMealPlanSuggestions(currentNutrients, dailyGoals)
        
        // Update current nutrients in daily goals
        dailyGoals.currentNutrients.apply {
            calories = currentNutrients.calories
            protein = currentNutrients.protein
            carbs = currentNutrients.carbs
            fat = currentNutrients.fat
        }
        
        // Update the UI with recommendations and suggestions
        recommendationsAdapter.submitList(recommendations)
        
        // Fix the meal suggestions TextView ID
        findViewById<TextView>(R.id.tv_meal_suggestions).text = suggestions.joinToString("\n")
    }

    private fun getProteinRichFoods() = listOf(
        FoodRecommendation(
            name = "Chicken Breast",
            description = "High in protein, low in fat",
            price = "$5.99/lb",
            imageResource = R.drawable.chicken_breast,
            portion = "3-4 oz serving"
        ),
        FoodRecommendation(
            name = "Greek Yogurt",
            description = "Rich in protein and probiotics",
            price = "$1.99/cup",
            imageResource = R.drawable.greek_yogurt,
            portion = "1 cup serving"
        )
    )

    private fun getCarbRichFoods() = listOf(
        FoodRecommendation(
            name = "Brown Rice",
            description = "Complex carbs with fiber",
            price = "$2.99/lb",
            imageResource = R.drawable.brown_rice,
            portion = "1/2 cup serving"
        ),
        FoodRecommendation(
            name = "Sweet Potato",
            description = "Nutrient-rich carbohydrates",
            price = "$1.49/lb",
            imageResource = R.drawable.sweet_potato,
            portion = "1 medium potato"
        )
    )

    private fun getFatRichFoods() = listOf(
        FoodRecommendation(
            name = "Avocado",
            description = "Healthy fats and fiber",
            price = "$1.99 each",
            imageResource = R.drawable.avocado,
            portion = "1/2 avocado"
        ),
        FoodRecommendation(
            name = "Almonds",
            description = "Healthy fats and protein",
            price = "$7.99/lb",
            imageResource = R.drawable.almonds,
            portion = "1 oz (23 almonds)"
        )
    )

    data class DailyNutritionGoals(
        val calories: Int,
        val protein: Float,
        val carbs: Float,
        val fats: Float
    )
}