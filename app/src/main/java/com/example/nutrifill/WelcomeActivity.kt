package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var btnGetStarted: Button
    private val TAG = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomeactivity)
        Log.d(TAG, "onCreate: WelcomeActivity started")

        btnGetStarted = findViewById(R.id.btn_get_started)

        // Check if user is already logged in
        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", null)
        
        if (token != null) {
            // User is already logged in, navigate to HomeActivity after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                val name = sharedPref.getString("name", "User") ?: "User"
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("NAME", name)
                startActivity(intent)
                finish()
            }, 1500) // 1.5 seconds delay to show the welcome screen
        } else {
            // User is not logged in, show the Get Started button
            btnGetStarted.setOnClickListener {
                startActivity(Intent(this, FstActivity::class.java))
                finish()
            }
        }
    }
}
