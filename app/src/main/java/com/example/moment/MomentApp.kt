packag    override fun onCreate() {
        super.onCreate()
        // Log BuildConfig values so developers can confirm configuration at startup
        Log.i("MomentApp", "Environment=${BuildConfig.ENVIRONMENT} API_URL=${BuildConfig.API_URL}")
        // Kick off a simple connectivity test to the configured API
        ApiTester.testConnection(BuildConfig.API_URL, BuildConfig.API_KEY)
    }xample.moment

import android.app.Application
import android.util.Log

class MomentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Log BuildConfig values so developers can confirm configuration at startup
        Log.i("MomentApp", "Environment=${BuildConfig.ENVIRONMENT} API_URL=${BuildConfig.API_URL}")
    // Kick off a simple connectivity test to the configured API
    ApiTester.testConnection(BuildConfig.API_URL)
    }
}
