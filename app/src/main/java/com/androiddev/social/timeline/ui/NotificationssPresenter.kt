package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Notification
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
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
    val api: UserApi,
    val repository: OauthRepository
) : NotificationPresenter() {

    val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: Unit ->
            val token = " Bearer ${repository.getCurrent()}"
            api.notifications(authHeader = token, offset = null)
        }
    ).build()

    override suspend fun eventHandler(event: NotificationEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val token = " Bearer ${repository.getCurrent()}"
                val notification =
                    kotlin.runCatching { store.get(Unit) }
                if (notification.isSuccess) {
                    val conversations: List<Notification> = notification.getOrThrow()
                    val statuses = conversations.filter {
                        it.status != null
                    }
                    model = model.copy(statuses = statuses)
                }
            }
        }
    }
}
