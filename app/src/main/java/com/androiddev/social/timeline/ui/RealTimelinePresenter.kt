package com.androiddev.social.timeline.ui

import androidx.compose.material3.ColorScheme
import androidx.paging.*
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.shared.headerLinks
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
    val oauthRepository: OauthRepository,

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

    val bookmarksFlow: Flow<PagingData<Status>> = Pager(
        config = pagingConfig,
    )
    {
        BookmarksPagingSource(userApi = api, oauthRepository = oauthRepository)
    }
        .flow.cachedIn(scope)

    val favoritesFlow: Flow<PagingData<Status>> = Pager(
        config = pagingConfig,
    )
    {
        FavoritesPagingSource(userApi = api, oauthRepository = oauthRepository)
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

                    FeedType.Bookmarks -> {
                        model = model.copy(bookmarkedStatuses = bookmarksFlow.map {
                            it.map {
                                it.toStatusDb(FeedType.Bookmarks)
                                    .mapStatus(colorScheme = event.colorScheme)
                            }
                        })
                    }

                    FeedType.Favorites -> {
                        model = model.copy(favoriteStatuses = favoritesFlow.map {
                            it.map {
                                it.toStatusDb(FeedType.Favorites)
                                    .mapStatus(colorScheme = event.colorScheme)
                            }
                        })
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
        val bookmarkedStatuses: Flow<PagingData<UI>>? = null,
        val favoriteStatuses: Flow<PagingData<UI>>? = null,
        val localStatuses: Flow<PagingData<UI>>? = null,
        val userStatuses: Flow<PagingData<UI>>? = null,
        val userWithMediaStatuses: Flow<PagingData<UI>>? = null,
        val userWithRepliesStatuses: Flow<PagingData<UI>>? = null,
    )

    sealed interface HomeEffect
}


class BookmarksPagingSource(
    val userApi: UserApi,
    val oauthRepository: OauthRepository
) : PagingSource<String, Status>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Status> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key
            val response = if (nextPageNumber == null) {
                userApi.bookmarkedStatuses(
                    authHeader = " Bearer ${oauthRepository.getCurrent()}",
                )
            } else {
                userApi.bookmarkedStatuses(
                    authHeader = " Bearer ${oauthRepository.getCurrent()}",
                    url = nextPageNumber
                )

            }

            val data = response.body()!!
            val links = headerLinks(response)
            return LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = links.second.toString()
            )
        } catch (e: Exception) {
            val cause = e.cause
            return LoadResult.Error(e)

        }
    }

    override fun getRefreshKey(state: PagingState<String, Status>): String? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.plus(-1)
        }
    }
}


class FavoritesPagingSource(
    val userApi: UserApi,
    val oauthRepository: OauthRepository
) : PagingSource<String, Status>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Status> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPageNumber = params.key
            val response = if (nextPageNumber == null) {
                userApi.favorites(
                    authHeader = " Bearer ${oauthRepository.getCurrent()}",
                )
            } else {
                userApi.favorites(
                    authHeader = " Bearer ${oauthRepository.getCurrent()}",
                    url = nextPageNumber
                )

            }

            val data = response.body()!!
            val links = headerLinks(response)
            return LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = links.second.toString()
            )
        } catch (e: Exception) {
            val cause = e.cause
            return LoadResult.Error(e)

        }
    }

    override fun getRefreshKey(state: PagingState<String, Status>): String? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.plus(-1)
        }
    }
}