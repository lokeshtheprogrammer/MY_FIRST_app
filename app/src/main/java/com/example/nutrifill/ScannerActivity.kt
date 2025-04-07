package com.example.nutrifill

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.nutrifill.api.VisionApiService
import com.example.nutrifill.api.VisionLabel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ScannerActivity : AppCompatActivity(), DisplayManager.DisplayListener {
    private lateinit var cameraPreview: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var tvScanStatus: TextView
    private lateinit var btnDone: Button
    private lateinit var displayManager: DisplayManager
    private var displayId: Int = -1
    private lateinit var visionApiService: VisionApiService

    companion object {
        private const val TAG = "ScannerActivity"
        private const val CAMERA_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        Log.d(TAG, "onCreate: ScannerActivity started")

        // Initialize views
        cameraPreview = findViewById(R.id.camera_preview)
        tvScanStatus = findViewById(R.id.tv_scan_status)
        btnDone = findViewById(R.id.btnDone)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize display manager
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayId = cameraPreview.display.displayId

        // Initialize Vision API service
        visionApiService = VisionApiService()

        // Register display listener
        displayManager.registerDisplayListener(this, null)

        // Set up button listener
        btnDone.setOnClickListener {
            // Launch FoodDetailsActivity with scan results
            val intent = Intent(this, FoodDetailsActivity::class.java)
            intent.putExtra("FOOD_ID", "sample_food_id") // Replace with actual food ID from scan
            startActivity(intent)
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        Log.d(TAG, "Starting camera")
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                Log.e(TAG, "Camera initialization failed: ${e.message}")
                Toast.makeText(this, "Camera failed to start: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: InterruptedException) {
                Log.e(TAG, "Camera interrupted: ${e.message}")
                Toast.makeText(this, "Camera interrupted: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        Log.d(TAG, "Binding camera preview")
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(cameraPreview.surfaceProvider)

        // Set up the image analyzer
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImage(imageProxy)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            Log.d(TAG, "Camera preview bound successfully")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "No camera available: ${e.message}")
            Toast.makeText(this, "No camera available: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted, starting camera")
                startCamera()
            } else {
                Log.w(TAG, "Camera permission denied")
                Toast.makeText(this, "Camera permission required to scan", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // Display listener implementations
    override fun onDisplayAdded(displayId: Int) = Unit

    override fun onDisplayRemoved(displayId: Int) = Unit

    override fun onDisplayChanged(displayId: Int) {
        if (this.displayId == displayId) {
            Log.d(TAG, "Display $displayId rotation changed")
            if (::cameraProviderFuture.isInitialized && cameraProviderFuture.isDone) {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            }
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        try {
            val image = imageProxy.image ?: run {
                Log.e(TAG, "Failed to get image from ImageProxy")
                imageProxy.close()
                return
            }

            // Convert image to bitmap
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            // Process with Vision API
            CoroutineScope(Dispatchers.IO).launch {
                visionApiService.detectLabels(bitmap).fold(
                    onSuccess = { labels ->
                        processVisionResults(labels)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Vision API failed: ${e.message}", e)
                        runOnUiThread {
                            tvScanStatus.text = "Error processing image"
                            btnDone.isEnabled = false
                        }
                    }
                )
            }

            imageProxy.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}", e)
            runOnUiThread {
                tvScanStatus.text = "Error processing image"
                Toast.makeText(this, "Error processing image. Please try again.", Toast.LENGTH_SHORT).show()
                btnDone.isEnabled = false
            }
            imageProxy.close()
        }
    }

    private fun processVisionResults(labels: List<VisionLabel>) {
        if (labels.isNotEmpty()) {
            val foodLabels = labels.filter { it.confidence > 0.7f }
            if (foodLabels.isNotEmpty()) {
                val bestLabel = foodLabels.maxByOrNull { it.confidence }!!
                val detectedText = visionApiService.getLastResponse()?.responses?.firstOrNull()?.textAnnotations?.firstOrNull()?.description ?: ""
                val localizedObjects = visionApiService.getLastResponse()?.responses?.firstOrNull()?.localizedObjectAnnotations ?: emptyList()
                val webEntities = visionApiService.getLastResponse()?.responses?.firstOrNull()?.webDetection?.webEntities ?: emptyList()

                val resultText = buildString {
                    append("Detected Food: ${bestLabel.description}\n")
                    append("Confidence: ${String.format("%.1f", bestLabel.confidence * 100)}%\n")
                    
                    if (detectedText.isNotEmpty()) {
                        append("\nText on Package:\n$detectedText\n")
                    }

                    if (localizedObjects.isNotEmpty()) {
                        append("\nDetected Objects:\n")
                        localizedObjects.take(3).forEach { obj ->
                            append("- ${obj.name} (${String.format("%.1f", obj.score * 100)}%)\n")
                        }
                    }

                    if (webEntities.isNotEmpty()) {
                        append("\nWeb Matches:\n")
                        webEntities.take(3).forEach { entity ->
                            append("- ${entity.description}\n")
                        }
                    }

                    append("\n${getNutritionInfo(bestLabel.description)}")
                }

                runOnUiThread {
                    tvScanStatus.text = resultText
                    btnDone.isEnabled = true
                }
            } else {
                runOnUiThread {
                    tvScanStatus.text = "Low confidence detection. Please try again."
                    btnDone.isEnabled = false
                }
            }
        } else {
            runOnUiThread {
                tvScanStatus.text = "No food item detected. Please try again."
                btnDone.isEnabled = false
            }
        }
    }

    private fun getFoodNameFromResults(results: FloatArray): String {
        // TODO: Implement mapping of model output to food names
        // This is a placeholder implementation
        val maxIndex = results.indices.maxByOrNull { results[it] } ?: 0
        return "Food Item ${maxIndex + 1}"
    }

    private fun getNutritionInfo(foodName: String): String {
        // Use foodName to provide food-specific nutrition information
        return buildString {
            append("Nutrition Information for $foodName:\n")
            // TODO: Replace with actual API call to nutrition database
            append("Calories: ${getEstimatedCalories(foodName)} kcal\n")
            append("Protein: ${getEstimatedProtein(foodName)}g\n")
            append("Carbs: ${getEstimatedCarbs(foodName)}g\n")
            append("Fat: ${getEstimatedFat(foodName)}g")
        }
    }

    private fun getEstimatedCalories(foodName: String): Int {
        // Simple estimation based on food name keywords
        return when {
            foodName.contains("salad", ignoreCase = true) -> 150
            foodName.contains("burger", ignoreCase = true) -> 550
            foodName.contains("fruit", ignoreCase = true) -> 80
            else -> 450 // default value
        }
    }

    private fun getEstimatedProtein(foodName: String): Int {
        return when {
            foodName.contains("meat", ignoreCase = true) -> 25
            foodName.contains("fish", ignoreCase = true) -> 22
            foodName.contains("vegetable", ignoreCase = true) -> 5
            else -> 18 // default value
        }
    }

    private fun getEstimatedCarbs(foodName: String): Int {
        return when {
            foodName.contains("rice", ignoreCase = true) -> 45
            foodName.contains("bread", ignoreCase = true) -> 30
            foodName.contains("fruit", ignoreCase = true) -> 20
            else -> 55 // default value
        }
    }

    private fun getEstimatedFat(foodName: String): Int {
        return when {
            foodName.contains("fried", ignoreCase = true) -> 25
            foodName.contains("nuts", ignoreCase = true) -> 15
            foodName.contains("vegetable", ignoreCase = true) -> 3
            else -> 12 // default value
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        displayManager.unregisterDisplayListener(this)
        cameraExecutor.shutdown()
    }
}
