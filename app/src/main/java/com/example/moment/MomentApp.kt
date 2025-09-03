package com.example.moment

import android.app.Application
import android.util.Log
import com.example.moment.data.network.MomentApiClient

class MomentApp : Application() {
    
    // Global API client instance
    lateinit var apiClient: MomentApiClient
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API client
        apiClient = MomentApiClient.getInstance(this)
        
        // Log BuildConfig values so developers can confirm configuration at startup
        Log.i("MomentApp", "Environment=${BuildConfig.ENVIRONMENT} API_URL=${BuildConfig.API_URL}")
        
        // Kick off a simple connectivity test to the configured API
        ApiTester.testConnection(BuildConfig.API_URL, BuildConfig.API_KEY)
    }
}
