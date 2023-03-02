package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

abstract class ConversationPresenter :
    Presenter<ConversationPresenter.ConversationEvent, ConversationPresenter.ConversationModel, ConversationPresenter.ConversationEffect>(
        ConversationModel(emptyMap(), emptyMap())
    ) {
    sealed interface ConversationEvent

    data class Load(val statusId: String) : ConversationEvent

    data class ConversationModel(
        val before: Map<String, List<Status>>,
        val after: Map<String, List<Status>>
    )

    sealed interface ConversationEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = ConversationPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealConversationPresenter @Inject constructor(
    val api: UserApi,
    val repository: OauthRepository
) :
    ConversationPresenter() {


    override suspend fun eventHandler(event: ConversationEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val token = " Bearer ${repository.getCurrent()}"
                val conversation = kotlin.runCatching { api.conversation(event.statusId) }
                if (conversation.isSuccess) {
                    val statuses = conversation.getOrThrow()
                    val before = model.before.toMutableMap()
                    before[event.statusId] = statuses.ancestors
                    model = model.copy(before = before)

                    val after = model.after.toMutableMap()
                    after[event.statusId] = statuses.descendants
                    model = model.copy(after = after)
                }
            }
        }
    }
}
