package com.androiddev.social.timeline.ui

import android.app.Application
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.auth.data.USER_KEY_PREFIX
import com.androiddev.social.auth.data.UserManager
import com.androiddev.social.shared.UserApi
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
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
    val userManager: UserManager
) :
    AvatarPresenter() {

    override suspend fun eventHandler(event: AvatarEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val touch = oauthRepository.getCurrent()//touch it to make sure we save it
                val accountTokens = application.baseContext.getAccounts()
                val accounts = accountTokens?.entries?.map { current ->
                    val token = " Bearer ${current.value}"
                    val domain = current
                        .key
                        .name
                        .removePrefix(USER_KEY_PREFIX)
                    val account: Result<Account?> =
                        kotlin.runCatching {
                            userManager.userComponentFor(domain = domain)?.api()
                                ?.accountVerifyCredentials(token)
                        }

                    account.getOrNull()
                        ?.copy(
                            domain = domain
                        )
                }?.filterNotNull()

                model = model.copy(accounts = accounts)

            }
        }
    }
}
