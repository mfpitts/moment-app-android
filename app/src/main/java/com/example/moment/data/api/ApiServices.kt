package com.example.moment.data.api

import com.example.moment.data.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interfaces for all Moment app endpoints
 */

interface AuthApiService {
    @POST("api/v1/auth/send-otp/")
    suspend fun sendOtp(
        @Header("x-device-hash") deviceHash: String,
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("api/v1/auth/verify-otp/")
    suspend fun verifyOtp(
        @Header("x-device-hash") deviceHash: String,
        @Body request: VerifyOtpRequest
    ): Response<TokenResponse>

    @POST("api/v1/auth/refresh-token/")
    suspend fun refreshToken(
        @Header("x-device-hash") deviceHash: String,
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>

    @POST("api/v1/auth/logout/")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>
}

interface UserApiService {
    @GET("api/v1/user/me/")
    suspend fun getCurrentUser(): Response<UserRead>

    @PATCH("api/v1/user/me/profile/")
    suspend fun updateProfile(@Body profile: UserProfileUpdate): Response<UserRead>

    @PATCH("api/v1/user/me/preferences/")
    suspend fun updatePreferences(@Body preferences: UserPreferencesUpdate): Response<UserRead>

    @PATCH("api/v1/user/me/change-email/")
    suspend fun changeEmail(@Body request: ChangeEmailRequest): Response<UserRead>

    @Multipart
    @POST("api/v1/user/me/profile-picture/")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): Response<UserRead>

    @GET("api/v1/user/me/profile-picture/")
    suspend fun getProfilePicture(): Response<ResponseBody>

    @Multipart
    @POST("api/v1/user/me/session-picture/")
    suspend fun uploadSessionPicture(
        @Part file: MultipartBody.Part
    ): Response<SessionPictureResponse>

    @GET("api/v1/user/me/session-picture/")
    suspend fun getSessionPicture(): Response<ResponseBody>

    @DELETE("api/v1/user/me/")
    suspend fun deactivateAccount(): Response<Unit>
}

interface KYCApiService {
    @Multipart
    @POST("api/v1/kyc/verify-license/")
    suspend fun verifyLicense(
        @Part licenseFront: MultipartBody.Part,
        @Part licenseBack: MultipartBody.Part,
        @Part selfie: MultipartBody.Part
    ): Response<Unit>

    @GET("api/v1/kyc/verify-license/status/")
    suspend fun getVerificationStatus(): Response<KYCVerificationRead>
}

interface LocationApiService {
    @GET("api/v1/location/eligibility/")
    suspend fun checkEligibility(): Response<EligibilityResponse>
}

interface NotificationApiService {
    @GET("api/v1/notifications/")
    suspend fun getNotifications(): Response<NotificationsRead>

    @DELETE("api/v1/notifications/{notification_id}/")
    suspend fun deleteNotification(@Path("notification_id") notificationId: Int): Response<Unit>

    @PATCH("api/v1/notifications/{notification_id}/read/")
    suspend fun markNotificationAsRead(@Path("notification_id") notificationId: Int): Response<Unit>

    @GET("api/v1/user-reviews/")
    suspend fun getUserReviews(): Response<UserReviewPromptsRead>

    @POST("api/v1/user-reviews/{match_id_token}/")
    suspend fun submitUserReview(
        @Path("match_id_token") matchIdToken: String,
        @Body decision: UserReviewDecisionRequest
    ): Response<UserReviewDecisionResponse>
}

interface ReportApiService {
    @POST("api/v1/reports/")
    suspend fun createReport(@Body report: CreateReportRequest): Response<Unit>

    @Multipart
    @POST("api/v1/reports/{report_id}/evidence")
    suspend fun addEvidence(
        @Path("report_id") reportId: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>
}

interface AdminApiService {
    @GET("api/v1/admin/reports/")
    suspend fun getReports(
        @Query("status") status: String? = null,
        @Query("reason") reason: String? = null,
        @Query("reporter_id") reporterId: Int? = null,
        @Query("reported_user_id") reportedUserId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<ReportListResponse>

    @PATCH("api/v1/admin/reports/{report_id}")
    suspend fun updateReport(
        @Path("report_id") reportId: Int,
        @Body update: UpdateReportRequest
    ): Response<ReportRead>

    @POST("api/v1/admin/user/{user_id}/ban")
    suspend fun banUser(@Path("user_id") userId: Int): Response<Unit>

    @POST("api/v1/admin/user/{user_id}/unban")
    suspend fun unbanUser(@Path("user_id") userId: Int): Response<Unit>

    @DELETE("api/v1/admin/user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Unit>

    @PATCH("api/v1/admin/user/{user_id}/promote")
    suspend fun promoteUser(@Path("user_id") userId: Int): Response<Unit>

    @PATCH("api/v1/admin/user/{user_id}/demote")
    suspend fun demoteUser(@Path("user_id") userId: Int): Response<Unit>
}
