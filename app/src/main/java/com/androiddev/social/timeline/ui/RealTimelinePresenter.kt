package com.androiddev.social.timeline.ui

import androidx.compose.material3.ColorScheme
import androidx.paging.*
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.*
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    val scope = CoroutineScope(Dispatchers.Main)
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 30,
        prefetchDistance = 10
    )


    val homeFlow: Flow<PagingData<StatusDB>> = Pager(
        config = pagingConfig,
        remoteMediator = timelineRemoteMediators
            .filterIsInstance<HomeTimelineRemoteMediator>()
            .single()
    )
    {
        statusDao.getTimeline(FeedType.Home.type)
    }
        .flow.cachedIn(scope)
    val localFlow = Pager(
        config = pagingConfig,
        remoteMediator = timelineRemoteMediators
            .filterIsInstance<LocalTimelineRemoteMediator>()
            .single()
    )
    {
        statusDao.getTimeline(FeedType.Local.type)
    }
        .flow.cachedIn(scope)
    val federatedFlow = Pager(
        config = pagingConfig,
        remoteMediator = timelineRemoteMediators
            .filterIsInstance<FederatedTimelineRemoteMediator>()
            .single()
    )
    {
        statusDao.getTimeline(FeedType.Federated.type)
    }
        .flow.cachedIn(scope)
    val trendingFlow = Pager(
        config = pagingConfig,
        remoteMediator = timelineRemoteMediators
            .filterIsInstance<TrendingRemoteMediator>()
            .single()
    )
    {
        statusDao.getTimeline(FeedType.Trending.type)
    }
        .flow.cachedIn(scope)


    override suspend fun eventHandler(event: HomeEvent, scope: CoroutineScope) = withContext(
        Dispatchers.IO
    ) {
        when (event) {
            is Load -> {
                val result =
                    kotlin.runCatching {
                        api.accountVerifyCredentials(authHeader = " Bearer ${oauthRepository.getCurrent()}")
                    }
                result.getOrNull()?.let {
                    model = model.copy(account = it)
                }
                when (event.feedType) {
                    FeedType.Home -> {
                        model = model.copy(homeStatuses = homeFlow.map {
                            it.map {
                                it.mapStatus(event.colorScheme)
                            }
                        })
                    }

                    FeedType.Local -> {
                        model = model.copy(localStatuses = localFlow.map {
                            it.map {
                                it.mapStatus(event.colorScheme)
                            }
                        })
                    }

                    FeedType.Federated -> {
                        model = model.copy(federatedStatuses = federatedFlow.map {
                            it.map {
                                it.mapStatus(event.colorScheme)
                            }
                        })

                    }

                    FeedType.Trending -> {
                        model = model.copy(trendingStatuses = trendingFlow.map {
                            it.map {
                                it.mapStatus(event.colorScheme)
                            }
                        })

                    }

                    FeedType.UserWithReplies -> {
                        val remoteMediator =
                            timelineRemoteMediators.filterIsInstance<UserWithRepliesRemoteMediator>()
                                .single()
                        remoteMediator.accountId = event.accountId!!

                        val flow = Pager(
                            config = PagingConfig(
                                pageSize = 10,
                                initialLoadSize = 10,
                                prefetchDistance = 10
                            ),
                            remoteMediator = remoteMediator
                        ) {
                            statusDao.getUserTimeline(
                                FeedType.UserWithReplies.type,
                                event.accountId
                            )
                        }.flow

                        model = model.copy(
                            userWithRepliesStatuses = flow.map {
                                it.map { it.mapStatus(event.colorScheme) }
                            }.cachedIn(scope)
                        )

                    }

                    FeedType.UserWithMedia -> {
                        val remoteMediator =
                            timelineRemoteMediators.filterIsInstance<UserWithMediaRemoteMediator>()
                                .single()
                        remoteMediator.accountId = event.accountId!!

                        val flow = Pager(
                            config = pagingConfig,
                            remoteMediator = remoteMediator
                        ) {
                            statusDao.getUserTimeline(FeedType.UserWithMedia.type, event.accountId)
                        }.flow

                        model = model.copy(
                            userWithMediaStatuses = flow.map {
                                it.map { it.mapStatus(event.colorScheme) }
                            }.cachedIn(scope)
                        )

                    }

                    FeedType.User -> {
                        val remoteMediator =
                            timelineRemoteMediators.filterIsInstance<UserRemoteMediator>()
                                .single()
                        remoteMediator.accountId = event.accountId!!
                        val flow = Pager(
                            config = pagingConfig,
                            remoteMediator = remoteMediator
                        ) {
                            statusDao.getUserTimeline(FeedType.User.type, event.accountId)
                        }.flow

                        model = model.copy(
                            userStatuses = flow.map {
                                it.map { it.mapStatus(event.colorScheme) }
                            }.cachedIn(scope)
                        )

                    }

                    else -> {}
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
    data class Load(
        val feedType: FeedType,
        val accountId: String? = null,
        val colorScheme: ColorScheme
    ) : HomeEvent


    data class HomeModel(
        val loading: Boolean,
        val homeStatuses: Flow<PagingData<UI>>? = null,
        val account: Account? = null,
        val federatedStatuses: Flow<PagingData<UI>>? = null,
        val trendingStatuses: Flow<PagingData<UI>>? = null,
        val localStatuses: Flow<PagingData<UI>>? = null,
        val userStatuses: Flow<PagingData<UI>>? = null,
        val userWithMediaStatuses: Flow<PagingData<UI>>? = null,
        val userWithRepliesStatuses: Flow<PagingData<UI>>? = null,
    )

    sealed interface HomeEffect
}