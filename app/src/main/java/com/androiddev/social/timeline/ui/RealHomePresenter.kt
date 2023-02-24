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
@ContributesBinding(AuthRequiredScope::class, boundType = HomePresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealHomePresenter @Inject constructor(
    timelineRemoteMediator: TimelineRemoteMediator,
    val statusDao: StatusDao,
    val api: UserApi,
    val oauthRepository: OauthRepository
) : HomePresenter() {

    val flow = Pager(
        config = PagingConfig(pageSize = 40, initialLoadSize = 40),
        remoteMediator = timelineRemoteMediator
    ) {
        val data: PagingSource<Int, StatusDB> = statusDao.getAll()
        data
    }.flow

    override suspend fun eventHandler(event: HomeEvent) {
        when (event) {
            is Load -> {
                val scope = CoroutineScope(Dispatchers.IO)

                model = model.copy(
                    statuses =
                    flow.cachedIn(scope)
                )
            }
            is PostMessage -> {
                val result = kotlin.runCatching {
                    api.newStatus(
                        authHeader = " Bearer ${oauthRepository.getCurrent()}",
                        content = event.content
                    )
                }
                if (result.isSuccess) {
                    withContext(Dispatchers.IO){
                        statusDao.insertAll(listOf(result.getOrThrow().toStatusDb()))
                    }
                }
            }
        }
    }
}


abstract class HomePresenter :
    Presenter<HomePresenter.HomeEvent, HomePresenter.HomeModel, HomePresenter.HomeEffect>(
        HomeModel(true)
    ) {
    sealed interface HomeEvent
    object Load : HomeEvent
    data class PostMessage(val content: String) : HomeEvent

    data class HomeModel(
        val loading: Boolean,
        val statuses: Flow<PagingData<StatusDB>>? = null
    )

    sealed interface HomeEffect
}