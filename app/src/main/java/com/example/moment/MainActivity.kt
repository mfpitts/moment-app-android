package com.example.moment

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.moment.data.repository.AuthRepository
import com.example.moment.data.repository.UserRepository
import com.example.moment.data.repository.LocationRepository
import com.example.moment.data.network.LocationWebSocketClient
import kotlinx.coroutines.launch

/**
 * Main activity demonstrating basic usage of the Moment app API client
 * This serves as an example of how to use the repositories and API services
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var locationRepository: LocationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Get API client from application
        val app = application as MomentApp
        val apiClient = app.apiClient
        
        // Initialize repositories
        authRepository = AuthRepository(apiClient)
        userRepository = UserRepository(apiClient)
        
        val webSocketClient = LocationWebSocketClient(apiClient)
        locationRepository = LocationRepository(apiClient, webSocketClient)
        
        // Example: Check if user is authenticated
        if (authRepository.isAuthenticated()) {
            Log.i("MainActivity", "User is authenticated")
            loadUserProfile()
        } else {
            Log.i("MainActivity", "User not authenticated")
            // Here you would show login/registration UI
        }
        
        // Example: Observe location match events
        observeLocationEvents()
    }
    
    /**
     * Example of loading user profile
     */
    private fun loadUserProfile() {
        lifecycleScope.launch {
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    Log.i("MainActivity", "Loaded user: ${user.email}")
                    // Update UI with user data
                }
                .onFailure { error ->
                    Log.e("MainActivity", "Failed to load user", error)
                    // Handle error - maybe show login screen
                }
        }
    }
    
    /**
     * Example of authentication flow
     */
    private fun authenticateUser(email: String, phone: String) {
        lifecycleScope.launch {
            // Step 1: Send OTP
            authRepository.sendOtp(email, phone)
                .onSuccess { response ->
                    Log.i("MainActivity", "OTP sent: ${response.detail}")
                    // Show OTP input UI
                    // In real app, you'd collect OTP from user input
                    // verifyOtp(email, phone, userEnteredOtp)
                }
                .onFailure { error ->
                    Log.e("MainActivity", "Failed to send OTP", error)
                    // Handle error
                }
        }
    }
    
    /**
     * Example of OTP verification
     */
    private fun verifyOtp(email: String, phone: String, otp: String) {
        lifecycleScope.launch {
            authRepository.verifyOtp(email, phone, otp)
                .onSuccess { tokenResponse ->
                    Log.i("MainActivity", "Authentication successful")
                    // User is now authenticated, load their profile
                    loadUserProfile()
                }
                .onFailure { error ->
                    Log.e("MainActivity", "OTP verification failed", error)
                    // Handle error - maybe show error message
                }
        }
    }
    
    /**
     * Example of starting location-based matching
     */
    private fun startLocationMatching() {
        lifecycleScope.launch {
            // Check eligibility first
            locationRepository.checkEligibility()
                .onSuccess { eligibility ->
                    if (eligibility.missing == null || eligibility.missing.isEmpty()) {
                        Log.i("MainActivity", "Eligible for location matching")
                        locationRepository.connectToLocationMatching()
                        
                        // Send location updates (you'd get these from LocationManager)
                        // locationRepository.sendLocationUpdate(37.7749, -122.4194)
                    } else {
                        Log.w("MainActivity", "Not eligible: ${eligibility.missing}")
                        // Handle missing requirements
                    }
                }
                .onFailure { error ->
                    Log.e("MainActivity", "Failed to check eligibility", error)
                }
        }
    }
    
    /**
     * Example of observing location match events
     */
    private fun observeLocationEvents() {
        lifecycleScope.launch {
            // Observe match events
            locationRepository.getMatchEvents().collect { matchMessage ->
                Log.i("MainActivity", "Match found: ${matchMessage.user?.firstName}")
                // Handle match - show match UI
            }
        }
        
        lifecycleScope.launch {
            // Observe session end events
            locationRepository.getSessionEndEvents().collect { sessionEndMessage ->
                Log.i("MainActivity", "Session ended: ${sessionEndMessage.reason}")
                // Handle session end
            }
        }
        
        lifecycleScope.launch {
            // Observe connection events
            locationRepository.getConnectionEvents().collect { connectionEvent ->
                when (connectionEvent) {
                    is LocationWebSocketClient.ConnectionEvent.Connected -> {
                        Log.i("MainActivity", "Connected to location matching")
                    }
                    is LocationWebSocketClient.ConnectionEvent.Disconnected -> {
                        Log.i("MainActivity", "Disconnected from location matching")
                    }
                    is LocationWebSocketClient.ConnectionEvent.Error -> {
                        Log.e("MainActivity", "Location matching error: ${connectionEvent.message}")
                    }
                    is LocationWebSocketClient.ConnectionEvent.Unauthorized -> {
                        Log.w("MainActivity", "Unauthorized for location matching")
                        // Handle unauthorized - maybe refresh token or show login
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup location resources
        locationRepository.cleanup()
    }
}
