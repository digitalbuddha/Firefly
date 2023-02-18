package com.androiddev.social.auth.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.shared.Api
import com.androiddev.social.shared.Token
import com.androiddev.social.timeline.data.NewOauthApplication
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

interface AppTokenRepository {
    suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication
    suspend fun getUserToken(accessTokenRequest: AccessTokenRequest? = null): String?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealAppTokenRepository @Inject constructor(
    val api: Api,
    val dataStore: DataStore<Preferences>
) : AppTokenRepository {

    val userToken = stringPreferencesKey("USER_TOKEN")


    private val lastUserRequest = AtomicReference<AccessTokenRequest>()

    private val appTokenStore = StoreBuilder.from(fetcher = Fetcher.of { key: AppTokenRequest ->
        fetchAppToken(key)
    }).build()

    private val userTokenStore = StoreBuilder.from(fetcher = Fetcher.of { key: AccessTokenRequest ->
        fetchUserToken(key)
    }).build()

    override suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return appTokenStore.get(appTokenRequest)
    }

    override suspend fun getUserToken(accessTokenRequest: AccessTokenRequest?): String? {
         return accessTokenRequest?.let {
            userTokenStore.get(accessTokenRequest).accessToken
        }?: dataStore.data.map { preferences->
            preferences[userToken]
        }.firstOrNull()

    }

    suspend fun fetchAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return api.createApplication(
            appTokenRequest.url,
            appTokenRequest.scopes,
            appTokenRequest.client_name,
            appTokenRequest.redirect_uris
        )
    }

    suspend fun fetchUserToken(accessTokenRequest: AccessTokenRequest): Token {
        return api.createAccessToken(
            domain = "https://${accessTokenRequest.domain}/oauth/token",
            clientId = accessTokenRequest.clientId,
            clientSecret = accessTokenRequest.clientSecret,
            redirectUri = accessTokenRequest.redirectUri,
            grantType = accessTokenRequest.grantType,
            code = accessTokenRequest.code,
            scope = accessTokenRequest.scope
        ).also { token ->
            dataStore.edit {
                it[userToken] = token.accessToken
            }
            lastUserRequest.set(accessTokenRequest)
        }
    }


}

data class AppTokenRequest(
    val url: String, val scopes: String, val client_name: String, val redirect_uris: String
)

data class AccessTokenRequest(
    val domain: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val code: String,
    val grantType: String = "authorization_code",
    val scope: String = "read write follow push"
)
