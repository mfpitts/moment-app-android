package com.example.moment.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.moment.BuildConfig
import com.example.moment.data.api.*
import com.example.moment.data.models.RefreshTokenRequest
import com.example.moment.data.models.TokenResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Network client with authentication and device hash validation
 * Follows the Moment app authentication patterns with JWT + device hash
 */
class MomentApiClient private constructor(
    private val context: Context,
    private val deviceHash: String
) {
    
    companion object {
        @Volatile
        private var INSTANCE: MomentApiClient? = null
        
        fun getInstance(context: Context): MomentApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MomentApiClient(
                    context.applicationContext,
                    generateDeviceHash(context)
                ).also { INSTANCE = it }
            }
        }
        
        private fun generateDeviceHash(context: Context): String {
            // Generate a unique device hash based on device properties
            // In production, you might want to use Android ID or other device identifiers
            val deviceInfo = "${android.os.Build.MODEL}-${android.os.Build.SERIAL}-${context.packageName}"
            return MessageDigest.getInstance("SHA-256")
                .digest(deviceInfo.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences("moment_auth", Context.MODE_PRIVATE)
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("MomentApi", message)
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val newRequest = addAuthHeaders(request)
        val response = chain.proceed(newRequest)
        
        // Handle 401 responses - attempt token refresh
        if (response.code == 401 && !request.url.encodedPath.contains("refresh-token")) {
            response.close()
            val refreshed = refreshAccessToken()
            if (refreshed) {
                // Retry with new token
                val retryRequest = addAuthHeaders(request)
                chain.proceed(retryRequest)
            } else {
                // Refresh failed, clear tokens and return original response
                clearTokens()
                response
            }
        } else {
            response
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    
    // API service instances
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val userApi: UserApiService = retrofit.create(UserApiService::class.java)
    val kycApi: KYCApiService = retrofit.create(KYCApiService::class.java)
    val locationApi: LocationApiService = retrofit.create(LocationApiService::class.java)
    val notificationApi: NotificationApiService = retrofit.create(NotificationApiService::class.java)
    val reportApi: ReportApiService = retrofit.create(ReportApiService::class.java)
    val adminApi: AdminApiService = retrofit.create(AdminApiService::class.java)
    
    /**
     * Add authentication headers to request
     */
    private fun addAuthHeaders(request: Request): Request {
        val builder = request.newBuilder()
            .addHeader("x-device-hash", deviceHash)
        
        // Add access token if available and not an auth endpoint
        val accessToken = getAccessToken()
        if (accessToken != null && !isAuthEndpoint(request)) {
            builder.addHeader("Authorization", "Bearer $accessToken")
        }
        
        return builder.build()
    }
    
    /**
     * Check if request is to an authentication endpoint
     */
    private fun isAuthEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/auth/") || path.contains("/api/v1/auth/")
    }
    
    /**
     * Attempt to refresh the access token
     */
    private fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = getRefreshToken() ?: return false
            
            // Create a separate retrofit instance without auth interceptor to avoid recursion
            val refreshRetrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("x-device-hash", deviceHash)
                                .build()
                            chain.proceed(request)
                        }
                        .build()
                )
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            
            val refreshApi = refreshRetrofit.create(AuthApiService::class.java)
            
            runBlocking {
                val response = refreshApi.refreshToken(
                    deviceHash,
                    RefreshTokenRequest(refreshToken)
                )
                
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    if (tokenResponse != null) {
                        saveTokens(tokenResponse)
                        true
                    } else {
                        false
                    }
                } else {
                    Log.w("MomentApi", "Token refresh failed: ${response.code()}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("MomentApi", "Error refreshing token", e)
            false
        }
    }
    
    /**
     * Save authentication tokens
     */
    fun saveTokens(tokenResponse: TokenResponse) {
        prefs.edit().apply {
            putString("access_token", tokenResponse.accessToken)
            putString("refresh_token", tokenResponse.refreshToken)
            putLong("refresh_token_expires_at", tokenResponse.refreshTokenExpiresAt)
            apply()
        }
    }
    
    /**
     * Get current access token
     */
    fun getAccessToken(): String? = prefs.getString("access_token", null)
    
    /**
     * Get current refresh token
     */
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        val refreshExpiry = prefs.getLong("refresh_token_expires_at", 0)
        
        return accessToken != null && refreshToken != null && 
               System.currentTimeMillis() / 1000 < refreshExpiry
    }
    
    /**
     * Clear all authentication tokens
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("refresh_token_expires_at")
            apply()
        }
    }
    
    /**
     * Get device hash for external use
     */
    fun getDeviceHash(): String = deviceHash
}
