package com.androiddev.social.timeline.data

import app.cash.molecule.AndroidUiDispatcher
import com.androiddev.social.AuthRequiredScope
import com.androiddev.social.SingleIn
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class ProfilePresenter :
    Presenter<ProfilePresenter.ProfileEvent, ProfilePresenter.ProfileModel, ProfilePresenter.ProfileEffect>(
        ProfileModel(null, null)
    ) {
    sealed interface ProfileEvent

    data class Load(val accountId: String) : ProfileEvent

    data class ProfileModel(
        /**
         * the current account that user is using to interact with the server.
         */
        val currentAccount: Account?,

        /**
         * The account that profile screen wants to display.
         */
        val account: Account?,
    )

    sealed interface ProfileEffect
}

@ContributesBinding(AuthRequiredScope::class, boundType = ProfilePresenter::class)
@SingleIn(AuthRequiredScope::class)
class RealProfilePresenter @Inject constructor(
    val accountRepository: AccountRepository
) :
    ProfilePresenter() {


    override suspend fun eventHandler(event: ProfileEvent, scope: CoroutineScope) =
        withContext(Dispatchers.IO) {
            when (event) {
                is Load -> {
                    model = model.copy(currentAccount = accountRepository.getCurrent())
                    accountRepository.subscribe(event.accountId)
                        .flowOn(AndroidUiDispatcher.Main)
                        .collect { account ->
                            model = model.copy(account = account)
                        }
                }
            }
        }
}