package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.nutrifill.network.UserDetailsRequest
import com.example.nutrifill.network.UserDetailsResponse
import com.example.nutrifill.repository.UserRepository
import android.widget.RadioButton
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class UserDetailsActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var activityLevelLabel: TextView
    private lateinit var radioGroupPregnancy: RadioGroup
    private lateinit var btnSaveContinue: Button
    private lateinit var progressBar: ProgressBar

    private val prefs by lazy {
        getSharedPreferences("NutrifillPrefs", MODE_PRIVATE)
    }

    private val userRepository: UserRepository by lazy { 
        UserRepository(applicationContext)
    }

    // Remove duplicate onCreate content
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userdetailsactivity)

        // Check if user is authenticated
        val userId = prefs.getString("userId", null)
        if (userId == null) {
            startActivity(Intent(this, FstActivity::class.java))
            finish()
            return
        }

        initializeViews()
        setupListeners()
        setupSpinner()
        
        // Pre-fill name if available from signup
        etName.setText(prefs.getString("userName", ""))
    }

    // Remove duplicate listener setup
    private fun setupListeners() {
        btnSaveContinue.setOnClickListener {
            if (validateAndSaveUserDetails()) {
                navigateToNutritionSummary()
            }
        }

        radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            val isGenderSelected = checkedId != -1
            activityLevelLabel.visibility = if (isGenderSelected) View.VISIBLE else View.GONE
            spinnerActivityLevel.visibility = if (isGenderSelected) View.VISIBLE else View.GONE
            radioGroupPregnancy.visibility = if (checkedId == R.id.gender_female) View.VISIBLE else View.GONE
        }
    }

    private fun initializeViews() {
        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        radioGroupGender = findViewById(R.id.radio_group_gender)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        spinnerActivityLevel = findViewById(R.id.activity_level)
        activityLevelLabel = findViewById(R.id.activity_level_label)
        radioGroupPregnancy = findViewById(R.id.pregnancy_status_group)
        btnSaveContinue = findViewById(R.id.save_continue_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupSpinner() {
        val activityLevels = arrayOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active", "Extremely Active")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivityLevel.adapter = adapter
    }

    private fun validateAndSaveUserDetails(): Boolean {
        if (!validateInputs()) return false
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_SHORT).show()
            return false
        }

        progressBar.visibility = View.VISIBLE
        btnSaveContinue.isEnabled = false

        val userId = prefs.getString("userId", "") ?: ""
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val activityLevel = spinnerActivityLevel.selectedItem?.toString()
        val gender = findViewById<RadioButton>(radioGroupGender.checkedRadioButtonId).text.toString()
        val isPregnant = if (gender == "Female") {
            radioGroupPregnancy.checkedRadioButtonId == R.id.pregnant_yes
        } else null

        val userDetails = UserDetailsRequest(
            userId = userId,
            name = name,
            age = age.toInt(),
            gender = gender,
            height = height.toFloat(),
            weight = weight.toFloat(),
            activityLevel = activityLevel ?: "Sedentary",
            isPregnant = isPregnant
        )

        lifecycleScope.launch {
            try {
                val result = userRepository.saveUserDetails(userDetails)
                result.onSuccess { response ->
                    // Save BMI and calories info
                    response.userDetails?.let { details ->
                        prefs.edit().apply {
                            putFloat("bmi", details.bmi)
                            putFloat("dailyCalories", details.dailyCalories.toFloat())
                            apply()
                        }
                    }
                    saveToPrefsAndNavigate()
                }.onFailure { exception ->
                    Toast.makeText(
                        this@UserDetailsActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@UserDetailsActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
                btnSaveContinue.isEnabled = true
            }
        }

        return true
    }

    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val activityLevel = spinnerActivityLevel.selectedItem?.toString()

        when {
            name.isEmpty() -> {
                etName.error = "Enter name"
                return false
            }
            age.isEmpty() -> {
                etAge.error = "Enter age"
                return false
            }
            height.isEmpty() -> {
                etHeight.error = "Enter height"
                return false
            }
            weight.isEmpty() -> {
                etWeight.error = "Enter weight"
                return false
            }
            radioGroupGender.checkedRadioButtonId == -1 -> {
                Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
                return false
            }
            activityLevel == null -> {
                Toast.makeText(this, "Please select activity level", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun navigateToNutritionSummary() {
        saveToPrefsAndNavigate()
    }

    private fun saveToPrefsAndNavigate() {
        val height = etHeight.text.toString().toFloat() / 100 // Convert to meters
        val weight = etWeight.text.toString().toFloat()
        val gender = findViewById<RadioButton>(radioGroupGender.checkedRadioButtonId).text.toString()
        val activityLevel = spinnerActivityLevel.selectedItem.toString()
        
        val bmi = weight / (height * height)
        val dailyCalories = calculateDailyCalories(
            etAge.text.toString().toInt(),
            gender,
            height,
            weight,
            activityLevel,
            isPregnant = gender == "Female" && 
                radioGroupPregnancy.checkedRadioButtonId == R.id.pregnant_yes
        )

        startActivity(Intent(this, NutritionSummaryActivity::class.java).apply {
            putExtra("BMI", bmi)
            putExtra("DAILY_CALORIES", dailyCalories)
            putExtra("NAME", etName.text.toString().trim())
            putExtra("GENDER", gender)
            putExtra("IS_PREGNANT", gender == "Female" && 
                radioGroupPregnancy.checkedRadioButtonId == R.id.pregnant_yes)
        })
        finish()
    }

    private fun calculateDailyCalories(age: Int, gender: String, height: Float, weight: Float, activityLevel: String, isPregnant: Boolean?): Double {
        val bmr = if (gender == "Male") {
            10 * weight + 6.25 * (height * 100) - 5 * age + 5
        } else {
            10 * weight + 6.25 * (height * 100) - 5 * age - 161
        }
        val activityMultiplier = when (activityLevel) {
            "Sedentary" -> 1.2
            "Lightly Active" -> 1.375
            "Moderately Active" -> 1.55
            "Very Active" -> 1.725
            "Extremely Active" -> 1.9
            else -> 1.2
        }
        var calories = bmr * activityMultiplier
        if (isPregnant == true) calories += 300
        return calories
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
        }
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}