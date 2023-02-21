package com.androiddev.social.timeline.ui

import androidx.paging.*
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

@ContributesBinding(AuthRequiredScope::class, boundType = HomePresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealHomePresenter @Inject constructor(
    val timelineSource: Provider<TimelineSource>,
) : HomePresenter() {

    override suspend fun eventHandler(event: HomeEvent) {
        when (event) {
            is Load -> {
                val scope = CoroutineScope(Dispatchers.IO)
                model = model.copy(statuses = Pager(PagingConfig(pageSize = 6)) {
                    timelineSource.get()
                }.flow.cachedIn(scope))
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

    data class HomeModel(
        val loading: Boolean,
        val statuses: Flow<PagingData<UI>>? = null
    )

    sealed interface HomeEffect
}

class TimelineSource @Inject constructor(
    val api: UserApi,
    private val oauthRepository: OauthRepository,

    ) : PagingSource<String, UI>() {


    override fun getRefreshKey(state: PagingState<String, UI>): String? {
        return null
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, UI> {
        return try {
            val token = oauthRepository.getCurrent()
            val before = params.key
            val timeline = api.getTimeline(" Bearer $token", since = before)
            val list = timeline.mapStatus()
            LoadResult.Page(
                data = list,
                prevKey = null,
                nextKey = if (list.isEmpty()) null else timeline.last().id
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}