package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homeactivity)
        Log.d(TAG, "onCreate: HomeActivity started")

        // Get username from previous activity (e.g., NutritionSummaryActivity, FstActivity, or SignupActivity)
        val username = intent.getStringExtra("NAME") ?: "User"
        findViewById<TextView>(R.id.tv_welcome).text = "Welcome, $username!"
        Log.d(TAG, "Welcome message set for $username")

        // Profile button
        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            Log.d(TAG, "Profile button clicked")
            startActivity(Intent(this, UserDetailsActivity::class.java))
        }

        // Card click listeners
        findViewById<LinearLayout>(R.id.card_scan).setOnClickListener {
            Log.d(TAG, "Scan card clicked")
            val intent = Intent(this, ScanActivity::class.java).apply {
                putExtra("NAME", username) // Pass username to ScanActivity
            }
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.card_history).setOnClickListener {
            Log.d(TAG, "History card clicked")
            val intent = Intent(this, NutritionHistoryActivity::class.java).apply {
                putExtra("NAME", username) // Pass username explicitly
                putExtra("SCAN_RESULT", this@HomeActivity.intent.getStringExtra("SCAN_RESULT"))
                putExtra("AGE", this@HomeActivity.intent.getIntExtra("AGE", 0))
                putExtra("GENDER", this@HomeActivity.intent.getStringExtra("GENDER"))
                putExtra("HEIGHT", this@HomeActivity.intent.getFloatExtra("HEIGHT", 0f))
                putExtra("WEIGHT", this@HomeActivity.intent.getFloatExtra("WEIGHT", 0f))
                putExtra("ACTIVITY_LEVEL", this@HomeActivity.intent.getStringExtra("ACTIVITY_LEVEL"))
                putExtra("IS_PREGNANT", this@HomeActivity.intent.getBooleanExtra("IS_PREGNANT", false))
            }
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.card_recommendations).setOnClickListener {
            Log.d(TAG, "Recommendations card clicked")
            val intent = Intent(this, RecommendationsActivity::class.java).apply {
                putExtra("DEFICIENCIES", this@HomeActivity.intent.getStringExtra("DEFICIENCIES"))
            }
            startActivity(intent)
        }
    }
}