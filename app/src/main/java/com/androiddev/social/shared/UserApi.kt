package com.androiddev.social.shared

import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.Status
import kotlinx.serialization.Serializable
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
        @Header("Authorization") authHeader: String?,
        @Query("local") localOnly: Boolean = true,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String?,
    ): List<Status>

    @GET("/api/v1/trends/statuses")
    suspend fun getTrending(
        @Header("Authorization") authHeader: String?,
        @Query("limit") limit: String = "40",
        @Query("offset") offset: String?,
    ): List<Status>

    @POST("/api/v1/statuses")
    @FormUrlEncoded
    suspend fun newStatus(
        @Header("Authorization") authHeader: String?,
        @Field("in_reply_to_id") replyStatusId: String? = null,
        @Field("status") content: String,
        @Field("visibility") visibility: String,
    ): Status

    @Serializable
    data class Conversation(val ancestors: List<Status>, val descendants: List<Status>)

    @GET("api/v1/statuses/{id}/context")
    suspend fun conversation(
        @Path("id") statusId: String
    ): Conversation

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

}

