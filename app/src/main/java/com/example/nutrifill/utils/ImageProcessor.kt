package com.example.nutrifill.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.ByteArrayOutputStream

class ImageProcessor {
    fun processImage(imageBytes: ByteArray): ByteArray {
        try {
            // Convert byte array to bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: throw ImageQualityException("Failed to decode image")

            // Apply image enhancements
            val enhancedBitmap = enhanceImage(bitmap)

            // Convert back to byte array
            return convertBitmapToByteArray(enhancedBitmap)
        } catch (e: Exception) {
            throw ImageProcessingException("Failed to process image: ${e.message}")
        }
    }

    private fun enhanceImage(originalBitmap: Bitmap): Bitmap {
        val matrix = ColorMatrix()
        matrix.setSaturation(1.5f) // Increase saturation

        val enhancedBitmap = Bitmap.createBitmap(
            originalBitmap.width,
            originalBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(enhancedBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        return enhancedBitmap
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}

// Custom exceptions
class ImageQualityException(message: String) : Exception(message)
class ImageProcessingException(message: String) : Exception(message)