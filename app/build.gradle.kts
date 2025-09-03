plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

import java.util.Properties

// Load local.properties file for secrets
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.moment"
    compileSdk = 36

    // Enable generation of BuildConfig fields used by the app
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.moment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Read secrets from local.properties, env variables, or use defaults
        val apiKey = localProperties.getProperty("API_KEY") ?: System.getenv("API_KEY") ?: ""
        val apiUrl = localProperties.getProperty("API_URL") ?: System.getenv("API_URL") ?: "https://api.example.com"
        val environment = localProperties.getProperty("ENVIRONMENT") ?: System.getenv("ENVIRONMENT") ?: "development"
        
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "API_URL", "\"$apiUrl\"")
        buildConfigField("String", "ENVIRONMENT", "\"$environment\"")
    }

    buildTypes {
        debug {
            // development values
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}