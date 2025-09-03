package com.example.moment.utils

import android.util.Log

/**
 * Utility functions for handling API responses and common operations
 */
object ApiUtils {
    
    /**
     * Log API result for debugging
     */
    fun <T> logResult(tag: String, operation: String, result: Result<T>) {
        result.onSuccess { data ->
            Log.d(tag, "$operation succeeded: $data")
        }.onFailure { error ->
            Log.e(tag, "$operation failed", error)
        }
    }
    
    /**
     * Extract error message from Result
     */
    fun getErrorMessage(result: Result<*>): String {
        return result.exceptionOrNull()?.message ?: "Unknown error"
    }
    
    /**
     * Check if error is authentication related
     */
    fun isAuthError(error: Throwable): Boolean {
        val message = error.message?.lowercase() ?: ""
        return message.contains("401") || 
               message.contains("unauthorized") || 
               message.contains("authentication")
    }
    
    /**
     * Check if error is network related
     */
    fun isNetworkError(error: Throwable): Boolean {
        val message = error.message?.lowercase() ?: ""
        return message.contains("network") || 
               message.contains("connection") || 
               message.contains("timeout") ||
               error is java.net.UnknownHostException ||
               error is java.net.SocketTimeoutException
    }
}

/**
 * Constants for the Moment app
 */
object MomentConstants {
    
    // File upload limits (matching backend constraints)
    const val MAX_IMAGE_SIZE_MB = 10
    const val MAX_VIDEO_SIZE_MB = 100  
    const val MAX_PDF_SIZE_MB = 5
    
    // Rate limiting info for UI feedback
    const val OTP_RATE_LIMIT_MINUTES = 2
    const val REPORT_RATE_LIMIT_HOURS = 5
    const val PROFILE_UPDATE_RATE_LIMIT_MINUTES = 5
    
    // WebSocket constants
    const val HEARTBEAT_REMINDER_COOLDOWN_MINUTES = 30
    const val HEARTBEAT_TIMEOUT_MINUTES = 5
    
    // Report reasons (should match backend enum)
    object ReportReasons {
        const val INAPPROPRIATE_BEHAVIOR = "inappropriate_behavior"
        const val FAKE_PROFILE = "fake_profile"
        const val HARASSMENT = "harassment"
        const val SPAM = "spam"
        const val UNDERAGE = "underage"
        const val OTHER = "other"
    }
    
    // KYC verification statuses
    object KYCStatus {
        const val PENDING = "pending"
        const val APPROVED = "approved"
        const val REJECTED = "rejected"
        const val EXPIRED = "expired"
    }
}

/**
 * Extension functions for common operations
 */

/**
 * Safe execution of suspend functions with error handling
 */
suspend inline fun <T> safeApiCall(
    tag: String,
    operation: String,
    crossinline call: suspend () -> T
): Result<T> = try {
    val result = Result.success(call())
    Log.d(tag, "$operation succeeded")
    result
} catch (e: Exception) {
    Log.e(tag, "$operation failed", e)
    Result.failure(e)
}

/**
 * Check if a file size is within limits for upload
 */
fun java.io.File.isWithinSizeLimit(maxSizeMB: Int): Boolean {
    val maxSizeBytes = maxSizeMB * 1024 * 1024
    return this.length() <= maxSizeBytes
}

/**
 * Get file extension
 */
fun java.io.File.getExtension(): String {
    return this.name.substringAfterLast('.', "")
}

/**
 * Check if file is a valid image type
 */
fun java.io.File.isValidImage(): Boolean {
    val validExtensions = setOf("jpg", "jpeg", "png")
    return this.getExtension().lowercase() in validExtensions
}

/**
 * Check if file is a valid video type  
 */
fun java.io.File.isValidVideo(): Boolean {
    val validExtensions = setOf("mp4", "mov")
    return this.getExtension().lowercase() in validExtensions
}

/**
 * Check if file is a valid PDF
 */
fun java.io.File.isValidPdf(): Boolean {
    return this.getExtension().lowercase() == "pdf"
}
