package com.nutrifill.voice

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

class VoiceAccessibilityService : AccessibilityService() {
    private lateinit var voiceAssistantManager: VoiceAssistantManager

    override fun onCreate() {
        super.onCreate()
        voiceAssistantManager = VoiceAssistantManager.getInstance(this)
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
            flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            notificationTimeout = 100
        }
        serviceInfo = info

        // Initial welcome message
        voiceAssistantManager.speak(
            "Voice assistant activated. Say 'help' for available commands.",
            true
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Announce screen changes
                event.className?.toString()?.let { className ->
                    val screenName = className.substringAfterLast('.')
                        .replace("Activity", "")
                        .replace("Fragment", "")
                        .replace("([A-Z])".toRegex()) { " ${it.value}" }
                        .trim()
                    
                    voiceAssistantManager.speak("Now on $screenName screen", false)
                }
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // Provide audio feedback for clicks
                event.contentDescription?.toString()?.let { description ->
                    voiceAssistantManager.speak("Clicked $description", false)
                }
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                // Announce focused elements
                event.contentDescription?.toString()?.let { description ->
                    voiceAssistantManager.speak(description, false)
                }
            }
        }
    }

    override fun onInterrupt() {
        voiceAssistantManager.speak("Voice assistant interrupted", false)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceAssistantManager.onDestroy()
    }
}