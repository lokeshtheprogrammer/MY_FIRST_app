package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrifill.network.RetrofitClient
import com.example.nutrifill.network.UserRequest
import com.example.nutrifill.network.UserResponse
import com.example.nutrifill.network.UserProfile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FstActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnSkipLogin: Button
    private lateinit var signupLink: TextView
    private lateinit var progressBar: ProgressBar

    private val TAG = "FstActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fstactivity)
        Log.d(TAG, "onCreate: FstActivity started")

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignIn = findViewById(R.id.signin_button)
        btnSkipLogin = findViewById(R.id.skip_login_button)
        signupLink = findViewById(R.id.signup_link)
        progressBar = findViewById(R.id.progress_bar)

        // Skip Login button click handler
        btnSkipLogin.setOnClickListener {
            // Save demo user info to SharedPreferences
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("token", "demo_token")
                putString("name", "Demo User")
                putString("email", "demo@example.com")
                apply()
            }
            
            // Navigate to HomeActivity
            val intent = Intent(this@FstActivity, HomeActivity::class.java)
            intent.putExtra("NAME", "Demo User")
            intent.putExtra("EMAIL", "demo@example.com")
            startActivity(intent)
            finish()
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                etEmail.error = "Enter Email"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.error = "Enter Password"
                return@setOnClickListener
            }

            // Show progress bar
            progressBar.visibility = View.VISIBLE
            btnSignIn.isEnabled = false
            // Fix: Pass default values for `name` and `role`


            // Create request object
            val request = UserRequest(
                email = email,
                password = password,
                name = "",  // Add empty name for login
                role = "user"  // Add default role
            )

            // Make API call
            RetrofitClient.instance.login(request).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    progressBar.visibility = View.GONE
                    btnSignIn.isEnabled = true  // Changed from btnLogin to btnSignIn

                    if (response.isSuccessful && response.body() != null) {
                        val userResponse = response.body()!!
                        // Use ?. for nullable UserProfile
                        val userName = userResponse.profile?.name ?: "User"
                        // Change this line to match your actual property name
                        val userEmail = userResponse.profile?.email ?: ""  // If it's named differently, change "email" to the correct property name
                        
                        Log.d(TAG, "Login successful: $userName")
                        Toast.makeText(this@FstActivity, "Welcome, $userName!", Toast.LENGTH_SHORT).show()
                        
                        // Save user info to SharedPreferences
                        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("token", userResponse.token)
                            putString("name", userResponse.profile?.name ?: "User")
                            // Change this line to match your actual property name
                            putString("email", userResponse.profile?.email ?: "")  // If it's named differently, change "email" to the correct property name
                            apply()
                        }
                        
                        // Navigate to HomeActivity
                        val intent = Intent(this@FstActivity, HomeActivity::class.java)
                        intent.putExtra("NAME", userName)
                        intent.putExtra("EMAIL", userEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Login failed"
                        Log.e(TAG, "Login error: $errorMessage")
                        Toast.makeText(this@FstActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnSignIn.isEnabled = true
                    Log.e(TAG, "Network error: ${t.message}")
                    Toast.makeText(this@FstActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        signupLink.setOnClickListener {
            Log.d(TAG, "Signup link clicked")
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
}