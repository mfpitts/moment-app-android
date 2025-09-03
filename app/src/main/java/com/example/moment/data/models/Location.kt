package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for location-based matching
 */

@JsonClass(generateAdapter = true)
data class LocationMessage(
    @Json(name = "type") val type: String,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class MatchMessage(
    @Json(name = "type") val type: String,
    @Json(name = "user") val user: MatchedUser? = null
)

@JsonClass(generateAdapter = true)
data class SessionEndMessage(
    @Json(name = "type") val type: String,
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class MatchedUser(
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "age") val age: Int?,
    @Json(name = "bio") val bio: String?,
    @Json(name = "profile_picture_url") val profilePictureUrl: String?
)

@JsonClass(generateAdapter = true)
data class EligibilityResponse(
    @Json(name = "missing") val missing: List<String>? = null
)
