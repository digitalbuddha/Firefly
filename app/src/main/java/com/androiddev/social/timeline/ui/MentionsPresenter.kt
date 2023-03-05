package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.conversation.Conversation
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
import javax.inject.Inject

abstract class MentionsPresenter :
    Presenter<MentionsPresenter.MentionsEvent, MentionsPresenter.MentionsModel, MentionsPresenter.MentionsEffect>(
        MentionsModel(emptyList())
    ) {
    sealed interface MentionsEvent

    object Load : MentionsEvent

    data class MentionsModel(
        val statuses: List<Status>
    )

    sealed interface MentionsEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = MentionsPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealMentionsPresenter @Inject constructor(
    val api: UserApi,
    val repository: OauthRepository
) : MentionsPresenter() {

    val store = StoreBuilder.from(
        fetcher = Fetcher.of { key: Unit ->
            val token = " Bearer ${repository.getCurrent()}"
            api.conversations(authHeader = token)
        }
    ).build()

    override suspend fun eventHandler(event: MentionsEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val token = " Bearer ${repository.getCurrent()}"
                val mentions =
                    kotlin.runCatching { store.get(Unit) }
                if (mentions.isSuccess) {
                    val conversations: List<Conversation> = mentions.getOrThrow()
                    val statuses = conversations.map { it.lastStatus }
                    model = model.copy(statuses = statuses)
                }
            }
        }
    }
}
