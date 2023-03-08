package com.androiddev.social.timeline.ui

import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.AccountRepository
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class ProfilePresenter :
    Presenter<ProfilePresenter.ProfileEvent, ProfilePresenter.ProfileModel, ProfilePresenter.ProfileEffect>(
        ProfileModel(null)
    ) {
    sealed interface ProfileEvent

    data class Load(val accountId:String) : ProfileEvent

    data class ProfileModel(
        val account: Account?,
    )

    sealed interface ProfileEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = ProfilePresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealProfilePresenter @Inject constructor(
    val repository: OauthRepository,
    val accountRepository: AccountRepository
) :
    ProfilePresenter() {


    override suspend fun eventHandler(event: ProfileEvent, coroutineScope: CoroutineScope) =
        withContext(Dispatchers.IO) {
            when (event) {
                is Load -> {
                    withContext(Dispatchers.IO) {
                        val account = accountRepository.get(event.accountId)
                        model = model.copy(account = account)
                    }

                }
            }
        }
}