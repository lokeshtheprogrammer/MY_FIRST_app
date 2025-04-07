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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignup: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var signinLink: TextView

    private val TAG = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signupactivity)
        Log.d(TAG, "onCreate: SignupActivity started")

        // Update to match the IDs in your layout file
        etFullName = findViewById(R.id.fullname_input)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.confirm_password_input)
        btnSignup = findViewById(R.id.btn_signup)
        progressBar = findViewById(R.id.progress_bar)
        signinLink = findViewById(R.id.signin_link)

        signinLink.setOnClickListener {
            startActivity(Intent(this, FstActivity::class.java))
            finish()
        }

        btnSignup.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (TextUtils.isEmpty(fullName)) {
                etFullName.error = "Enter Full Name"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email)) {
                etEmail.error = "Enter Email"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.error = "Enter Password"
                return@setOnClickListener
            }
            if (password.length < 8) {
                etPassword.error = "Password must be at least 8 characters"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Show progress bar
            progressBar.visibility = View.VISIBLE
            btnSignup.isEnabled = false

            // Create request object
            val request = UserRequest(
                name = fullName,
                email = email,
                password = password
            )

            // Make API call
            RetrofitClient.instance.register(request).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    progressBar.visibility = View.GONE
                    btnSignup.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val userResponse = response.body()!!
                        Log.d(TAG, "Registration successful: ${userResponse.message}")
                        Toast.makeText(this@SignupActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        
                        // Save token to SharedPreferences
                        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("token", userResponse.token)
                            putString("name", fullName)
                            putString("email", email)
                            apply()
                        }
                        
                        // Navigate to UserDetailsActivity
                        val intent = Intent(this@SignupActivity, UserDetailsActivity::class.java)
                        intent.putExtra("NAME", fullName)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                        Log.e(TAG, "Registration error: $errorMessage")
                        Toast.makeText(this@SignupActivity, "Registration failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnSignup.isEnabled = true
                    Log.e(TAG, "Network error: ${t.message}")
                    Toast.makeText(this@SignupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}