package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for notifications and user reviews
 */

@JsonClass(generateAdapter = true)
data class NotificationsRead(
    @Json(name = "notifications") val notifications: List<Notification>
)

@JsonClass(generateAdapter = true)
data class Notification(
    @Json(name = "id") val id: Int,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class UserReviewPromptsRead(
    @Json(name = "prompts") val prompts: List<UserReviewPrompt>
)

@JsonClass(generateAdapter = true)
data class UserReviewPrompt(
    @Json(name = "match_id_token") val matchIdToken: String,
    @Json(name = "user") val user: MatchedUser,
    @Json(name = "matched_at") val matchedAt: String
)

@JsonClass(generateAdapter = true)
data class UserReviewDecisionRequest(
    @Json(name = "allow_rematch") val allowRematch: Boolean
)

@JsonClass(generateAdapter = true)
data class UserReviewDecisionResponse(
    @Json(name = "message") val message: String
)
