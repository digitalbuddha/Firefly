package com.androiddev.social.auth.data

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.shared.Api
import com.androiddev.social.shared.Token
import com.androiddev.social.timeline.data.NewOauthApplication
import com.squareup.anvil.annotations.ContributesBinding
import org.mobilenativefoundation.store.cache5.Cache
import org.mobilenativefoundation.store.cache5.CacheBuilder
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

interface AppTokenRepository {
    suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication
    suspend fun createAccessToken(accessTokenRequest: AccessTokenRequest): Token
    var appToken: String?
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class RealAppTokenRepository @Inject constructor(
    val api: Api
) : AppTokenRepository {
    private val cache: Cache<AppTokenRequest, NewOauthApplication> =
        CacheBuilder<AppTokenRequest, NewOauthApplication>()
            .maximumSize(100)
            .expireAfterWrite(1.hours)
            .build()

    override var appToken: String? = null

    override suspend fun getAppToken(appTokenRequest: AppTokenRequest): NewOauthApplication {
        return cache.getIfPresent(appTokenRequest) ?: api.createApplication(
            appTokenRequest.url,
            appTokenRequest.scopes,
            appTokenRequest.client_name,
            appTokenRequest.redirect_uris
        )
            .also {
                cache.put(appTokenRequest, it)
            }
    }

    override suspend fun createAccessToken(accessTokenRequest: AccessTokenRequest): Token {
        return api.createAccessToken(
            domain = "https://${accessTokenRequest.domain}/oauth/token",
            clientId = accessTokenRequest.clientId,
            clientSecret = accessTokenRequest.clientSecret,
            redirectUri = accessTokenRequest.redirectUri,
            grantType = accessTokenRequest.grantType,
            code = accessTokenRequest.code,
            scope = accessTokenRequest.scope
        ).also {
            appToken = it.accessToken
        }
    }


}

data class AppTokenRequest(
    val url: String,
    val scopes: String,
    val client_name: String,
    val redirect_uris: String
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
