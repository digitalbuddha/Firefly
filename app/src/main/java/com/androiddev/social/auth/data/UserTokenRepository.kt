package com.androiddev.social.auth.data

import androidx.datastore.core.DataStore
import com.androiddev.social.SingleIn
import com.androiddev.social.UserScope
import com.androiddev.social.shared.Api
import com.androiddev.social.shared.Token
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.*
import javax.inject.Inject

interface OauthRepository {
    suspend fun getCurrent(): String?
}

val USER_KEY_PREFIX = "USER_TOKEN_FOR_"

@ContributesBinding(UserScope::class)
@SingleIn(UserScope::class)
class RealOauthRepository @Inject constructor(
    val accessTokenRequest: AccessTokenRequest,
    val api: Api,
    private val dataStore: DataStore<LoggedInAccounts>
) : OauthRepository {

    private val sourceOfTruth = SourceOfTruth.of<String, Token, String>(
        reader = {
            dataStore.data.map {
                val server = it.servers[accessTokenRequest.domain]
                val currentUser =
                    server?.users?.get(accessTokenRequest.code)
                currentUser?.accessToken
            }
        },
        writer = { _, token ->
            dataStore.updateData {
                val server = it.servers[accessTokenRequest.domain]!!
                val users = server.users.toMutableMap()
                val user = users.getOrDefault(
                    accessTokenRequest.code,
                    User(accessToken = token.accessToken, accessTokenRequest = accessTokenRequest)
                )


                users[accessTokenRequest.code] = user.copy(accessToken = token.accessToken)

                val serverResult = server.copy(users = users)
                val servers = it.servers.toMutableMap()
                servers[accessTokenRequest.domain!!] = serverResult

                it.copy(servers = servers)
            }
        }
    )

    private val userTokenStore: Store<String, String> =
        StoreBuilder.from(
            fetcher = Fetcher.of { key: String -> fetcher() },
            sourceOfTruth = sourceOfTruth
        ).build()

    override suspend fun getCurrent(): String {
        return userTokenStore.get(accessTokenRequest.domain!!)
    }

    suspend fun fetcher(): Token {
        return api.createAccessToken(
            domain = "https://${accessTokenRequest.domain}/oauth/token",
            clientId = accessTokenRequest.clientId,
            clientSecret = accessTokenRequest.clientSecret,
            redirectUri = accessTokenRequest.redirectUri,
            grantType = "authorization_code",
            code = accessTokenRequest.code,
            scope = "read write follow push"
        )
    }
}

