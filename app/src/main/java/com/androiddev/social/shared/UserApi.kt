package com.androiddev.social.shared

import com.androiddev.social.search.SearchResult
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.NewStatus
import com.androiddev.social.timeline.data.Notification
import com.androiddev.social.timeline.data.Poll
import com.androiddev.social.timeline.data.Relationship
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.Tag
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


interface UserApi {
    @GET("api/v1/timelines/home")
    suspend fun getHomeTimeline(
        @Header("Authorization") authHeader: String?,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String?
    ): List<Status>

    @GET("/api/v1/timelines/tag/{tag}")
    suspend fun getTagTimeline(
        @Header("Authorization") authHeader: String?,
        @Path("tag") tag: String,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String?
    ): List<Status>

    @GET("api/v1/timelines/public")
    suspend fun getLocalTimeline(
        @Header("Authorization") authHeader: String,
        @Query("local") localOnly: Boolean = true,
        @Query("limit") limit: String = "40",
        @Query("max_id") since: String? = null,
    ): List<Status>

    @GET("/api/v1/trends/statuses")
    suspend fun getTrending(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: String = "40",
        @Query("offset") offset: String?,
    ): List<Status>

    @GET("/api/v1/notifications")
    suspend fun conversations(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: String = "40",
        @Query("types[]") types: Set<String>? = setOf("mention"),
//        @Query("exclude_types[]") excludes: Set<String>?,
    ): List<Notification>

    @GET("/api/v2/search")
    suspend fun search(
        @Header("Authorization") authHeader: String,
        @Query("q") searchTerm: String,
        @Query("limit") limit: String = "40",
        @Query("resolve") resolve: Boolean = false,
        @Query("following") following: Boolean = false,
    ): SearchResult

    @GET("/api/v1/notifications")
    suspend fun notifications(
        @Header("Authorization") authHeader: String,
        @Query("offset") offset: String?,
        @Query("limit") limit: String = "40",
//        @Query("types[]") types:Set<String>? = setOf("mention", "status"),
//        @Query("exclude_types[]") excludes: Set<String>?,
    ): List<Notification>


    @POST("/api/v1/statuses")
    suspend fun newStatus(
        @Header("Authorization") authHeader: String,
        @Body status: NewStatus,
    ): Status

    @GET("/api/v1/statuses/{id}")
    suspend fun getStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @GET("api/v1/accounts/{id}/statuses")
    suspend fun accountStatuses(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String,
        @Query("max_id") since: String?,
        @Query("exclude_replies") excludeReplies: Boolean? = null,
        @Query("only_media") onlyMedia: Boolean? = null,
        @Query("pinned") pinned: Boolean? = null,
        @Query("limit") limit: Int? = 40,
    ): List<Status>

    @GET("/api/v1/bookmarks")
    suspend fun bookmarkedStatuses(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int? = 40,
    ): Response<List<Status>>

    @GET
    suspend fun bookmarkedStatuses(
        @Header("Authorization") authHeader: String,
        @Url url: String,
    ): Response<List<Status>>

    @GET("/api/v1/favourites")
    suspend fun favorites(
        @Header("Authorization") authHeader: String,
        @Query("limit") limit: Int? = 40,
    ): Response<List<Status>>

    @GET
    suspend fun favorites(
        @Header("Authorization") authHeader: String,
        @Url url: String,
    ): Response<List<Status>>

    @GET("api/v1/accounts/{id}/followers")
    suspend fun followers(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String,
        @Query("max_id") since: String?,
        @Query("limit") limit: Int? = 40,
    ): Response<List<Account>>

    @GET
    suspend fun followers(
        @Header("Authorization") authHeader: String,
        @Url url: String
    ): Response<List<Account>>


    @GET("api/v1/accounts/{id}/following")
    suspend fun following(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String,
        @Query("max_id") since: String?,
        @Query("limit") limit: Int? = 40,
    ): Response<List<Account>>

    @GET
    suspend fun following(
        @Header("Authorization") authHeader: String,
        @Url url: String
    ): Response<List<Account>>


    @GET("api/v1/accounts/relationships")
    suspend fun relationships(
        @Header("Authorization") authHeader: String,
        @Query("id[]") accountIds: List<String>
    ): List<Relationship>

    @GET("api/v1/accounts/{id}")
    suspend fun account(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String,
    ): Account

    @Serializable
    data class StatusNode(val ancestors: List<Status>, val descendants: List<Status>)

    @GET("api/v1/statuses/{id}/context")
    suspend fun conversation(
        @Header("Authorization") authHeader: String,
        @Path("id") statusId: String
    ): StatusNode

    @POST("/api/v1/statuses/{id}/reblog")
    suspend fun boostStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @POST("/api/v1/statuses/{id}/unreblog")
    suspend fun unBoostStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @POST("api/v1/accounts/{id}/follow")
    suspend fun followAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String
    ): Relationship

    @POST("api/v1/accounts/{id}/unfollow")
    suspend fun unfollowAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") accountId: String
    ): Relationship


    @POST("/api/v1/tags/{name}/follow")
    suspend fun followTag(
        @Header("Authorization") authHeader: String,
        @Path("name") name: String,
    ): Tag

    @POST("/api/v1/tags/{name}/unfollow")
    suspend fun unfollowTag(
        @Header("Authorization") authHeader: String,
        @Path("name") name: String,
    ): Tag

    @POST("/api/v1/statuses/{id}/favourite")
    suspend fun favouriteStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @POST("/api/v1/statuses/{id}/unfavourite")
    suspend fun unfavouriteStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @POST("/api/v1/statuses/{id}/bookmark")
    suspend fun bookmarkStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Status

    @GET("api/v1/accounts/verify_credentials")
    suspend fun accountVerifyCredentials(
        @Header("Authorization") authHeader: String,
    ): Account

    @GET("/api/v1/followed_tags")
    suspend fun followedTags(
        @Header("Authorization") authHeader: String,
    ): List<Tag>

    @Serializable
    data class UploadIds(
        val id: String
    )

    @Multipart
    @POST("api/v2/media")
    suspend fun upload(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part description: MultipartBody.Part? = null,
        @Part focus: MultipartBody.Part? = null
    ): UploadIds

    @GET("/api/v1/polls/{id}")
    suspend fun viewPoll(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    ): Poll

    @POST("/api/v1/polls/{id}/votes")
    suspend fun votePoll(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
        @Query("choices[]") choices: List<Int>,
    ): Poll

    @DELETE("/api/v1/statuses/{id}")
    suspend fun deleteStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    )

    @POST("/api/v1/accounts/{id}/mute")
    suspend fun muteAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    )

    @POST("/api/v1/accounts/{id}/unmute")
    suspend fun unMuteAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    )

    @POST("/api/v1/accounts/{id}/block")
    suspend fun blockAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    )

    @POST("/api/v1/accounts/{id}/unblock")
    suspend fun unblockAccount(
        @Header("Authorization") authHeader: String,
        @Path("id") id: String,
    )
}
