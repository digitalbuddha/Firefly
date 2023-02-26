package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
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
        val account: Account? = null
    )

    sealed interface AvatarEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = AvatarPresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealAvatarPresenter @Inject constructor(val api: UserApi, val repository: OauthRepository) :
    AvatarPresenter() {

    override suspend fun eventHandler(event: AvatarEvent, coroutineScope: CoroutineScope) {
        when (event) {
            is Load -> {
                val token = " Bearer ${repository.getCurrent()}"
                val account: Result<Account> = kotlin.runCatching { api.accountVerifyCredentials(token) }
                if (account.isSuccess) model = model.copy(account = account.getOrThrow())
            }
        }
    }
}
