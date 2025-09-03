package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for KYC verification
 */

@JsonClass(generateAdapter = true)
data class KYCVerificationRead(
    @Json(name = "id") val id: Int,
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "failure_reason") val failureReason: String?
)
