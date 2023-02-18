package com.androiddev.social.timeline.ui

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.AppTokenRepository
import com.androiddev.social.shared.Api
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
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
@ContributesBinding(AppScope::class, boundType = AvatarPresenter::class)
@SingleIn(AppScope::class)
class RealAvatarPresenter @Inject constructor(val api: Api, val repository: AppTokenRepository) :
    AvatarPresenter() {

    override suspend fun eventHandler(event: AvatarEvent) {
        when (event) {
            Load -> {
                val token = " Bearer ${repository.getUserToken()}"
                val account = api.accountVerifyCredentials(token)
                model = model.copy(account = account)
            }
        }
    }
}
