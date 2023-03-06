package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.conversation.Conversation
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Status
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreRequest
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
    val mentionRepository: MentionRepository,
) : MentionsPresenter() {
    override suspend fun eventHandler(event: MentionsEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                coroutineScope.launch(Dispatchers.IO) {
                    mentionRepository.get().collectLatest {
                        val statuses = it.map { it.lastStatus }
                        model = model.copy(statuses = statuses)
                    }
                }
            }
        }
    }
}


interface MentionRepository {
    suspend fun get(): Flow<List<Conversation>>
}

@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealMentionRepository @Inject constructor(
    userApi: UserApi,
    oauthRepository: OauthRepository
) :
    MentionRepository {
    val store = StoreBuilder.from(
        Fetcher.of { key: Unit ->
            userApi.conversations(
                authHeader = " Bearer ${oauthRepository.getCurrent()}",
            )
        }
    ).build()

    override suspend fun get(): Flow<List<Conversation>> =
        store.stream(StoreRequest.cached(Unit, refresh = true)).map { it.dataOrNull() }
            .filterNotNull()
}