package com.example.nutrifill.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.nutrifill.BuildConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class VisionApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val apiKey = BuildConfig.GOOGLE_VISION_API_KEY
    private val baseUrl = "https://vision.googleapis.com/v1/images:annotate"
    private var lastResponse: VisionResponse? = null

    fun getLastResponse(): VisionResponse? = lastResponse

    companion object {
        private const val TAG = "VisionApiService"
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_DELAY = 1000L
        private const val MAX_BACKOFF_DELAY = 10000L
        private const val COMPRESSION_QUALITY = 70
    }

    private fun compressImage(bitmap: Bitmap): ByteArray {
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream)
            stream.toByteArray()
        }
    }

    suspend fun detectLabels(bitmap: Bitmap): Result<List<VisionLabel>> = withContext(Dispatchers.IO) {
        var currentDelay = INITIAL_BACKOFF_DELAY
        repeat(MAX_RETRIES) { attempt ->
            try {
                if (apiKey.isBlank()) {
                    throw Exception("API key is not configured")
                }

                Log.d(TAG, "Attempting Vision API request (attempt ${attempt + 1}/$MAX_RETRIES)")
                val imageBytes = compressImage(bitmap)

                if (imageBytes.isEmpty()) {
                    throw Exception("Failed to process image")
                }

                Log.d(TAG, "Image compressed successfully. Size: ${imageBytes.size} bytes")

            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val requestBody = VisionRequest(
                requests = listOf(
                    AnnotateImageRequest(
                        image = Image(content = base64Image),
                        features = listOf(
                            Feature(type = "LABEL_DETECTION", maxResults = 10)
                        )
                    )
                )
            )

            val jsonBody = gson.toJson(requestBody)
            Log.d(TAG, "Making Vision API request with payload size: ${jsonBody.length}")

            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMessage = "API call failed with code ${response.code}: ${response.message}"
                    Log.e(TAG, errorMessage)
                    throw Exception(errorMessage)
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response body from server")
                Log.d(TAG, "Received Vision API response successfully")

                val visionResponse = gson.fromJson(responseBody, VisionResponse::class.java)
                lastResponse = visionResponse

                val annotations = visionResponse.responses.firstOrNull()?.labelAnnotations
                if (annotations.isNullOrEmpty()) {
                    throw Exception("No label annotations found in the API response")
                }

                Log.d(TAG, "Successfully parsed ${annotations.size} label annotations")
                return@withContext Result.success(annotations)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting labels (attempt ${attempt + 1}): ${e.message}", e)
            
            if (attempt < MAX_RETRIES - 1) {
                Log.d(TAG, "Retrying after $currentDelay ms")
                delay(currentDelay)
                currentDelay = (currentDelay * 1.5).toLong().coerceAtMost(MAX_BACKOFF_DELAY)
                return@repeat
            }
            
            return@withContext Result.failure(e)
        }
    }
    return@withContext Result.failure(Exception("Failed after $MAX_RETRIES attempts"))
    }
}

// Data classes for request/response
data class VisionRequest(
    val requests: List<AnnotateImageRequest>
)

data class AnnotateImageRequest(
    val image: Image,
    val features: List<Feature>
)

data class Image(
    val content: String
)

data class Feature(
    val type: String,
    val maxResults: Int = 10,
    val model: String? = null
)

data class VisionResponse(
    val responses: List<AnnotateImageResponse>
)

data class AnnotateImageResponse(
    @SerializedName("labelAnnotations")
    val labelAnnotations: List<VisionLabel>?,
    @SerializedName("textAnnotations")
    val textAnnotations: List<VisionText>?,
    @SerializedName("localizedObjectAnnotations")
    val localizedObjectAnnotations: List<VisionObject>?,
    @SerializedName("webDetection")
    val webDetection: WebDetection?
)

data class VisionLabel(
    val description: String,
    val score: Float,
    val topicality: Float,
    val confidence: Float = score
)

data class VisionText(
    val locale: String?,
    val description: String,
    val boundingPoly: BoundingPoly?
)

data class VisionObject(
    val name: String,
    val score: Float,
    val boundingPoly: BoundingPoly?
)

data class BoundingPoly(
    val vertices: List<Vertex>
)

data class Vertex(
    val x: Int,
    val y: Int
)

data class TextAnnotation(
    val description: String,
    val boundingPoly: BoundingPoly?
)

data class LocalizedObject(
    val name: String,
    val score: Float,
    val boundingPoly: BoundingPoly?
)

data class WebDetection(
    val webEntities: List<WebEntity>?,
    val bestGuessLabels: List<BestGuessLabel>?
)

data class WebEntity(
    val entityId: String,
    val score: Float,
    val description: String
)

data class BestGuessLabel(
    val label: String,
    val languageCode: String
)