package com.example.nutrifill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrifill.network.ProfileRequest
import com.example.nutrifill.network.RetrofitClient
import com.example.nutrifill.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.MotionEvent
import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.example.nutrifill.R

class UserDetailsActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var spinnerActivityLevel: Spinner
    private lateinit var activityLevelLabel: TextView
    private lateinit var radioGroupPregnancy: RadioGroup
    private lateinit var etDietaryPreferences: EditText
    private lateinit var etAllergies: EditText
    private lateinit var btnSaveContinue: Button
    private lateinit var btnLogout: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvBMI: TextView
    private lateinit var tvBMICategory: TextView
    private lateinit var tvDailyCalories: TextView
    private lateinit var tvNutritionNeeds: TextView
    private lateinit var btnCalculate: Button
    
    private val TAG = "UserDetailsActivity"

    // Declare tvName as a class member
    private lateinit var tvName: TextView

    // In onCreate method
    private fun initializeViews() {
        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        radioGroupGender = findViewById(R.id.radio_group_gender)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        spinnerActivityLevel = findViewById(R.id.activity_level)
        activityLevelLabel = findViewById(R.id.activity_level_label)
        radioGroupPregnancy = findViewById(R.id.pregnancy_status_group)
        etDietaryPreferences = findViewById(R.id.et_dietary_preferences)
        etAllergies = findViewById(R.id.et_allergies)
        btnSaveContinue = findViewById(R.id.btn_save_continue)
        btnLogout = findViewById(R.id.btn_logout)
        progressBar = findViewById(R.id.progress_bar)
        tvBMI = findViewById(R.id.tv_bmi)
        tvBMICategory = findViewById(R.id.tv_bmi_category)
        tvDailyCalories = findViewById(R.id.tv_daily_calories)
        tvNutritionNeeds = findViewById(R.id.tv_nutrition_needs)
        btnCalculate = findViewById(R.id.btn_calculate_health_metrics)
        
        // Initialize views with proper visibility
        tvBMI.visibility = View.GONE
        tvBMICategory.visibility = View.GONE
        tvDailyCalories.visibility = View.GONE
        tvNutritionNeeds.visibility = View.GONE
        btnCalculate.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userdetailsactivity)
        
        // Initialize tvName
        tvName = findViewById(R.id.et_name)
        
        // Get name from intent - keep only this declaration
        val nameFromIntent = intent.getStringExtra("NAME") ?: "User"
        
        // Check if we should open BMI calculator directly
        val openBMI = intent.getBooleanExtra("OPEN_BMI", false)
        
        // Update UI with name
        updateUI(nameFromIntent)
        
        // Initialize all views
        initializeViews()
        
        // If opened for BMI calculation, show relevant views
        if (openBMI) {
            tvBMI.visibility = View.VISIBLE
            tvBMICategory.visibility = View.VISIBLE
            tvDailyCalories.visibility = View.VISIBLE
            tvNutritionNeeds.visibility = View.VISIBLE
            btnCalculate.visibility = View.VISIBLE
        }

        btnCalculate.setOnClickListener {
            if (validateInputs()) {
                calculateHealthMetrics()
                // Launch BMIResultActivity with calculated metrics
                val intent = Intent(this, BMIResultActivity::class.java)
                val bmi = etWeight.text.toString().toFloat() / ((etHeight.text.toString().toFloat() / 100) * (etHeight.text.toString().toFloat() / 100))
                val bmiCategory = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 25 -> "Normal weight"
                    bmi < 30 -> "Overweight"
                    else -> "Obese"
                }
                intent.putExtra("bmi", bmi)
                intent.putExtra("bmiCategory", bmiCategory)
                intent.putExtra("dailyCalories", tvDailyCalories.text.toString().split(":")[1].trim().split(" ")[0].toInt())
                val nutritionText = tvNutritionNeeds.text.toString().split("\n")
                intent.putExtra("protein", nutritionText[1].split(":")[1].trim().split("g")[0].toInt())
                intent.putExtra("carbs", nutritionText[2].split(":")[1].trim().split("g")[0].toInt())
                intent.putExtra("fats", nutritionText[3].split(":")[1].trim().split("g")[0].toInt())
                startActivity(intent)
            }
        }

        // Set the name in the EditText - use the existing variable
        etName.setText(nameFromIntent)

        // Set up activity level spinner
        val activityLevels = arrayOf(
            ProfileRequest.ACTIVITY_SEDENTARY,
            ProfileRequest.ACTIVITY_LIGHTLY,
            ProfileRequest.ACTIVITY_MODERATELY,
            ProfileRequest.ACTIVITY_VERY,
            ProfileRequest.ACTIVITY_EXTREMELY
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, activityLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivityLevel.adapter = adapter
        
        // Make activity level visible
        activityLevelLabel.visibility = View.VISIBLE
        spinnerActivityLevel.visibility = View.VISIBLE

        // Set up gender change listener to show/hide pregnancy option
        radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            radioGroupPregnancy.visibility = if (checkedId == R.id.gender_female) View.VISIBLE else View.GONE
        }

        btnSaveContinue.setOnClickListener {
            if (validateInputs()) {
                saveUserDetails()
            }
        }

        btnLogout.setOnClickListener {
            // Clear user session data
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }

            // Navigate back to login screen
            val intent = Intent(this@UserDetailsActivity, FstActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        if (etName.text.toString().trim().isEmpty()) {
            etName.error = "Name is required"
            return false
        }
        if (etAge.text.toString().trim().isEmpty()) {
            etAge.error = "Age is required"
            return false
        }
        if (etHeight.text.toString().trim().isEmpty()) {
            etHeight.error = "Height is required"
            return false
        }
        if (etWeight.text.toString().trim().isEmpty()) {
            etWeight.error = "Weight is required"
            return false
        }
        return true
    }

    private fun calculateHealthMetrics() {
        val height = etHeight.text.toString().toFloat() / 100 // Convert cm to m
        val weight = etWeight.text.toString().toFloat()
        val age = etAge.text.toString().toInt()
        val isMale = radioGroupGender.checkedRadioButtonId == R.id.gender_male
        val activityLevelPosition = spinnerActivityLevel.selectedItemPosition

        // Calculate BMI
        val bmi = weight / (height * height)
        tvBMI.text = String.format("BMI: %.1f", bmi)

        // Determine BMI Category
        val bmiCategory = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
        tvBMICategory.text = "Category: $bmiCategory"

        // Calculate Daily Calorie Needs (Harris-Benedict Equation)
        val bmr = if (isMale) {
            88.362 + (13.397 * weight) + (4.799 * height * 100) - (5.677 * age)
        } else {
            447.593 + (9.247 * weight) + (3.098 * height * 100) - (4.330 * age)
        }

        val activityMultiplier = when (activityLevelPosition) {
            0 -> 1.2 // Sedentary
            1 -> 1.375 // Lightly active
            2 -> 1.55 // Moderately active
            3 -> 1.725 // Very active
            4 -> 1.9 // Extra active
            else -> 1.2
        }

        val dailyCalories = bmr * activityMultiplier
        tvDailyCalories.text = String.format("Daily Calorie Needs: %.0f kcal", dailyCalories)

        // Calculate Macronutrient Distribution
        val protein = (dailyCalories * 0.25 / 4).toInt() // 25% of calories from protein (4 cal/g)
        val carbs = (dailyCalories * 0.45 / 4).toInt() // 45% of calories from carbs (4 cal/g)
        val fats = (dailyCalories * 0.30 / 9).toInt() // 30% of calories from fat (9 cal/g)

        tvNutritionNeeds.text = "Daily Nutrition Needs:\n" +
                "Protein: ${protein}g\n" +
                "Carbohydrates: ${carbs}g\n" +
                "Fats: ${fats}g"
    }

    private fun saveUserDetails() {
        progressBar.visibility = View.VISIBLE
        btnSaveContinue.isEnabled = false

        // Get values from form
        val name = etName.text.toString().trim()
        val age = etAge.text.toString().toIntOrNull() ?: 0
        
        val genderId = radioGroupGender.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.gender_male -> "Male"
            R.id.gender_female -> "Female"
            R.id.gender_other -> "Other"
            else -> "Other"
        }
        
        val height = etHeight.text.toString().toFloatOrNull() ?: 0f
        val weight = etWeight.text.toString().toFloatOrNull() ?: 0f
        val activityLevel = spinnerActivityLevel.selectedItem.toString()
        
        val isPregnant = gender == "female" && 
                         radioGroupPregnancy.visibility == View.VISIBLE && 
                         radioGroupPregnancy.checkedRadioButtonId == R.id.pregnant_yes

        // Get dietary preferences and allergies
        val dietaryPreferences = etDietaryPreferences.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val allergies = etAllergies.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Get token from SharedPreferences
        val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        // Create request object
        val profileRequest = ProfileRequest(
            name = name,
            age = age,
            gender = gender,
            height = height,
            weight = weight,
            activityLevel = activityLevel,
            isPregnant = if (gender.equals("female", ignoreCase = true)) isPregnant else false,
            dietaryPreferences = dietaryPreferences,
            allergies = allergies
        )

        // Make API call
        // In the API call section, add error logging for better debugging
        RetrofitClient.instance.updateProfile("Bearer $token", profileRequest)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    progressBar.visibility = View.GONE
                    btnSaveContinue.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {
                        val userResponse = response.body()!!
                        Log.d(TAG, "Profile saved successfully: ${userResponse.message}")
                        Toast.makeText(this@UserDetailsActivity, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        
                        // Save profile info to SharedPreferences
                        with(sharedPref.edit()) {
                            putString("name", name)
                            putInt("age", age)
                            putString("gender", gender)
                            putFloat("height", height)
                            putFloat("weight", weight)
                            apply()
                        }
                        
                        // Navigate to HomeActivity
                        val intent = Intent(this@UserDetailsActivity, HomeActivity::class.java)
                        intent.putExtra("NAME", name)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Failed to save profile"
                        Log.e(TAG, "Error saving profile: $errorMessage, Status Code: ${response.code()}")
                        Toast.makeText(this@UserDetailsActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
                
                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnSaveContinue.isEnabled = true
                    Log.e(TAG, "Network error: ${t.message}", t)  // Add the throwable for stack trace
                    Toast.makeText(this@UserDetailsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Make sure updateUI is a class method with the correct parameter
    private fun updateUI(userName: String) {
        tvName.text = userName
    }

  
}