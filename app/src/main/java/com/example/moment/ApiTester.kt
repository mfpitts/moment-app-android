package com.example.moment

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.concurrent.thread

object ApiTester {
    private val client = OkHttpClient()

    fun testConnection(url: String, apiKey: String = "") {
        // run in background thread to avoid blocking Application startup
        thread {
            val requestBuilder = Request.Builder().url(url).get()
            
            // Add API key to headers if provided
            if (apiKey.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            
            val request = requestBuilder.build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.w("ApiTester", "Connection test failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        Log.i("ApiTester", "Connection test status=${it.code} url=$url")
                    }
                }
            })
        }
    }
}
