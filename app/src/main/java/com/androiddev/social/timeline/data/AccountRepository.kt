package com.androiddev.social.timeline.data

import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.auth.data.OauthRepository
import com.androiddev.social.shared.UserApi
import com.squareup.anvil.annotations.ContributesBinding
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
import javax.inject.Inject

interface AccountRepository {
    suspend fun get(accountId: String): Account
}

@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealAccountRepository @Inject constructor(
    api: UserApi,
    oauthRepository: OauthRepository,
) : AccountRepository {
    private val fetcher = Fetcher.of { accountId: String ->
        val token = " Bearer ${oauthRepository.getCurrent()}"
        val account = api.account(authHeader = token, accountId = accountId)
        val relationships = api.relationships(token, listOf(account.id))
        account.copy(isFollowed = relationships.firstOrNull()?.following==true)
    }

    private val store = StoreBuilder.from(
        fetcher = fetcher,
    ).build()

    override suspend fun get(accountId: String): Account = store.get(accountId)


}