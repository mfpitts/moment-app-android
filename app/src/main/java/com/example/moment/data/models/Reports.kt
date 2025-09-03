package com.example.moment.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for reporting and safety
 */

@JsonClass(generateAdapter = true)
data class CreateReportRequest(
    @Json(name = "user_token") val userToken: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String
)

@JsonClass(generateAdapter = true)
data class ReportListResponse(
    @Json(name = "reports") val reports: List<ReportRead>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "page_size") val pageSize: Int
)

@JsonClass(generateAdapter = true)
data class ReportRead(
    @Json(name = "id") val id: Int,
    @Json(name = "reason") val reason: String,
    @Json(name = "description") val description: String,
    @Json(name = "status") val status: String,
    @Json(name = "action_taken") val actionTaken: String?,
    @Json(name = "admin_notes") val adminNotes: String?,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "reporter") val reporter: User?,
    @Json(name = "reported_user") val reportedUser: User?
)

@JsonClass(generateAdapter = true)
data class UpdateReportRequest(
    @Json(name = "status") val status: String,
    @Json(name = "action_taken") val actionTaken: String,
    @Json(name = "admin_notes") val adminNotes: String
)

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int,
    @Json(name = "email") val email: String,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?
)
