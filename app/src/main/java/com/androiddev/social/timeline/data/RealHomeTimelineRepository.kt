package com.androiddev.social.timeline.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//interface HomeTimelineRepository {
//    suspend fun stream(feedRequest: FeedRequest): Flow<StoreResponse<List<StatusDB>>>
//    suspend fun paging(feedRequest: FeedRequest): PagingSource<Int, StatusDB>
//    suspend fun post(newStatus: NewStatus): Status
//}

enum class FeedType(val type: String, ) {
    Home("Home"),
    Local("Local"),

    Federated("Federated"),
    User("User"),
    UserWithMedia("UserWithMedia"),
    UserWithReplies("UserWithReplies"),
    Trending("Trending"),
    Bookmarks("Bookmarks"),
    Favorites("Favorites"),
    Hashtag("Hashtag");

    var tagName: String = ""
//    Mention("Mention")
//    object Favorites : FeedType("Favorites")
}

data class FeedRequest(val feedType: FeedType, val before: String)

@Serializable
data class NewStatus(
    val status: String,
//    @SerialName("spoiler_text") val warningText: String,
    @SerialName("in_reply_to_id") val replyStatusId: String?,
    val visibility: String,
//    val sensitive: Boolean,
    @SerialName("media_ids") val mediaIds: List<String>?,
//    @SerialName("scheduled_at") val scheduledAt: String?,
    val poll: NewPoll? = null,
)

@Serializable
data class NewPoll(
    /**
     * Possible answers to the poll. If provided, [NewStatus.mediaIds] cannot be used, and [expiresIn] must be provided.
     */
    val options: List<String> = emptyList(),
    /**
     * Duration that the poll should be open, in seconds. If provided, [NewStatus.mediaIds] cannot be used, and [options] must be provided.
     */
    @SerialName("expires_in") val expiresIn: Int,
    /**
     * Allow multiple choices? Defaults to false.
     */
    val multiple: Boolean = false,
    /**
     * Hide vote counts until the poll ends? Defaults to false.
     */
    @SerialName("hide_totals") val hideTotals: Boolean = false,
)