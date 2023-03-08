package com.androiddev.social.timeline.ui

import android.app.Application
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.auth.data.UserManager
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class AvatarPresenter :
    Presenter<AvatarPresenter.AvatarEvent, AvatarPresenter.AvatarModel, AvatarPresenter.AvatarEffect>(
        AvatarModel(true)
    ) {
    sealed interface AvatarEvent

    object Load : AvatarEvent

    data class AvatarModel(
        val loading: Boolean,
        val accounts: List<Account>? = null
    )

    sealed interface AvatarEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = AvatarPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealAvatarPresenter @Inject constructor(
    val api: UserApi,
    val application: Application,
    val oauthRepository: OauthRepository,
    val userManager: UserManager,
    val accessTokenRequest: AccessTokenRequest
) :
    AvatarPresenter() {

    override suspend fun eventHandler(event: AvatarEvent, coroutineScope: CoroutineScope) = withContext(
        Dispatchers.IO) {
        when (event) {
            is Load -> {
                val touch = oauthRepository.getCurrent()//touch it to make sure we save it
                val accountTokens: List<AccessTokenRequest> = application.baseContext.getAccounts()
                val accounts = accountTokens.map { accountTokenRequest ->
                    val account: Result<Account?> =
                        kotlin.runCatching {
                            val userComponent =
                                userManager.userComponentFor(accessTokenRequest = accountTokenRequest)
                            val credentials = userComponent.api()
                                .accountVerifyCredentials(
                                    " Bearer ${
                                        userComponent.oauthRepository().getCurrent()
                                    }"
                                )
                            credentials
                        }

                    account.getOrNull()
                        ?.copy(
                            domain = accountTokenRequest.domain
                        )
                }.filterNotNull()

                model =
                    model.copy(accounts = accounts.sortedBy { if (it.domain == accessTokenRequest.domain) 0 else 1 })

            }
        }
    }
}
