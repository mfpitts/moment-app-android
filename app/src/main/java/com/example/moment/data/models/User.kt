package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for user profile and preferences
 */

@JsonClass(generateAdapter = true)
data class UserRead(
    @Json(name = "id") val id: Int,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_admin") val isAdmin: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "profile") val profile: UserProfile?,
    @Json(name = "preferences") val preferences: UserPreferences?
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?,
    @Json(name = "bio") val bio: String?,
    @Json(name = "age") val age: Int?,
    @Json(name = "profile_picture_url") val profilePictureUrl: String?
)

@JsonClass(generateAdapter = true)
data class UserPreferences(
    @Json(name = "age_min") val ageMin: Int?,
    @Json(name = "age_max") val ageMax: Int?,
    @Json(name = "max_distance") val maxDistance: Int?
)

@JsonClass(generateAdapter = true)
data class UserProfileUpdate(
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?,
    @Json(name = "bio") val bio: String?,
    @Json(name = "age") val age: Int?
)

@JsonClass(generateAdapter = true)
data class UserPreferencesUpdate(
    @Json(name = "age_min") val ageMin: Int?,
    @Json(name = "age_max") val ageMax: Int?,
    @Json(name = "max_distance") val maxDistance: Int?
)

@JsonClass(generateAdapter = true)
data class ChangeEmailRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class SessionPictureResponse(
    @Json(name = "filename") val filename: String
)
