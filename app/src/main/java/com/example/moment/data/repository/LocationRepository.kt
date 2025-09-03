package com.example.moment.data.repository

import android.util.Log
import com.example.moment.data.models.*
import com.example.moment.data.network.LocationWebSocketClient
import com.example.moment.data.network.MomentApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext

/**
 * Repository for location-based matching operations
 * Manages WebSocket connections and eligibility checks
 */
class LocationRepository(
    private val apiClient: MomentApiClient,
    private val webSocketClient: LocationWebSocketClient
) {
    
    /**
     * Check eligibility for location-based matching
     */
    suspend fun checkEligibility(): Result<EligibilityResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.locationApi.checkEligibility()
            
            if (response.isSuccessful) {
                val eligibility = response.body()
                if (eligibility != null) {
                    Result.success(eligibility)
                } else {
                    // Empty response means eligible
                    Result.success(EligibilityResponse())
                }
            } else {
                Result.failure(Exception("Check eligibility failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error checking eligibility", e)
            Result.failure(e)
        }
    }
    
    /**
     * Connect to location WebSocket for real-time matching
     */
    fun connectToLocationMatching() {
        webSocketClient.connect()
    }
    
    /**
     * Disconnect from location WebSocket
     */
    fun disconnectFromLocationMatching() {
        webSocketClient.disconnect()
    }
    
    /**
     * Send location update to the WebSocket
     */
    fun sendLocationUpdate(latitude: Double, longitude: Double) {
        webSocketClient.sendLocationUpdate(latitude, longitude)
    }
    
    /**
     * Check if WebSocket is connected
     */
    fun isConnectedToLocationMatching(): Boolean {
        return webSocketClient.isConnected()
    }
    
    /**
     * Get match events from WebSocket
     */
    fun getMatchEvents(): SharedFlow<MatchMessage> {
        return webSocketClient.matchEvents
    }
    
    /**
     * Get session end events from WebSocket
     */
    fun getSessionEndEvents(): SharedFlow<SessionEndMessage> {
        return webSocketClient.sessionEndEvents
    }
    
    /**
     * Get connection events from WebSocket
     */
    fun getConnectionEvents(): SharedFlow<LocationWebSocketClient.ConnectionEvent> {
        return webSocketClient.connectionEvents
    }
    
    /**
     * Cleanup WebSocket resources
     */
    fun cleanup() {
        webSocketClient.cleanup()
    }
}
