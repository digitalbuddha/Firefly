package com.androiddev.social.timeline.data


//interface HomeTimelineRepository {
//    suspend fun stream(feedRequest: FeedRequest): Flow<StoreResponse<List<StatusDB>>>
//    suspend fun paging(feedRequest: FeedRequest): PagingSource<Int, StatusDB>
//    suspend fun post(newStatus: NewStatus): Status
//}

sealed class FeedType(val type: String) {
    object Home : FeedType("HOME")
    object Local : FeedType("LOCAL")
}

data class FeedRequest(val feedType: FeedType, val before: String)
data class NewStatus(val content: String)

//
//@ContributesBinding(UserScope::class)
//@SingleIn(UserScope::class)
//class RealHomeTimelineRepository @Inject constructor(
//    private val userApi: UserApi,
//    private val oauthRepository: OauthRepository,
//    private val dao: StatusDao
//) : HomeTimelineRepository {
////    private val store = StoreBuilder
////        .from(
////            fetcher = timelineFetcher(),
////            sourceOfTruth = sourceOfTruth()
////        )
////        .build()
//
//    override suspend fun stream(feedRequest: FeedRequest) =
//        store.stream(StoreRequest.cached(feedRequest, refresh = true))
//
//    override suspend fun paging(feedRequest: FeedRequest): PagingSource<Int, StatusDB> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun post(newStatus: NewStatus): Status {
//        val token = oauthRepository.getCurrent()
//        return userApi.newStatus(" Bearer $token", newStatus.content).also {
//            dao.insertAll(listOf(it.toStatusDb()))
//        }
//    }
//
//    fun timelineFetcher(): Fetcher<FeedRequest, List<Status>> =
//        Fetcher.of { params: FeedRequest ->
//            val token = oauthRepository.getCurrent()
//            val before = params.before
//            userApi.getTimeline(" Bearer $token", since = before)
//        }
//
////    fun sourceOfTruth(): SourceOfTruth<FeedRequest, List<Status>, List<StatusDB>> =
////        SourceOfTruth.of(
////            reader = { dao.getAll() },
////            writer = { _, input: List<Status> -> dao.insertAll(input.map { it.toStatusDb() }) }
////        )
//}
