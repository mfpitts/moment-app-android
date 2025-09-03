package com.example.moment.data.repository

import android.util.Log
import com.example.moment.data.models.*
import com.example.moment.data.network.MomentApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for authentication operations
 * Handles OTP verification, token management, and user sessions
 */
class AuthRepository(private val apiClient: MomentApiClient) {
    
    /**
     * Send OTP to user's email for registration or login
     */
    suspend fun sendOtp(email: String, phone: String): Result<SendOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.sendOtp(
                deviceHash = apiClient.getDeviceHash(),
                request = SendOtpRequest(email, phone)
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Send OTP failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending OTP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verify OTP and obtain authentication tokens
     */
    suspend fun verifyOtp(email: String, phone: String, otp: String): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.authApi.verifyOtp(
                deviceHash = apiClient.getDeviceHash(),
                request = VerifyOtpRequest(email, phone, otp)
            )
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    // Save tokens for future API calls
                    apiClient.saveTokens(tokenResponse)
                    Result.success(tokenResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("OTP verification failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error verifying OTP", e)
            Result.failure(e)
        }
    }
    
    /**
     * Refresh authentication tokens
     */
    suspend fun refreshToken(): Result<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = apiClient.getRefreshToken()
                ?: return@withContext Result.failure(Exception("No refresh token available"))
            
            val response = apiClient.authApi.refreshToken(
                deviceHash = apiClient.getDeviceHash(),
                request = RefreshTokenRequest(refreshToken)
            )
            
            if (response.isSuccessful) {
                val tokenResponse = response.body()
                if (tokenResponse != null) {
                    apiClient.saveTokens(tokenResponse)
                    Result.success(tokenResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Result.failure(Exception("Token refresh failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error refreshing token", e)
            Result.failure(e)
        }
    }
    
    /**
     * Logout user and revoke tokens
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = apiClient.getRefreshToken()
            if (refreshToken != null) {
                val response = apiClient.authApi.logout(LogoutRequest(refreshToken))
                if (!response.isSuccessful) {
                    Log.w("AuthRepository", "Logout request failed: ${response.code()}")
                    // Continue with local logout even if server request fails
                }
            }
            
            // Clear local tokens regardless of server response
            apiClient.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during logout", e)
            // Clear local tokens even on error
            apiClient.clearTokens()
            Result.failure(e)
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean = apiClient.isAuthenticated()
    
    /**
     * Get current access token
     */
    fun getAccessToken(): String? = apiClient.getAccessToken()
}
