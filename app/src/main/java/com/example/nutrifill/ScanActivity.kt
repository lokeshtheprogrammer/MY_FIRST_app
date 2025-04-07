package com.example.nutrifill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrifill.databinding.ActivityScanBinding
import com.example.nutrifill.service.NutritionService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.result.contract.ActivityResultContracts
import android.view.LayoutInflater
import com.example.nutrifill.utils.toBitmap
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.nutrifill.model.NutritionInfo
import com.example.nutrifill.model.FoodRecognitionResult
import com.example.nutrifill.service.FoodRecognitionService
import com.example.nutrifill.model.FoodItem
import com.example.nutrifill.repository.FoodRepository

class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private lateinit var btnCapture: Button
    private lateinit var btnAnalyze: Button
    private lateinit var imgFood: ImageView
    private lateinit var tvInstructions: TextView
    private lateinit var tvResults: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    
    private var foodImage: Bitmap? = null
    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101
    
    private val TAG = "ScanActivity"
    
    private lateinit var foodRecognitionService: FoodRecognitionService
    private lateinit var foodRepository: FoodRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        // Initialize views using binding
        btnCapture = binding.btnCapture
        btnAnalyze = binding.btnAnalyze
        imgFood = binding.imgFood
        tvInstructions = binding.tvInstructions
        tvResults = binding.tvResults
        progressBar = binding.progressBar
        previewView = binding.previewView
        // Initialize bottom navigation if it exists in the layout
        bottomNav = findViewById(R.id.bottomNav)

        // Set initial visibility
        imgFood.visibility = View.GONE
        
        // Initialize repositories
        foodRepository = FoodRepository(this)
        
        lifecycleScope.launch(Dispatchers.IO) {
            foodRecognitionService = FoodRecognitionService(this@ScanActivity)
            // Verify models are working
            val (isWorking, status) = foodRecognitionService.verifyModels()
            withContext(Dispatchers.Main) {
                if (!isWorking) {
                    Log.e(TAG, "Model verification failed: $status")
                    Toast.makeText(this@ScanActivity, "Model initialization failed. Some features may not work.", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "Model verification successful: $status")
                }
            }
        }
        
        if (checkCameraPermission()) {
            setupCamera()
        } else {
            requestCameraPermission()
        }
        
        setupBottomNavigation()
        setupButtons()
        
        // Initially hide analyze button and results
        btnAnalyze.visibility = View.GONE
        tvResults.visibility = View.GONE
    }

    // Add imageCapture property at class level
    private var imageCapture: ImageCapture? = null
    
    // Update openCamera function to use CameraX
    private fun setupCamera() {
        try {
            cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Unbind any existing use cases before rebinding
                    cameraProvider.unbindAll()
                    
                    // Create camera selector
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    // Create preview use case
                    val preview = Preview.Builder()
                        .build()
                    
                    // Create image capture use case
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    // Connect preview to preview view
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Camera setup failed: ${e.message}", e)
                    Toast.makeText(this, "Failed to setup camera", Toast.LENGTH_SHORT).show()
                }
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            Log.e(TAG, "Camera initialization failed: ${e.message}", e)
            Toast.makeText(this, "Failed to initialize camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        btnCapture.setOnClickListener {
            captureImage()
        }

        btnAnalyze.setOnClickListener {
            foodImage?.let { bitmap ->
                analyzeImage(bitmap)
            } ?: run {
                Toast.makeText(this, "Please capture an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    foodImage = bitmap
                    imgFood.setImageBitmap(bitmap)
                    btnAnalyze.visibility = View.VISIBLE
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed: ${exception.message}")
                    Toast.makeText(this@ScanActivity, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }



    // Default values for food analysis
    private var selectedItem: String = ""
    private var portionSize: Float = 100f
    private var portionUnit: String = "g"

    private fun analyzeImage(bitmap: Bitmap) {
        progressBar.visibility = View.VISIBLE
        btnAnalyze.isEnabled = false
        tvResults.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Pre-process the image to ensure correct format
                val processedBitmap = preprocessImage(bitmap)
                
                // Attempt food recognition with retry logic
                var recognitionResults = foodRecognitionService.recognizeFood(processedBitmap)
                var attempts = 0
                val maxAttempts = 2

                while (recognitionResults.isEmpty() && attempts < maxAttempts) {
                    attempts++
                    Log.d(TAG, "Retry attempt $attempts for food recognition")
                    recognitionResults = foodRecognitionService.recognizeFood(processedBitmap)
                }

                val recognitionResult = recognitionResults.firstOrNull()
                if (recognitionResult == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScanActivity, "No food detected. Please try taking a clearer photo.", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                        btnAnalyze.isEnabled = true
                        return@withContext
                    }
                }

                val nutritionService = NutritionService()
                val foodItem = FoodItem(
                    id = System.currentTimeMillis().toString(),
                    name = recognitionResult?.foodName ?: "Unknown Food",
                    calories = recognitionResult?.nutritionInfo?.calories?.toFloat() ?: 0f,
                    protein = recognitionResult?.nutritionInfo?.protein ?: 0f,
                    carbohydrates = recognitionResult?.nutritionInfo?.carbs ?: 0f,
                    fats = recognitionResult?.nutritionInfo?.fat ?: 0f,
                    servingSize = recognitionResult?.portionEstimate ?: recognitionResult?.nutritionInfo?.servingSize ?: 100f,
                    servingUnit = recognitionResult?.portionUnit ?: "g",
                    timestamp = System.currentTimeMillis().toString()
                )

                // Save food item and get updated nutrition info
                val savedItemResult = foodRepository.saveFoodItem(foodItem)
                
                withContext(Dispatchers.Main) {
                    if (savedItemResult.isSuccess) {
                        val savedItem = savedItemResult.getOrNull()!!
                        val resultIntent = Intent().apply {
                            putExtra("food_name", savedItem.name)
                            putExtra("calories", savedItem.calories)
                            putExtra("protein", savedItem.protein)
                            putExtra("carbohydrates", savedItem.carbohydrates)
                            putExtra("fat", savedItem.fats)
                            putExtra("portion_size", savedItem.servingSize)
                            putExtra("portion_unit", savedItem.servingUnit)
                        }
                        setResult(RESULT_OK, resultIntent)
                        Toast.makeText(this@ScanActivity, "Food recognized and saved: ${savedItem.name}", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = savedItemResult.exceptionOrNull()?.message ?: "Failed to save food item"
                        Toast.makeText(this@ScanActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Food recognition failed: ${e.message}", e)
                    val errorMessage = when {
                        e.message?.contains("format", ignoreCase = true) == true -> 
                            "Image format error. Please try again."
                        e.message?.contains("network", ignoreCase = true) == true -> 
                            "Network error. Please check your connection."
                        else -> "Failed to analyze image. Please try again."
                    }
                    Toast.makeText(this@ScanActivity, errorMessage, Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    btnAnalyze.isEnabled = true
                }
            }
        }
    }
    
    private fun preprocessImage(bitmap: Bitmap): Bitmap {
        return try {
            // Create a mutable copy of the bitmap to ensure we can process it
            val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            
            // Add any necessary preprocessing steps here
            // For example: resizing, color space conversion, etc.
            
            mutableBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Image preprocessing failed: ${e.message}")
            bitmap // Return original bitmap if preprocessing fails
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@ScanActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this@ScanActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this@ScanActivity, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources if needed
    }

    private fun setupBottomNavigation() {
        // Set the active item
        bottomNav.selectedItemId = R.id.navigation_scan
        
        // Set up navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this@ScanActivity, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_scan -> {
                    // Already on scan screen
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this@ScanActivity, NutritionHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    val sharedPref = getSharedPreferences("NutriFillPrefs", MODE_PRIVATE)
                    val userName = sharedPref.getString("name", "User") ?: "User"
                    val intent = Intent(this@ScanActivity, UserDetailsActivity::class.java)
                    intent.putExtra("NAME", userName)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                foodImage = imageBitmap
                imgFood.setImageBitmap(imageBitmap)
                imgFood.visibility = View.VISIBLE
                btnAnalyze.visibility = View.VISIBLE
                tvInstructions.text = "Image captured! Click Analyze to identify the food."
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(this, "No camera application found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show()
        }
    }
}