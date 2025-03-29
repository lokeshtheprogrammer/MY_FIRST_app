package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.nutrifill.network.RetrofitClient
import com.example.nutrifill.network.UserRequest
import com.example.nutrifill.network.UserResponse
import com.example.nutrifill.repository.UserRepository

class FstActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var signupLink: TextView
    private lateinit var progressBar: ProgressBar
    
    // Initialize UserRepository
    private val userRepository by lazy { 
        UserRepository.getInstance(applicationContext) 
    }

    companion object {
        private const val TAG = "FstActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fstactivity)
        Log.d(TAG, "onCreate: FstActivity started")

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignIn = findViewById(R.id.signin_button)
        signupLink = findViewById(R.id.signup_link)
        progressBar = findViewById(R.id.progress_bar)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnSignIn.setOnClickListener {
            if (validateInputs()) {
                performLogin(
                    etEmail.text.toString().trim(),
                    etPassword.text.toString().trim()
                )
            }
        }

        signupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                etEmail.error = "Enter Email"
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Enter Password"
                return false
            }
        }
        return true
    }

    private fun performLogin(email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnSignIn.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = userRepository.login(email, password)
                result.onSuccess { userResponse ->
                    // Save user data
                    getSharedPreferences("NutrifillPrefs", MODE_PRIVATE).edit().apply {
                        putString("token", userResponse.token)
                        putString("userId", userResponse.profile?.id)
                        putString("userName", userResponse.profile?.name)
                        apply()
                    }
                    startActivity(Intent(this@FstActivity, UserDetailsActivity::class.java))
                    finish()
                }.onFailure { exception ->
                    Toast.makeText(
                        this@FstActivity,
                        "Login failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FstActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
                btnSignIn.isEnabled = true
            }
        }
    }
}