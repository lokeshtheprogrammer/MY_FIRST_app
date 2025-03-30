package com.example.nutrifill.utils

import java.io.IOException
import retrofit2.HttpException

object ErrorHandler {
    fun handleScanningError(error: Exception): String {
        return when (error) {
            is IOException -> "Network error: Please check your internet connection"
            is SecurityException -> "Camera permission denied"
            is HttpException -> {
                when (error.code()) {
                    401 -> "Authentication failed"
                    429 -> "Too many requests. Please try again later"
                    500 -> "Server error. Please try again later"
                    else -> "Server error: ${error.message()}"
                }
            }
            else -> "An unexpected error occurred: ${error.message}"
        }
    }
}