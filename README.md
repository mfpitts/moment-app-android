# Moment Android App - API Integration

This Android app is configured to work with the Moment backend API as described in the endpoint contracts documentation. The app provides a complete client implementation for all backend endpoints including authentication, user management, KYC verification, location-based matching, notifications, and reporting.

## Architecture Overview

The Android app follows clean architecture principles with the following structure:

### Data Layer
- **Models** (`data/models/`): Data classes for all API requests and responses
- **API Services** (`data/api/`): Retrofit interfaces for all endpoints
- **Network Client** (`data/network/`): Main API client with authentication and WebSocket support
- **Repositories** (`data/repository/`): Repository pattern for data access

### Core Components

#### MomentApiClient
The main API client that handles:
- JWT token management with automatic refresh
- Device hash validation (required by all endpoints)
- Authentication interceptors
- Rate limiting awareness
- Network error handling

#### LocationWebSocketClient
WebSocket client for real-time location matching:
- Grid-based connection management
- Heartbeat functionality (30-second intervals)
- Match event handling
- Session management
- Connection state monitoring

## Configuration

### Build Configuration
The app uses `BuildConfig` fields for environment configuration:
- `API_URL`: Backend API base URL
- `API_KEY`: Optional API key for development
- `ENVIRONMENT`: Current environment (development/staging/production)

### Local Configuration
Set these values in `android/local.properties`:
```properties
API_URL=https://your-backend-api.com
API_KEY=your-optional-api-key
ENVIRONMENT=development
```

## Authentication Flow

### 1. Send OTP
```kotlin
val authRepository = AuthRepository(apiClient)

authRepository.sendOtp(email, phone)
    .onSuccess { response ->
        // OTP sent, show verification UI
        println("OTP expires at: ${response.expiresAt}")
    }
    .onFailure { error ->
        // Handle error
    }
```

### 2. Verify OTP
```kotlin
authRepository.verifyOtp(email, phone, otp)
    .onSuccess { tokenResponse ->
        // User authenticated, tokens saved automatically
        // Status code 200 = existing user login
        // Status code 201 = new user created
    }
    .onFailure { error ->
        // Handle verification error
    }
```

### 3. Token Management
```kotlin
// Check authentication status
if (authRepository.isAuthenticated()) {
    // User is logged in
}

// Manual token refresh (usually automatic)
authRepository.refreshToken()
    .onSuccess { newTokens ->
        // Tokens refreshed successfully
    }

// Logout
authRepository.logout()
    .onSuccess {
        // User logged out, tokens cleared
    }
```

## User Profile Management

### Get Current User
```kotlin
val userRepository = UserRepository(apiClient)

userRepository.getCurrentUser()
    .onSuccess { user ->
        println("User: ${user.email}")
        println("Profile: ${user.profile?.firstName}")
        println("Preferences: ${user.preferences?.ageMin}")
    }
```

### Update Profile
```kotlin
val profileUpdate = UserProfileUpdate(
    firstName = "John",
    lastName = "Doe", 
    bio = "Updated bio",
    age = 25
)

userRepository.updateProfile(profileUpdate)
    .onSuccess { updatedUser ->
        // Profile updated successfully
    }
```

### Upload Profile Picture
```kotlin
val imageFile = File("path/to/image.jpg")

userRepository.uploadProfilePicture(imageFile)
    .onSuccess { updatedUser ->
        // Profile picture uploaded
        println("New picture URL: ${updatedUser.profile?.profilePictureUrl}")
    }
```

## KYC Verification

### Submit License Verification
```kotlin
val kycRepository = KYCRepository(apiClient)

val licenseFront = File("path/to/license_front.jpg")
val licenseBack = File("path/to/license_back.jpg") 
val selfie = File("path/to/selfie.jpg")

kycRepository.verifyLicense(licenseFront, licenseBack, selfie)
    .onSuccess {
        // Verification submitted (Status 202 - processing started)
    }
```

### Check Verification Status
```kotlin
kycRepository.getVerificationStatus()
    .onSuccess { verification ->
        when (verification.status) {
            "pending" -> // Still processing
            "approved" -> // Verification successful
            "rejected" -> // Verification failed: ${verification.failureReason}
        }
    }
```

## Location-Based Matching

### Check Eligibility
```kotlin
val locationRepository = LocationRepository(apiClient, webSocketClient)

locationRepository.checkEligibility()
    .onSuccess { eligibility ->
        if (eligibility.missing == null || eligibility.missing.isEmpty()) {
            // Eligible for location matching
            startLocationMatching()
        } else {
            // Missing requirements: ${eligibility.missing}
        }
    }
```

### Start Location Matching
```kotlin
// Connect to WebSocket
locationRepository.connectToLocationMatching()

// Send location updates
locationRepository.sendLocationUpdate(latitude = 37.7749, longitude = -122.4194)

// Observe match events
lifecycleScope.launch {
    locationRepository.getMatchEvents().collect { match ->
        println("Match found: ${match.user?.firstName}")
        // Handle match UI
    }
}

// Observe session end events
lifecycleScope.launch {
    locationRepository.getSessionEndEvents().collect { sessionEnd ->
        println("Session ended: ${sessionEnd.reason}")
        // Handle session end
    }
}

// Observe connection events  
lifecycleScope.launch {
    locationRepository.getConnectionEvents().collect { event ->
        when (event) {
            is LocationWebSocketClient.ConnectionEvent.Connected -> {
                // Connected successfully
            }
            is LocationWebSocketClient.ConnectionEvent.Unauthorized -> {
                // Handle unauthorized (maybe refresh token)
            }
            is LocationWebSocketClient.ConnectionEvent.Error -> {
                // Handle connection error
            }
        }
    }
}
```

## Notifications and User Reviews

### Get Notifications
```kotlin
val notificationRepository = NotificationRepository(apiClient)

notificationRepository.getNotifications()
    .onSuccess { notifications ->
        notifications.notifications.forEach { notification ->
            println("${notification.title}: ${notification.message}")
        }
    }
```

### Handle User Reviews
```kotlin
notificationRepository.getUserReviews()
    .onSuccess { reviews ->
        reviews.prompts.forEach { prompt ->
            // Show user review UI for each prompt
            // User decides: allow rematch or block
            
            notificationRepository.submitUserReview(
                prompt.matchIdToken,
                allowRematch = true // or false for permanent block
            ).onSuccess { response ->
                println(response.message)
            }
        }
    }
```

## Reporting and Safety

### Create Report
```kotlin
val reportRepository = ReportRepository(apiClient)

reportRepository.createReport(
    userToken = "user_token_from_match",
    reason = MomentConstants.ReportReasons.INAPPROPRIATE_BEHAVIOR,
    description = "User was behaving inappropriately"
).onSuccess {
    // Report submitted successfully
}
```

### Add Evidence
```kotlin
val evidenceFile = File("path/to/evidence.jpg")

reportRepository.addEvidence(reportId = 123, evidenceFile)
    .onSuccess {
        // Evidence added successfully
    }
```

## File Upload Guidelines

The app enforces the same file size limits as the backend:
- Images (JPEG/PNG): 10MB max
- Videos (MP4/MOV): 100MB max  
- PDFs: 5MB max

Use the utility functions to validate files:
```kotlin
val imageFile = File("path/to/image.jpg")

if (imageFile.isValidImage() && imageFile.isWithinSizeLimit(MomentConstants.MAX_IMAGE_SIZE_MB)) {
    // File is valid for upload
}
```

## Rate Limiting Awareness

The app is aware of backend rate limits:
- OTP requests: 2 per minute
- Profile updates: 5 per minute
- Reports: 5 per hour

Handle rate limit errors appropriately in your UI.

## Error Handling

All repository methods return `Result<T>` for consistent error handling:

```kotlin
repository.someOperation()
    .onSuccess { data ->
        // Handle success
    }
    .onFailure { error ->
        when {
            ApiUtils.isAuthError(error) -> {
                // Handle authentication error
                // Maybe redirect to login
            }
            ApiUtils.isNetworkError(error) -> {
                // Handle network error
                // Maybe show retry option
            }
            else -> {
                // Handle other errors
                val message = ApiUtils.getErrorMessage(Result.failure(error))
            }
        }
    }
```

## WebSocket Connection Management

The WebSocket client automatically handles:
- Authentication with JWT tokens and device hash
- Heartbeat messages every 30 seconds
- Connection recovery
- Custom close codes (4001 for unauthorized)

Remember to cleanup WebSocket resources:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    locationRepository.cleanup()
}
```

## Development Tips

1. **Logging**: All API calls are logged in debug builds
2. **Authentication**: Tokens are automatically managed and refreshed
3. **Device Hash**: Automatically generated and included in all requests
4. **Background Tasks**: KYC verification and email sending are asynchronous
5. **File Validation**: Always validate files before upload
6. **Rate Limits**: Respect backend rate limits in your UI

## Required Permissions

The app requires these permissions:
- `INTERNET`: API communication
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Location matching
- `CAMERA`: Profile pictures and KYC verification
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`: File uploads

## Admin Functions

If the user has admin privileges, additional repository methods are available through the `AdminApiService`. These include user management, report moderation, and ban/unban operations.

This implementation provides a complete, production-ready Android client for the Moment app backend API with proper error handling, authentication management, and real-time capabilities.
