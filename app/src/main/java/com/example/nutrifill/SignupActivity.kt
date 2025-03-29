package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrifill.network.UserResponse
import com.example.nutrifill.repository.UserRepository
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var loginLink: TextView

    private val userRepository by lazy { 
        UserRepository.getInstance(applicationContext) 
    }

    companion object {
        private const val TAG = "SignupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signupactivity)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.fullname_input)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.confirm_password_input)
        btnSignup = findViewById(R.id.btn_signup)
        progressBar = findViewById(R.id.progress_bar)
        loginLink = findViewById(R.id.signin_link)
    }

    private fun setupClickListeners() {
        btnSignup.setOnClickListener {
            if (validateInputs()) {
                performSignup()
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, FstActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                etFullName.error = "Enter Full Name"
                return false
            }
            email.isEmpty() -> {
                etEmail.error = "Enter Email"
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Enter Password"
                return false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                return false
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Confirm your password"
                return false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                return false
            }
        }
        return true
    }

    private fun performSignup() {
        updateUiState(isLoading = true)

        lifecycleScope.launch {
            try {
                val result = userRepository.register(
                    email = etEmail.text.toString().trim(),
                    password = etPassword.text.toString().trim(),
                    name = etFullName.text.toString().trim()
                )
                
                result.onSuccess { response: UserResponse ->
                    handleSuccessfulSignup(response)
                }.onFailure { exception: Throwable ->
                    showError(exception.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error during signup", e)
                showError("Network error: ${e.message}")
            } finally {
                updateUiState(isLoading = false)
            }
        }
    }

    private fun handleSuccessfulSignup(response: UserResponse) {
        getSharedPreferences("NutrifillPrefs", MODE_PRIVATE).edit().apply {
            putString("token", response.token)
            putString("userId", response.profile?.id)
            putString("userName", response.profile?.name)
            putString("userEmail", response.profile?.email)
            apply()
        }

        Log.d(TAG, "Signup successful for user: ${response.profile?.email}")
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, UserDetailsActivity::class.java))
        finish()
    }

    private fun updateUiState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSignup.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
