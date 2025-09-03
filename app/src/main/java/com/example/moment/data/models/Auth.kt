package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for authentication endpoints
 */

@JsonClass(generateAdapter = true)
data class SendOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class SendOtpResponse(
    @Json(name = "detail") val detail: String,
    @Json(name = "expires_at") val expiresAt: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "otp") val otp: String
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "refresh_token_expires_at") val refreshTokenExpiresAt: Long
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class LogoutRequest(
    @Json(name = "refresh_token") val refreshToken: String
)
