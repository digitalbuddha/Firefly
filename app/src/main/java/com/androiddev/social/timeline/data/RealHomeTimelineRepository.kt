package com.androiddev.social.timeline.data


//interface HomeTimelineRepository {
//    suspend fun stream(feedRequest: FeedRequest): Flow<StoreResponse<List<StatusDB>>>
//    suspend fun paging(feedRequest: FeedRequest): PagingSource<Int, StatusDB>
//    suspend fun post(newStatus: NewStatus): Status
//}

sealed class FeedType(val type: String) {
    object Home : FeedType("Home")
    object Local : FeedType("Local")

    object Federated : FeedType("Federated")
    object Favorites : FeedType("Favorites")
}

data class FeedRequest(val feedType: FeedType, val before: String)
data class NewStatus(val content: String)
