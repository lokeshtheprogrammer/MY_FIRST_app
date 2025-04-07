package com.example.nutrifill

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.adapters.FoodRecommendationAdapter
import com.example.nutrifill.models.FoodRecommendation

class RecommendationsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvRecommendationsSummary: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommendationsactivity)
        sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)

        initializeViews()
        loadUserProfile()
        setupRecyclerView()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recycler_view)
        tvRecommendationsSummary = findViewById(R.id.tv_recommendations_summary)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        val userBMI = calculateBMI(
            sharedPref.getFloat("weight", 0f),
            sharedPref.getFloat("height", 0f)
        )
        val dietaryRestrictions = sharedPref.getStringSet("dietary_restrictions", emptySet()) ?: emptySet()
        val calorieGoal = sharedPref.getString("calorie_goal", "2000")?.toIntOrNull() ?: 2000
        
        updateRecommendations(userBMI, dietaryRestrictions, calorieGoal)
        progressBar.visibility = View.GONE
    }

    private fun updateRecommendations(bmi: Float, dietaryRestrictions: Set<String>, calorieGoal: Int) {
        var recommendations = when {
            bmi < 18.5 -> getUnderweightRecommendations()
            bmi < 25 -> getNormalWeightRecommendations()
            else -> getOverweightRecommendations()
        }

        // Filter recommendations based on dietary restrictions
        if (dietaryRestrictions.isNotEmpty()) {
            recommendations = recommendations.filter { recommendation ->
                when {
                    dietaryRestrictions.contains("vegetarian") -> !recommendation.name.contains("Meat", ignoreCase = true)
                    dietaryRestrictions.contains("vegan") -> !recommendation.name.contains("Meat", ignoreCase = true) && 
                                                            !recommendation.name.contains("Milk", ignoreCase = true) && 
                                                            !recommendation.name.contains("Yogurt", ignoreCase = true)
                    dietaryRestrictions.contains("gluten_free") -> !recommendation.name.contains("Bread", ignoreCase = true) && 
                                                                   !recommendation.name.contains("Wheat", ignoreCase = true)
                    dietaryRestrictions.contains("dairy_free") -> !recommendation.name.contains("Milk", ignoreCase = true) && 
                                                                  !recommendation.name.contains("Yogurt", ignoreCase = true)
                    else -> true
                }
            }
        }

        // Adjust portion sizes based on calorie goal
        recommendations = recommendations.map { recommendation ->
            val adjustedPortion = when {
                calorieGoal < 1500 -> "Small portion: ${recommendation.portion}"
                calorieGoal > 2500 -> "Large portion: ${recommendation.portion}"
                else -> "Regular portion: ${recommendation.portion}"
            }
            recommendation.copy(portion = adjustedPortion)
        }
        
        val adapter = FoodRecommendationAdapter { recommendation ->
            // Show detailed nutrition information
            showNutritionDetails(recommendation)
        }
        adapter.submitList(recommendations)
        tvRecommendationsSummary.text = getBMICategory(bmi)
    }

    private fun getBMICategory(bmi: Float): String = when {
        bmi < 18.5 -> "You are underweight. Focus on healthy weight gain."
        bmi < 25 -> "You have a healthy weight. Maintain your current habits."
        else -> "You are overweight. Focus on healthy weight loss."
    }

    private fun getUnderweightRecommendations(): List<FoodRecommendation> = listOf(
        FoodRecommendation(
            name = "Nuts and Seeds",
            description = "High in healthy fats and protein",
            price = "$5.99/lb",
            portion = "1/4 cup daily",
            imageResource = R.drawable.nuts_seeds
        ),
        FoodRecommendation(
            name = "Whole Milk",
            description = "Good source of calories and protein",
            price = "$3.99/gallon",
            portion = "2-3 glasses daily",
            imageResource = R.drawable.milk
        ),
        FoodRecommendation(
            name = "Avocados",
            description = "Healthy fats and nutrients",
            price = "$1.99 each",
            portion = "1/2 avocado per day",
            imageResource = R.drawable.avocado
        ),
        FoodRecommendation(
            name = "Lean Meats",
            description = "Excellent protein source",
            price = "$6.99/lb",
            portion = "6-8 oz per meal",
            imageResource = R.drawable.lean_meat
        ),
        FoodRecommendation(
            name = "Quinoa",
            description = "Complete protein and complex carbs",
            price = "$4.99/lb",
            portion = "1 cup cooked per meal",
            imageResource = R.drawable.quinoa_bowl
        ),
        FoodRecommendation(
            name = "Sweet Potatoes",
            description = "Rich in nutrients and healthy carbs",
            price = "$2.99/lb",
            portion = "1 medium potato per meal",
            imageResource = R.drawable.sweet_potato
        )
    )

    private fun getNormalWeightRecommendations(): List<FoodRecommendation> = listOf(
        FoodRecommendation(
            name = "Fruits",
            description = "Rich in vitamins and fiber",
            price = "$4.99/lb",
            portion = "2-3 servings daily",
            imageResource = R.drawable.fruits
        ),
        FoodRecommendation(
            name = "Vegetables",
            description = "Low in calories, high in nutrients",
            price = "$3.99/lb",
            portion = "3-5 servings daily",
            imageResource = R.drawable.vegetables
        ),
        FoodRecommendation(
            name = "Whole Grains",
            description = "Good source of fiber and energy",
            price = "$2.99/lb",
            portion = "6-8 servings daily",
            imageResource = R.drawable.whole_grains
        ),
        FoodRecommendation(
            name = "Lean Proteins",
            description = "Maintain muscle mass",
            price = "$7.99/lb",
            portion = "5-6 oz per meal",
            imageResource = R.drawable.lean_protein
        )
    )

    private fun getOverweightRecommendations(): List<FoodRecommendation> = listOf(
        FoodRecommendation(
            name = "Leafy Greens",
            description = "Low in calories, high in nutrients",
            price = "$2.99/bunch",
            portion = "2-3 cups daily",
            imageResource = R.drawable.leafy_greens
        ),
        FoodRecommendation(
            name = "Lean Proteins",
            description = "Helps feel full longer",
            price = "$6.99/lb",
            portion = "4-5 oz per meal",
            imageResource = R.drawable.lean_protein
        ),
        FoodRecommendation(
            name = "Berries",
            description = "Low-calorie sweet alternatives",
            price = "$4.99/pint",
            portion = "1 cup daily",
            imageResource = R.drawable.berries
        ),
        FoodRecommendation(
            name = "Greek Yogurt",
            description = "High protein, low fat",
            price = "$1.99/cup",
            portion = "1 cup per serving",
            imageResource = R.drawable.greek_yogurt
        )
    )

    private fun calculateBMI(weight: Float, height: Float): Float {
        return if (height > 0) weight / (height * height) else 0f
    }

    private fun showNutritionDetails(recommendation: FoodRecommendation) {
        // Show a dialog with detailed nutrition information
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(recommendation.name)
            .setMessage(
                "Nutrition Information:\n\n" +
                "Description: ${recommendation.description}\n" +
                "Recommended Portion: ${recommendation.portion}\n" +
                "Price: ${recommendation.price}"
            )
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}