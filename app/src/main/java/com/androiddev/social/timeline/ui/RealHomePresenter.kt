package com.androiddev.social.timeline.ui

import com.androiddev.social.AppScope
import com.androiddev.social.auth.data.AppTokenRepository
import com.androiddev.social.shared.Api
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(AppScope::class, boundType = HomePresenter::class)
class RealHomePresenter @Inject constructor(
    private val timelineApi: Api,
    private val appTokenRepository: AppTokenRepository
) : HomePresenter() {
    init {

    }

    override suspend fun eventHandler(event: HomeEvent) {
        when (event) {
            is LoadSomething -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val token = " Bearer ${appTokenRepository.appToken}"
                    val list = timelineApi.getTimeline(token)
                    model = model.copy(loading = false, statuses = list)
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
    object LoadSomething : HomeEvent

    data class HomeModel(
        val loading: Boolean,
        val statuses: List<Status>? = listOf()
    )

    sealed interface HomeEffect
}