package com.androiddev.social.shared

import com.androiddev.social.conversation.Conversation
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.NewStatus
import com.androiddev.social.timeline.data.Status
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.*


interface UserApi {
    @GET("api/v1/timelines/home")
    suspend fun getHomeTimeline(
        @Header("Authorization") authHeader: String?,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String?
    ): List<Status>

    @GET("api/v1/timelines/public")
    suspend fun getLocalTimeline(
        @Header("Authorization") authHeader: String,
        @Query("local") localOnly: Boolean = true,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String?,
    ): List<Status>

    @GET("/api/v1/trends/statuses")
    suspend fun getTrending(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: String = "40",
        @Query("offset") offset: String?,
    ): List<Status>

    @GET("/api/v1/conversations")
    suspend fun conversations(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: String = "40",
    ): List<Conversation>


    @POST("/api/v1/statuses")
    suspend fun newStatus(
        @Header("Authorization") authHeader: String,
        @Body status: NewStatus,
    ): Status

    @Serializable
    data class StatusNode(val ancestors: List<Status>, val descendants: List<Status>)

    @GET("api/v1/statuses/{id}/context")
    suspend fun conversation(
        @Header("Authorization") authHeader: String,
        @Path("id") statusId: String
    ): StatusNode

    fun boostStatus(
        @Header("Authorization") authHeader: String?,
        @Path("id") id: String,
    ): Status

    @POST("/api/v1/statuses/{id}/favourite")
    suspend fun favoriteStatus(
        @Header("Authorization") authHeader: String?,
        @Path("id") id: String,
    ): Status

    @GET("api/v1/accounts/verify_credentials")
    suspend fun accountVerifyCredentials(
        @Header("Authorization") authHeader: String?,
    ): Account

    @Serializable
    data class UploadIds(
        val id: String
    )

    @Multipart
    @POST("api/v2/media")
    suspend fun upload(
        @Header("Authorization") authHeader: String?,
        @Part file: MultipartBody.Part,
        @Part description: MultipartBody.Part? = null,
        @Part focus: MultipartBody.Part? = null
    ): UploadIds

}
