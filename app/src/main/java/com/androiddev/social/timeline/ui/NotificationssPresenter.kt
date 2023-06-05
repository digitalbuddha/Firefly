package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Notification
import com.androiddev.social.timeline.data.StatusDao
import com.androiddev.social.timeline.data.toStatusDb
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreRequest
import javax.inject.Inject

abstract class NotificationPresenter :
    Presenter<NotificationPresenter.NotificationEvent, NotificationPresenter.NotificationModel, NotificationPresenter.NotificationEffect>(
        NotificationModel(emptyList())
    ) {
    sealed interface NotificationEvent

    object Load : NotificationEvent

    data class NotificationModel(
        val statuses: List<Notification>
    )

    sealed interface NotificationEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = NotificationPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealNotificationPresenter @Inject constructor(
    val repository: NotificationsRepository
) : NotificationPresenter() {


    override suspend fun eventHandler(event: NotificationEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                coroutineScope.launch(Dispatchers.IO) {
                    repository.get().collectLatest {
                        val statuses = it.filter {
                            it.status != null
                        }
                        model = model.copy(statuses = statuses)
                    }
                }
            }
        }
    }
}

interface NotificationsRepository {
    suspend fun get(): Flow<List<Notification>>
}

@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealNotificationsRepository @Inject constructor(
    userApi: UserApi,
    statusDao: StatusDao,
    oauthRepository: OauthRepository
) :
    NotificationsRepository {
    val store = StoreBuilder.from(
        Fetcher.of { key: Unit ->
            val notification = userApi.notifications(
                authHeader = " Bearer ${oauthRepository.getCurrent()}",
                offset = null
            )
            notification.forEach { notif ->
                notif.status?.let {status ->
                    statusDao.updateStatus(status.toStatusDb())
                }
            }
            notification
        }
    ).build()

    override suspend fun get(): Flow<List<Notification>> = withContext(Dispatchers.IO) {
        store.stream(StoreRequest.cached(Unit, refresh = true)).map { it.dataOrNull() }
            .filterNotNull()
    }
}