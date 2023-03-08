package com.androiddev.social.timeline.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//interface HomeTimelineRepository {
//    suspend fun stream(feedRequest: FeedRequest): Flow<StoreResponse<List<StatusDB>>>
//    suspend fun paging(feedRequest: FeedRequest): PagingSource<Int, StatusDB>
//    suspend fun post(newStatus: NewStatus): Status
//}

enum class FeedType(val type: String) {
    Home("Home"),
    Local("Local"),

    Federated("Federated"),
    Trending("Trending"),
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

)