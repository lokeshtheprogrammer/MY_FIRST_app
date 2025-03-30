package com.example.nutrifill

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.*
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Add this import at the top
import androidx.camera.core.ExperimentalGetImage
import com.example.nutrifill.models.FoodDetails
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import com.example.nutrifill.databinding.ActivityScannerBinding
import com.example.nutrifill.viewmodels.ScannerViewModel

// Add the annotation to the class
@OptIn(ExperimentalGetImage::class)
class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val viewModel: ScannerViewModel by viewModels()
    private var imageCapture: ImageCapture? = null  // Added this line

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        setupUI()
        setupErrorHandling()
        setupLoadingIndicators()
    }

    private fun setupUI() {
        binding.captureButton.setOnClickListener { _: View ->
            takePhoto()
        }

        viewModel.scanResult.observe(this) { result: FoodDetails ->
            showFoodDetailsDialog(result)
        }

        viewModel.error.observe(this) { error: String ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton("Retry") { dialog: DialogInterface, which: Int ->
                    retryLastAction()
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupErrorHandling() {
        viewModel.error.observe(this) { error: String ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton("Retry") { dialog: DialogInterface, which: Int -> 
                    retryLastAction() 
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> 
                    dialog.dismiss() 
                }
                .show()
        }
    }

    private fun setupLoadingIndicators() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingOverlay.isVisible = isLoading
            binding.captureButton.isEnabled = !isLoading
        }
    }

    private fun takePhoto() {
        binding.loadingOverlay.isVisible = true
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    
                    viewModel.analyzeImage(bytes)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun showFoodDetailsDialog(foodDetails: FoodDetails) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Food Details")
            .setMessage("Name: ${foodDetails.name}\nCalories: ${foodDetails.calories}")
            .setPositiveButton("Add") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRecommendations() {
        startActivity(Intent(this, RecommendationsActivity::class.java))
    }

    private fun showNutritionDetails(foodDetails: FoodDetails) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Nutrition Information: ${foodDetails.name}")
            .setMessage("""
                Basic Nutrition:
                • Calories: ${foodDetails.calories} kcal
                • Protein: ${foodDetails.protein}g
                • Carbohydrates: ${foodDetails.carbs}g
                • Fats: ${foodDetails.fats}g
                
                Additional Information:
                • Serving Size: 100g
                • Dietary Fiber: ${foodDetails.fiber}g
                • Sugars: ${foodDetails.sugars}g
                • Sodium: ${foodDetails.sodium}mg
                
                Vitamins & Minerals:
                ${foodDetails.vitamins.joinToString("\n") { "• ${it.name}: ${it.amount}${it.unit}" }}
                
                Would you like to add this to your daily intake?
            """.trimIndent())
            .setPositiveButton("Add to Daily Intake") { _, _ ->
                addToMealTracking(foodDetails)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
    
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    
        try {
            // Unbind any existing use cases
            cameraProvider.unbindAll()
    
            // Bind use cases to camera
            val camera = cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalyzer
            )
    
            // Store imageCapture for later use
            this.imageCapture = imageCapture
    
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun retryLastAction() {
        // Implement retry logic
    }

    private fun addToMealTracking(foodDetails: FoodDetails) {
        // TODO: Implement adding to meal tracking
    }

    companion object {
        private const val TAG = "ScannerActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    // Add the annotation to the function
    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        // ... existing image processing code ...
    }
}
