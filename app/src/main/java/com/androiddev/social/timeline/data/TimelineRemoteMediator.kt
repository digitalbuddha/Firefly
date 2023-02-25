package com.androiddev.social.timeline.data

import androidx.paging.*
import androidx.room.withTransaction
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.squareup.anvil.annotations.ContributesBinding
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
@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealTimelineRemoteMediator @Inject constructor(
    private val dao: StatusDao,
    private val database: AppDatabase,
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository
) : TimelineRemoteMediator() {
    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, StatusDB>
    ): MediatorResult {
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
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
                    lastItem.remoteId
                }
            }

            val token = oauthRepository.getCurrent()
            val response = userApi.getTimeline(
                authHeader = " Bearer $token", since = loadKey
            )

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dao.delete()
                }

                // Insert new statuses into database, which invalidates the
                // current PagingData, allowing Paging to present the updates
                // in the DB.
                dao.insertAll(response.map { it.toStatusDb() })
            }
//            val current= dao.getAllSync()
//            current.size
            MediatorResult.Success(
                endOfPaginationReached = false //TODO MIKE - when to flip
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}