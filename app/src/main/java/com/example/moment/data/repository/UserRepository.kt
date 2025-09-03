package com.example.moment.data.repository

import android.util.Log
import com.example.moment.data.models.*
import com.example.moment.data.network.MomentApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File

/**
 * Repository for user profile and preferences management
 */
class UserRepository(private val apiClient: MomentApiClient) {
    
    /**
     * Get current user profile and preferences
     */
    suspend fun getCurrentUser(): Result<UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.getCurrentUser()
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Get user failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current user", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile information
     */
    suspend fun updateProfile(profile: UserProfileUpdate): Result<UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.updateProfile(profile)
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Update profile failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update user matching preferences
     */
    suspend fun updatePreferences(preferences: UserPreferencesUpdate): Result<UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.updatePreferences(preferences)
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Update preferences failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating preferences", e)
            Result.failure(e)
        }
    }
    
    /**
     * Change user email address
     */
    suspend fun changeEmail(newEmail: String): Result<UserRead> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.changeEmail(ChangeEmailRequest(newEmail))
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Change email failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error changing email", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload profile picture
     */
    suspend fun uploadProfilePicture(imageFile: File): Result<UserRead> = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            
            val response = apiClient.userApi.uploadProfilePicture(body)
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Upload profile picture failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading profile picture", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get profile picture
     */
    suspend fun getProfilePicture(): Result<ResponseBody> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.getProfilePicture()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Get profile picture failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting profile picture", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload session picture for location matching
     */
    suspend fun uploadSessionPicture(imageFile: File): Result<SessionPictureResponse> = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            
            val response = apiClient.userApi.uploadSessionPicture(body)
            
            if (response.isSuccessful) {
                val sessionPicture = response.body()
                if (sessionPicture != null) {
                    Result.success(sessionPicture)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Upload session picture failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading session picture", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get session picture
     */
    suspend fun getSessionPicture(): Result<ResponseBody> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.getSessionPicture()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Get session picture failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting session picture", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate user account
     */
    suspend fun deactivateAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.userApi.deactivateAccount()
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Deactivate account failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error deactivating account", e)
            Result.failure(e)
        }
    }
}
