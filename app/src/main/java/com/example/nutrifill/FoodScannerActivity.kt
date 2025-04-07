package com.example.nutrifill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nutrifill.databinding.ActivityFoodScannerBinding
import com.example.nutrifill.network.EdamamApiService
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.cloud.vision.v1.*
import com.google.cloud.vision.v1.Feature.Type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class FoodScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var isScanMode = true

    companion object {
        private const val TAG = "FoodScannerActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupUI() {
        // Mode selection buttons
        binding.btnScanMode.setOnClickListener {
            switchToScanMode()
        }
        binding.btnManualMode.setOnClickListener {
            switchToManualMode()
        }

        // Capture button
        binding.controlsContainer.setOnClickListener {
            if (isScanMode) takePhoto()
            else submitManualEntry()
        }
    }

    private fun switchToScanMode() {
        isScanMode = true
        binding.cameraPreview.visibility = View.VISIBLE
        binding.manualEntryContainer.visibility = View.GONE
        binding.tvScanStatus.visibility = View.VISIBLE
        binding.capturedImage.visibility = View.GONE
        startCamera()
    }

    private fun switchToManualMode() {
        isScanMode = false
        binding.cameraPreview.visibility = View.GONE
        binding.manualEntryContainer.visibility = View.VISIBLE
        binding.tvScanStatus.visibility = View.GONE
        binding.capturedImage.visibility = View.GONE
    }

    private fun submitManualEntry() {
        val foodName = binding.etFoodName.text.toString()
        if (foodName.isNotEmpty()) {
            // Hide keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etFoodName.windowToken, 0)
            
            // Update UI to show loading state
            binding.tvScanStatus.visibility = View.VISIBLE
            binding.tvScanStatus.text = "Processing..."
            
            // Fetch nutrition data for the manually entered food
            fetchNutritionData(foodName)
        } else {
            Toast.makeText(this, "Please enter food name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

            // Image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    processImage(savedUri)
                }
            }
        )
    }

    private fun processImage(imageUri: Uri) {
        binding.tvScanStatus.text = "Processing image..."
        binding.capturedImage.visibility = View.VISIBLE
        binding.capturedImage.setImageURI(imageUri)
        binding.cameraPreview.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Load image into bitmap
                val bitmap = BitmapFactory.decodeFile(imageUri.path)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val imageBytes = stream.toByteArray()

                // Initialize FoodRecognitionService
                val foodRecognitionService = FoodRecognitionService(this@FoodScannerActivity)

                withContext(Dispatchers.Main) {
                    try {
                        val results = foodRecognitionService.recognizeFood(bitmap)
                        if (results.isNotEmpty()) {
                            val foodItem = results[0]
                            binding.tvScanStatus.text = "Detected: ${foodItem.foodName} (${(foodItem.confidence * 100).toInt()}%)"
                            fetchNutritionData(foodItem.foodName)
                        } else {
                            binding.tvScanStatus.text = "No food items detected"
                            Toast.makeText(this@FoodScannerActivity, "No food items detected", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: RecognitionException) {
                        binding.tvScanStatus.text = "Error: ${e.message}"
                        Toast.makeText(this@FoodScannerActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.tvScanStatus.text = "Error processing image"
                    Toast.makeText(this@FoodScannerActivity, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchNutritionData(foodItem: String) {
        binding.tvScanStatus.text = "Fetching nutrition data for $foodItem..."
        
        val client = okhttp3.OkHttpClient()
        val nutritionixAppId = BuildConfig.NUTRITIONIX_APP_ID
        val nutritionixAppKey = BuildConfig.NUTRITIONIX_APP_KEY
        
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = "{\"query\": \"$foodItem\"}".toRequestBody(mediaType)
        
        val request = okhttp3.Request.Builder()
            .url("https://trackapi.nutritionix.com/v2/natural/nutrients")
            .addHeader("x-app-id", nutritionixAppId)
            .addHeader("x-app-key", nutritionixAppKey)
            .post(requestBody)
            .build()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                
                if (response.isSuccessful && responseData != null) {
                    val json = org.json.JSONObject(responseData)
                    val foods = json.getJSONArray("foods")
                    
                    if (foods.length() > 0) {
                        val food = foods.getJSONObject(0)
                        val calories = food.getDouble("nf_calories").toFloat()
                        val protein = food.getDouble("nf_protein").toFloat()
                        val carbs = food.getDouble("nf_total_carbohydrate").toFloat()
                        val fats = food.getDouble("nf_total_fat").toFloat()
                        val servingWeight = food.getDouble("serving_weight_grams").toFloat()
                        val servingUnit = food.getString("serving_unit")
                        
                        withContext(Dispatchers.Main) {
                            // Show portion size dialog
                            showPortionDialog(foodItem, calories, protein, carbs, fats, servingWeight, servingUnit)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.tvScanStatus.text = "No nutrition data found for $foodItem"
                            Toast.makeText(this@FoodScannerActivity, "No nutrition data found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.tvScanStatus.text = "Could not find nutrition data for $foodItem"
                        Toast.makeText(this@FoodScannerActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvScanStatus.text = "Error fetching nutrition data"
                    Toast.makeText(this@FoodScannerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPortionDialog(
        foodItem: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fats: Float,
        servingWeight: Float,
        servingUnit: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_portion_size, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etPortionSize = dialogView.findViewById<EditText>(R.id.et_portion_size)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinner_portion_unit)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        // Set default portion size
        etPortionSize.setText("1")

        // Setup unit spinner with available units
        val units = arrayOf(servingUnit, "g", "oz", "cup")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter

        btnConfirm.setOnClickListener {
            val portionSize = etPortionSize.text.toString().toFloatOrNull() ?: 1f
            val selectedUnit = spinnerUnit.selectedItem.toString()

            // Calculate adjusted nutrition values based on portion size and unit
            val multiplier = when (selectedUnit) {
                "g" -> portionSize / servingWeight
                "oz" -> (portionSize * 28.35f) / servingWeight
                "cup" -> (portionSize * 240f) / servingWeight
                else -> portionSize
            }

            val adjustedCalories = calories * multiplier
            val adjustedProtein = protein * multiplier
            val adjustedCarbs = carbs * multiplier
            val adjustedFats = fats * multiplier

            // Save to shared preferences for history
            val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
            val historyJson = sharedPref.getString("nutrition_history", "[]") ?: "[]"
            val gson = Gson()
            val historyType = object : TypeToken<MutableList<Map<String, Any>>>() {}.type
            val historyList: MutableList<Map<String, Any>> = gson.fromJson(historyJson, historyType)

            // Add new entry
            val newEntry = mapOf(
                "foodName" to foodItem,
                "nutrients" to mapOf(
                    "calories" to adjustedCalories,
                    "protein" to adjustedProtein,
                    "carbs" to adjustedCarbs,
                    "fat" to adjustedFats
                ),
                "portionSize" to portionSize,
                "portionUnit" to selectedUnit,
                "timestamp" to System.currentTimeMillis()
            )
            historyList.add(0, newEntry)

            // Save updated history
            with(sharedPref.edit()) {
                putString("nutrition_history", gson.toJson(historyList))
                apply()
            }

            // Create result intent with adjusted values
            val resultIntent = Intent().apply {
                putExtra("foodName", foodItem)
                putExtra("calories", adjustedCalories)
                putExtra("protein", adjustedProtein)
                putExtra("carbs", adjustedCarbs)
                putExtra("fats", adjustedFats)
                putExtra("portion_size", portionSize)
                putExtra("portion_unit", selectedUnit)
            }

            setResult(RESULT_OK, resultIntent)
            dialog.dismiss()

            binding.tvScanStatus.text = "Found: $foodItem\nCalories: ${adjustedCalories.toInt()} kcal"
            Toast.makeText(this, "Nutrition data saved", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1500)
        }

        dialog.show()
    }

    private val outputDirectory: File by lazy {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        mediaDir ?: filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}