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

    abstract suspend fun fetch()
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
            val response = userApi.getTimeline(authHeader = " Bearer $token", since = loadKey)

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dao.delete()
                }
                dao.insertAll(response.map { it.toStatusDb(FeedType.Home) })
//                val homeTimelineAll = dao.getHomeTimelineAll()
//                homeTimelineAll

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

    override suspend fun fetch() {
//        withContext(Dispatchers.IO) {
//            val token = oauthRepository.getCurrent()
//            val response = userApi.getTimeline(
//                authHeader = " Bearer $token", since = null
//            )
//            dao.insertAll(response.map { it.toStatusDb(FeedType.Home) })
//        }
    }
}