package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.FeedType
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import javax.inject.Inject

abstract class UriPresenter :
    Presenter<UriPresenter.UrlEvent, UriPresenter.UriModel, UriPresenter.UriEffect>(
        UriModel()
    ) {
    sealed interface UrlEvent

    data class Open(val uri: URI, val type: FeedType) : UrlEvent
    object Reset : UrlEvent

    data class UriModel(
        val handledUri: HandledUri = NotHandled,
    )

    sealed interface HandledUri

    object NotHandled : HandledUri

    data class UnknownHandledUri(
        val uri: URI,
    ) : HandledUri

    data class ConversationHandledUri(
        val statusId: String,
        val type: FeedType,
    ) : HandledUri

    data class ProfileHandledUri(
        val accountId: String,
    ) : HandledUri

    sealed interface UriEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = UriPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealUriPresenter @Inject constructor(
    private val userApi: UserApi,
    private val oauthRepository: OauthRepository,
) : UriPresenter() {

    override suspend fun eventHandler(event: UrlEvent, scope: CoroutineScope) =
        withContext(Dispatchers.IO) {
            when (event) {
                is Open -> {
                    val searchResult = kotlin.runCatching {
                        userApi.search(
                            authHeader = oauthRepository.getAuthHeader(),
                            searchTerm = event.uri.toASCIIString(),
                            limit = 1.toString(),
                            resolve = true,
                            following = false,
                        )
                    }

                    when {
                        searchResult.isSuccess -> {
                            val result = searchResult.getOrThrow()
                            when {
                                result.statuses.isNotEmpty() -> {
                                    model = model.copy(
                                        handledUri = ConversationHandledUri(
                                            statusId = result.statuses.first().id,
                                            type = event.type
                                        )
                                    )
                                    return@withContext
                                }
                                result.accounts.isNotEmpty() -> {
                                    model = model.copy(
                                        handledUri = ProfileHandledUri(
                                            accountId = result.accounts.first().id,
                                        )
                                    )
                                    return@withContext
                                }
                            }
                        }
                    }
                    model = model.copy(handledUri = UnknownHandledUri(uri = event.uri))
                }

                Reset -> {
                    model = model.copy(handledUri = NotHandled)
                }
            }
        }

}
