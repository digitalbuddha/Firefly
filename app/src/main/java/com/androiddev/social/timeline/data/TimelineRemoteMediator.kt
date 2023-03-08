package com.androiddev.social.timeline.data

import androidx.paging.*
import androidx.room.withTransaction
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.squareup.anvil.annotations.ContributesMultibinding
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
abstract class TimelineRemoteMediator : RemoteMediator<Int, StatusDB>() {
    @OptIn(ExperimentalPagingApi::class)
    abstract override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult

}

@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class LocalTimelineRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository
) : TimelineRemoteMediator() {
    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response = userApi.getLocalTimeline(authHeader = " Bearer $token", since = loadKey)


            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.Local) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class HomeTimelineRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository
) : TimelineRemoteMediator() {
    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response = userApi.getHomeTimeline(authHeader = " Bearer $token", since = loadKey)

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.Home) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}


@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class FederatedTimelineRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository,
) : TimelineRemoteMediator() {
    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response = userApi.getLocalTimeline(
                authHeader = " Bearer $token",
                since = loadKey,
                localOnly = false
            )

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.Federated) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class TrendingRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository
) : TimelineRemoteMediator() {
    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem = state.anchorPosition
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem
                }
            }

            val token = oauthRepository.getCurrent()
            val response =
                userApi.getTrending(authHeader = " Bearer $token", offset = loadKey.toString())

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.Trending) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}


@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class UserRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository,
    private val accountRepository: AccountRepository

) : TimelineRemoteMediator() {
    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH
    lateinit var accountId: String
    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response =
                userApi.accountStatuses(
                    authHeader = " Bearer $token",
                    accountId = accountId,
                    since = null,
                    excludeReplies = true
                )

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.User) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class UserWithMediaRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository,
    private val accountRepository: AccountRepository

) : TimelineRemoteMediator() {
    lateinit var accountId: String

    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH
    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response =
                userApi.accountStatuses(
                    authHeader = " Bearer $token",
                    accountId = accountRepository.get(accountId).id,
                    onlyMedia = true,
                    since = null
                )

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.UserWithMedia) })
            }

            MediatorResult.Success(
                endOfPaginationReached = false
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

@ExperimentalPagingApi
@ContributesMultibinding(UserScope::class, boundType = TimelineRemoteMediator::class)
@SingleIn(UserScope::class)
class UserWithRepliesRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository,
    private val accountRepository: AccountRepository

) : TimelineRemoteMediator() {
    lateinit var accountId: String

    override suspend fun initialize(): InitializeAction = InitializeAction.SKIP_INITIAL_REFRESH
    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    state
                    val lastItem: StatusDB? = state.lastItemOrNull()
                    if (lastItem == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    lastItem.originalId
                }
            }

            val token = oauthRepository.getCurrent()
            val response =
                userApi.accountStatuses(
                    authHeader = " Bearer $token",
                    accountId = accountRepository.get(accountId).id,
                    since = loadKey,
                    excludeReplies = false
                )

            database.withTransaction {
                dao.insertAll(response.map { it.toStatusDb(FeedType.UserWithReplies) })
            }

            MediatorResult.Success(
                endOfPaginationReached = response.isEmpty()
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

