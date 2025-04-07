package com.nutrifill.voice

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper

class VoiceAssistantManager private constructor(context: Context) {
    private val voiceAssistant = VoiceAssistant(context)
    private val voiceOutput = VoiceOutputManager.getInstance(context)
    private val commandProcessor = VoiceCommandProcessor()
    private var currentActivity: Activity? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        @Volatile
        private var instance: VoiceAssistantManager? = null

        fun getInstance(context: Context): VoiceAssistantManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceAssistantManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        setupVoiceCommandHandling()
    }

    private fun setupVoiceCommandHandling() {
        voiceAssistant.setOnCommandReceivedListener { command ->
            mainHandler.post {
                when (val commandType = commandProcessor.processCommand(command)) {
                    is VoiceCommandProcessor.CommandType.FoodRecognition -> {
                        voiceAssistant.speak("Starting food recognition camera", true)
                        // Trigger food recognition activity
                    }
                    is VoiceCommandProcessor.CommandType.NutritionInfo -> {
                        voiceAssistant.speak("Fetching nutrition information", true)
                        // Show nutrition information
                    }
                    is VoiceCommandProcessor.CommandType.Navigation -> {
                        handleNavigationCommand(command)
                    }
                    is VoiceCommandProcessor.CommandType.Help -> {
                        voiceAssistant.speak(commandProcessor.getHelpMessage(), true)
                    }
                    is VoiceCommandProcessor.CommandType.Unknown -> {
                        voiceAssistant.speak(commandProcessor.getUnknownCommandMessage(), true)
                    }
                }
            }
        }
    }

    private fun handleNavigationCommand(command: String) {
        when {
            command.contains("back", ignoreCase = true) -> {
                voiceAssistant.speak("Going back", false)
                currentActivity?.onBackPressed()
            }
            command.contains("home", ignoreCase = true) -> {
                voiceAssistant.speak("Going to home screen", false)
                // Navigate to home screen
            }
            command.contains("settings", ignoreCase = true) -> {
                voiceAssistant.speak("Opening settings", false)
                // Navigate to settings
            }
        }
    }

    fun startListening() {
        voiceAssistant.startListening()
    }

    fun stopListening() {
        voiceAssistant.stopListening()
    }

    fun speak(text: String, listenAfter: Boolean = false) {
        voiceAssistant.speak(text, listenAfter)
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }

    fun onDestroy() {
        currentActivity = null
        voiceAssistant.shutdown()
  e      voiceOutput.shutdown()
    }

    fun announceFoodDetection(foodName: String, confidence: Float) {
        voiceOutput.speakFoodDetectionResult(foodName, confidence)
    }

    fun announceNutritionInfo(calories: Int, protein: Float, carbs: Float, fat: Float) {
        voiceOutput.speakNutritionInfo(calories, protein, carbs, fat)
    }

    fun announceHealthWarnings(allergens: List<String>, dietaryRestrictions: List<String>) {
        voiceOutput.speakHealthWarning(allergens, dietaryRestrictions)
    }
}