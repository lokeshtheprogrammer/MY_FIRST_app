package com.nutrifill.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class VoiceOutputManager private constructor(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var onUtteranceCompleteListener: (() -> Unit)? = null

    companion object {
        @Volatile
        private var instance: VoiceOutputManager? = null

        fun getInstance(context: Context): VoiceOutputManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceOutputManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        initializeTextToSpeech(context)
    }

    private fun initializeTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onUtteranceCompleteListener?.invoke()
                    }
                    override fun onError(utteranceId: String?) {}
                })
            }
        }
    }

    fun speakFoodDetectionResult(foodName: String, confidence: Float) {
        val message = if (confidence > 0.8) {
            "I am confident this is $foodName"
        } else if (confidence > 0.5) {
            "This appears to be $foodName"
        } else {
            "This might be $foodName, but I'm not entirely sure"
        }
        speak(message)
    }

    fun speakNutritionInfo(calories: Int, protein: Float, carbs: Float, fat: Float) {
        val nutritionMessage = "This food contains $calories calories, " +
                "$protein grams of protein, " +
                "$carbs grams of carbohydrates, and " +
                "$fat grams of fat."
        speak(nutritionMessage)
    }

    fun speakHealthWarning(allergens: List<String>, dietaryRestrictions: List<String>) {
        val warnings = mutableListOf<String>()
        
        if (allergens.isNotEmpty()) {
            warnings.add("Warning: This food contains ${allergens.joinToString(", ")}")
        }
        
        if (dietaryRestrictions.isNotEmpty()) {
            warnings.add("This food may not be suitable for ${dietaryRestrictions.joinToString(", ")} diets")
        }
        
        if (warnings.isNotEmpty()) {
            speak(warnings.joinToString(". "))
        }
    }

    private fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, queueMode, null, utteranceId)
    }

    fun setOnUtteranceCompleteListener(listener: () -> Unit) {
        onUtteranceCompleteListener = listener
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        instance = null
    }
}