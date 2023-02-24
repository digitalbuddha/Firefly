package com.androiddev.social.auth.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val accessTokenRequest: AccessTokenRequest,
    val api: Api,
    private val dataStore: DataStore<Preferences>
) : OauthRepository {

    private val sourceOfTruth = SourceOfTruth.of<String, Token, String>(
        reader = {
            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey(USER_KEY_PREFIX + accessTokenRequest.domain)]
            }
        },
        writer = { _, token ->
            dataStore.edit {
                it[stringPreferencesKey(USER_KEY_PREFIX + accessTokenRequest.domain)] =
                    token.accessToken
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

    suspend fun fetcher(): Token =
        api.createAccessToken(
            domain = "https://${accessTokenRequest.domain}/oauth/token",
            clientId = accessTokenRequest.clientId,
            clientSecret = accessTokenRequest.clientSecret,
            redirectUri = accessTokenRequest.redirectUri,
            grantType = "authorization_code",
            code = accessTokenRequest.code,
            scope = "read write follow push"
        )
}

