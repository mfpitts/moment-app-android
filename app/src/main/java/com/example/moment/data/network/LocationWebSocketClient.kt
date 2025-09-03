package com.example.moment.data.network

import android.util.Log
import com.example.moment.BuildConfig
import com.example.moment.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * WebSocket client for real-time location sharing and proximity matching
 * Implements the grid-based WebSocket architecture from the backend
 */
class LocationWebSocketClient(
    private val apiClient: MomentApiClient
) {
    
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L // 30 seconds
        private const val CUSTOM_WS_CLOSE_CODE_UNAUTHORIZED = 4001
    }
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val locationMessageAdapter = moshi.adapter(LocationMessage::class.java)
    private val matchMessageAdapter = moshi.adapter(MatchMessage::class.java)
    private val sessionEndMessageAdapter = moshi.adapter(SessionEndMessage::class.java)
    
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Event flows for observing WebSocket events
    private val _matchEvents = MutableSharedFlow<MatchMessage>()
    val matchEvents: SharedFlow<MatchMessage> = _matchEvents.asSharedFlow()
    
    private val _sessionEndEvents = MutableSharedFlow<SessionEndMessage>()
    val sessionEndEvents: SharedFlow<SessionEndMessage> = _sessionEndEvents.asSharedFlow()
    
    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>()
    val connectionEvents: SharedFlow<ConnectionEvent> = _connectionEvents.asSharedFlow()
    
    sealed class ConnectionEvent {
        object Connected : ConnectionEvent()
        object Disconnected : ConnectionEvent()
        data class Error(val message: String) : ConnectionEvent()
        object Unauthorized : ConnectionEvent()
    }
    
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Log.i("LocationWebSocket", "WebSocket connected")
            scope.launch {
                _connectionEvents.emit(ConnectionEvent.Connected)
            }
            startHeartbeat()
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("LocationWebSocket", "Received message: $text")
            scope.launch {
                handleMessage(text)
            }
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("LocationWebSocket", "WebSocket closing: $code - $reason")
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i("LocationWebSocket", "WebSocket closed: $code - $reason")
            stopHeartbeat()
            scope.launch {
                if (code == CUSTOM_WS_CLOSE_CODE_UNAUTHORIZED) {
                    _connectionEvents.emit(ConnectionEvent.Unauthorized)
                } else {
                    _connectionEvents.emit(ConnectionEvent.Disconnected)
                }
            }
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e("LocationWebSocket", "WebSocket error", t)
            stopHeartbeat()
            scope.launch {
                _connectionEvents.emit(ConnectionEvent.Error(t.message ?: "Unknown error"))
            }
        }
    }
    
    /**
     * Connect to the location WebSocket
     */
    fun connect() {
        if (webSocket != null) {
            Log.w("LocationWebSocket", "Already connected or connecting")
            return
        }
        
        val accessToken = apiClient.getAccessToken()
        val deviceHash = apiClient.getDeviceHash()
        
        if (accessToken == null) {
            Log.e("LocationWebSocket", "No access token available")
            scope.launch {
                _connectionEvents.emit(ConnectionEvent.Error("Not authenticated"))
            }
            return
        }
        
        // Build WebSocket URL with authentication parameters
        val wsUrl = BuildConfig.API_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://") +
            "api/v1/location/ws?token=$accessToken&device_hash=$deviceHash"
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // No timeout for WebSocket
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val request = Request.Builder()
            .url(wsUrl)
            .build()
        
        webSocket = client.newWebSocket(request, webSocketListener)
    }
    
    /**
     * Disconnect from the WebSocket
     */
    fun disconnect() {
        stopHeartbeat()
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }
    
    /**
     * Send location update
     */
    fun sendLocationUpdate(latitude: Double, longitude: Double) {
        val message = LocationMessage(
            type = "location",
            latitude = latitude,
            longitude = longitude
        )
        
        val json = locationMessageAdapter.toJson(message)
        val success = webSocket?.send(json) ?: false
        
        if (!success) {
            Log.w("LocationWebSocket", "Failed to send location update")
        } else {
            Log.d("LocationWebSocket", "Sent location update: $json")
        }
    }
    
    /**
     * Send heartbeat message
     */
    private fun sendHeartbeat() {
        val message = LocationMessage(type = "heartbeat")
        val json = locationMessageAdapter.toJson(message)
        val success = webSocket?.send(json) ?: false
        
        if (!success) {
            Log.w("LocationWebSocket", "Failed to send heartbeat")
        } else {
            Log.d("LocationWebSocket", "Sent heartbeat")
        }
    }
    
    /**
     * Start periodic heartbeat
     */
    private fun startHeartbeat() {
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                sendHeartbeat()
            }
        }
    }
    
    /**
     * Stop heartbeat
     */
    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    private suspend fun handleMessage(text: String) {
        try {
            // Parse the message to determine type
            val jsonObject = moshi.adapter(Map::class.java).fromJson(text) as? Map<String, Any>
            val type = jsonObject?.get("type") as? String
            
            when (type) {
                "match" -> {
                    val matchMessage = matchMessageAdapter.fromJson(text)
                    if (matchMessage != null) {
                        _matchEvents.emit(matchMessage)
                    }
                }
                "session_end" -> {
                    val sessionEndMessage = sessionEndMessageAdapter.fromJson(text)
                    if (sessionEndMessage != null) {
                        _sessionEndEvents.emit(sessionEndMessage)
                    }
                }
                else -> {
                    Log.w("LocationWebSocket", "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e("LocationWebSocket", "Error parsing WebSocket message", e)
        }
    }
    
    /**
     * Check if WebSocket is connected
     */
    fun isConnected(): Boolean {
        return webSocket != null
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        disconnect()
        scope.cancel()
    }
}
