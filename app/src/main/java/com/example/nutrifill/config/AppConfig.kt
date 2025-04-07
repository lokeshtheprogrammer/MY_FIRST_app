package com.example.nutrifill.config

object AppConfig {
    // TODO: In production, this should be configured through BuildConfig or remote config
    const val SERVER_IP = "192.168.51.132" // Current development server IP
    const val SERVER_PORT = "3000"
    
    val SERVER_BASE_URL: String
        get() = "http://$SERVER_IP:$SERVER_PORT/"
    
    val SERVER_DOMAIN: String
        get() = "$SERVER_IP:$SERVER_PORT"
}