package com.androiddev.social.auth.data

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.shared.Api
import com.androiddev.social.shared.Token
import com.androiddev.social.timeline.data.NewOauthApplication
import com.squareup.anvil.annotations.ContributesBinding
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

interface AppTokenRepository {
    suspend fun getAppToken(appTokenRequest: AppTokenRequest? = null): NewOauthApplication
    suspend fun getUserToken(accessTokenRequest: AccessTokenRequest?=null): Token
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealAppTokenRepository @Inject constructor(
    val api: Api
) : AppTokenRepository {

    private val lastRequest = AtomicReference<AppTokenRequest>()
    private val lastUserRequest = AtomicReference<AccessTokenRequest>()

    private val appTokenStore = StoreBuilder.from(fetcher = Fetcher.of { key: AppTokenRequest ->
        fetchAppToken(key)
    }).build()

    private val userTokenStore = StoreBuilder.from(fetcher = Fetcher.of { key: AccessTokenRequest ->
        fetchUserToken(key)
    }).build()

    override suspend fun getAppToken(appTokenRequest: AppTokenRequest?): NewOauthApplication {
        return appTokenStore.get(appTokenRequest ?: lastRequest.get())
    }

    override suspend fun getUserToken(accessTokenRequest: AccessTokenRequest?): Token {
        val userToken = userTokenStore.get(accessTokenRequest ?: lastUserRequest.get())
        return userToken
    }

    suspend fun fetchAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return api.createApplication(
            appTokenRequest.url,
            appTokenRequest.scopes,
            appTokenRequest.client_name,
            appTokenRequest.redirect_uris
        ).also {
            lastRequest.set(appTokenRequest)
        }
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
        ).also {
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
