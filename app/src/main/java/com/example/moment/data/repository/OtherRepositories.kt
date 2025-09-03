package com.example.moment.data.repository

import android.util.Log
import com.example.moment.data.models.*
import com.example.moment.data.network.MomentApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Repository for KYC verification operations
 */
class KYCRepository(private val apiClient: MomentApiClient) {
    
    /**
     * Submit license verification with front, back images and selfie
     */
    suspend fun verifyLicense(
        licenseFrontFile: File,
        licenseBackFile: File,
        selfieFile: File
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val licenseFrontBody = licenseFrontFile.asRequestBody("image/*".toMediaType())
            val licenseBackBody = licenseBackFile.asRequestBody("image/*".toMediaType())
            val selfieBody = selfieFile.asRequestBody("image/*".toMediaType())
            
            val licenseFrontPart = MultipartBody.Part.createFormData(
                "license_image_front", licenseFrontFile.name, licenseFrontBody
            )
            val licenseBackPart = MultipartBody.Part.createFormData(
                "license_image_back", licenseBackFile.name, licenseBackBody
            )
            val selfiePart = MultipartBody.Part.createFormData(
                "selfie_image", selfieFile.name, selfieBody
            )
            
            val response = apiClient.kycApi.verifyLicense(
                licenseFrontPart, licenseBackPart, selfiePart
            )
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("License verification failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("KYCRepository", "Error verifying license", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current KYC verification status
     */
    suspend fun getVerificationStatus(): Result<KYCVerificationRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.kycApi.getVerificationStatus()
            
            if (response.isSuccessful) {
                val verification = response.body()
                if (verification != null) {
                    Result.success(verification)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else if (response.code() == 404) {
                Result.failure(Exception("No verification found"))
            } else {
                Result.failure(Exception("Get verification status failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("KYCRepository", "Error getting verification status", e)
            Result.failure(e)
        }
    }
}

/**
 * Repository for notifications and user reviews
 */
class NotificationRepository(private val apiClient: MomentApiClient) {
    
    /**
     * Get unread notifications for current user
     */
    suspend fun getNotifications(): Result<NotificationsRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.notificationApi.getNotifications()
            
            if (response.isSuccessful) {
                val notifications = response.body()
                if (notifications != null) {
                    Result.success(notifications)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Get notifications failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting notifications", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.notificationApi.deleteNotification(notificationId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete notification failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.notificationApi.markNotificationAsRead(notificationId)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Mark notification as read failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get pending user review prompts
     */
    suspend fun getUserReviews(): Result<UserReviewPromptsRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.notificationApi.getUserReviews()
            
            if (response.isSuccessful) {
                val reviews = response.body()
                if (reviews != null) {
                    Result.success(reviews)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Get user reviews failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting user reviews", e)
            Result.failure(e)
        }
    }
    
    /**
     * Submit user review decision
     */
    suspend fun submitUserReview(
        matchIdToken: String,
        allowRematch: Boolean
    ): Result<UserReviewDecisionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.notificationApi.submitUserReview(
                matchIdToken,
                UserReviewDecisionRequest(allowRematch)
            )
            
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    Result.success(result)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Submit user review failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error submitting user review", e)
            Result.failure(e)
        }
    }
}

/**
 * Repository for reporting and safety operations
 */
class ReportRepository(private val apiClient: MomentApiClient) {
    
    /**
     * Create a new report
     */
    suspend fun createReport(
        userToken: String,
        reason: String,
        description: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.reportApi.createReport(
                CreateReportRequest(userToken, reason, description)
            )
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Create report failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error creating report", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add evidence to an existing report
     */
    suspend fun addEvidence(reportId: Int, evidenceFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestFile = evidenceFile.asRequestBody("application/octet-stream".toMediaType())
            val body = MultipartBody.Part.createFormData("file", evidenceFile.name, requestFile)
            
            val response = apiClient.reportApi.addEvidence(reportId, body)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Add evidence failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error adding evidence", e)
            Result.failure(e)
        }
    }
}
