package com.androiddev.social.auth.data

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.shared.Api
import com.androiddev.social.timeline.data.NewOauthApplication
import com.squareup.anvil.annotations.ContributesBinding
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.get
import javax.inject.Inject

interface AppTokenRepository {
    suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealAppTokenRepository @Inject constructor(
    val api: Api,
) : AppTokenRepository {


    private val appTokenStore = StoreBuilder.from(fetcher = Fetcher.of { key: AppTokenRequest ->
        fetchAppToken(key)
    }).build()




    override suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return appTokenStore.get(appTokenRequest)
    }

    suspend fun fetchAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return api.createApplication(
            appTokenRequest.url,
            appTokenRequest.scopes,
            appTokenRequest.client_name,
            appTokenRequest.redirect_uris
        )
    }



}

data class AppTokenRequest(
    val url: String, val scopes: String, val client_name: String, val redirect_uris: String
)

data class AccessTokenRequest(
    val domain: String?=null,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val code: String,
    val grantType: String = "authorization_code",
    val scope: String = "read write follow push"
)
