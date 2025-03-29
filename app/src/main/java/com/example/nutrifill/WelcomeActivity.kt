package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomeactivity)

        // Find the "Get Started" button
        val btnGetStarted: Button = findViewById(R.id.btn_get_started)

        // Navigate to SignupActivity when the button is clicked
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish() // Close WelcomeActivity
        }
    }
}
