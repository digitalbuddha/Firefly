package com.androiddev.social.timeline.ui

import androidx.paging.*
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.*
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalPagingApi
@ContributesBinding(AuthRequiredScope::class, boundType = TimelinePresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealTimelinePresenter @Inject constructor(
    val timelineRemoteMediators: @JvmSuppressWildcards Set<TimelineRemoteMediator>,
    val statusDao: StatusDao,
    val api: UserApi,
    val oauthRepository: OauthRepository
) : TimelinePresenter() {



    override suspend fun eventHandler(event: HomeEvent) {
        when (event) {
            is Load -> {

                val scope = CoroutineScope(Dispatchers.IO)
                if(event.feedType == FeedType.Home){
                    val remoteMediator =
                        timelineRemoteMediators.filterIsInstance<HomeTimelineRemoteMediator>()
                            .single()
                    val flow = Pager(
                        config = PagingConfig(pageSize = 20, initialLoadSize = 30, prefetchDistance = 10),
                        remoteMediator = remoteMediator
                    ) {
                        val data: PagingSource<Int, StatusDB> = statusDao.getHomeTimeline()
                        data
                    }.flow

                    model = model.copy(
                        statuses = flow.cachedIn(scope)
                    ).also {
                        remoteMediator.fetch()
                    }
//                } else if(event.feedType == FeedType.Local){
//                    val remoteMediator =
//                        timelineRemoteMediators.filterIsInstance<HomeTimelineRemoteMediator>()
//                            .single()
//                    val flow = Pager(
//                        config = PagingConfig(pageSize = 10, initialLoadSize = 10, prefetchDistance = 10),
//                        remoteMediator = remoteMediator
//                    ) {
//                        val data: PagingSource<Int, StatusDB> = statusDao.getHomeTimeline()
//                        data
//                    }.flow
//
//                    model = model.copy(
//                        statuses = flow.cachedIn(scope)
//                    ).also {
//                        remoteMediator.fetch()
//                    }
                }

            }

            is PostMessage -> {
                val result = kotlin.runCatching {
                    api.newStatus(
                        authHeader = " Bearer ${oauthRepository.getCurrent()}",
                        content = event.content
                    )
                }
                if (result.isSuccess) {
                    withContext(Dispatchers.IO) {
                        statusDao.insertAll(
                            listOf(
                                result.getOrThrow().toStatusDb(FeedType.Home)
                            )
                        )
                    }
                }
            }
        }
    }
}


abstract class TimelinePresenter :
    Presenter<TimelinePresenter.HomeEvent, TimelinePresenter.HomeModel, TimelinePresenter.HomeEffect>(
        HomeModel(true)
    ) {
    sealed interface HomeEvent
    data class Load(val feedType: FeedType) : HomeEvent
    data class PostMessage(val content: String) : HomeEvent

    data class HomeModel(
        val loading: Boolean, val statuses: Flow<PagingData<StatusDB>>? = null
    )

    sealed interface HomeEffect
}