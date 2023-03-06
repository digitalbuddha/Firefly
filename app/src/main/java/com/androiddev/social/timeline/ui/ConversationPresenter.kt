package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.FeedStoreRequest
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.timeline.data.StatusRepository
import com.androiddev.social.timeline.data.mapStatus
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class ConversationPresenter :
    Presenter<ConversationPresenter.ConversationEvent, ConversationPresenter.ConversationModel, ConversationPresenter.ConversationEffect>(
        ConversationModel(emptyMap())
    ) {
    sealed interface ConversationEvent

    data class Load(val statusId: String, val type: FeedType) : ConversationEvent

    data class ConversationModel(
        val conversations: Map<String, ConvoUI> = emptyMap(),
    )

    sealed interface ConversationEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = ConversationPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealConversationPresenter @Inject constructor(
    val api: UserApi,
    val repository: OauthRepository,
    val statusRepository: StatusRepository
) :
    ConversationPresenter() {


    override suspend fun eventHandler(event: ConversationEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val token = " Bearer ${repository.getCurrent()}"
                var currentConvo = model.conversations.getOrDefault(event.statusId, ConvoUI())

                withContext(Dispatchers.IO) {
                    val status = kotlin.runCatching {
                        statusRepository.get(FeedStoreRequest(event.statusId, event.type))
                    }
                    if (status.isSuccess) currentConvo =
                        currentConvo.copy(status = status.getOrThrow().mapStatus())
                    val conversations = model.conversations.toMutableMap()
                    conversations.put(event.statusId, currentConvo)
                    model = model.copy(conversations = conversations)
                }


                withContext(Dispatchers.IO) {
                    val conversation = kotlin.runCatching {
                        api.conversation(
                            authHeader = "$token",
                            statusId = event.statusId
                        )
                    }
                    if (conversation.isSuccess) {
                        val statuses = conversation.getOrThrow()
                        currentConvo = currentConvo.copy(before = statuses.ancestors)
                        currentConvo = currentConvo.copy(after = statuses.descendants)

                        val conversations = model.conversations.toMutableMap()
                        conversations.put(event.statusId, currentConvo)
                        model = model.copy(conversations = conversations)
                    }
                }
            }
        }
    }
}

data class ConvoUI(
    val before: List<Status> = emptyList(),
    val after: List<Status> = emptyList(),
    val status: UI? = null
)
