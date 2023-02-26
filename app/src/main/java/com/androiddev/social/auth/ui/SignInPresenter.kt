
package com.androiddev.social.auth.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.*
import com.androiddev.social.shared.Api
import com.androiddev.social.timeline.data.NewOauthApplication
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.net.URI
import java.net.URLEncoder
import javax.inject.Inject


abstract class SignInPresenter :
    Presenter<SignInPresenter.SignInEvent, SignInPresenter.SignInModel, SignInPresenter.SignInEffect>(
        SignInModel(false)
    ) {
    sealed interface SignInEvent
    data class SetServer(val domain: String) : SignInEvent


    data class SignInModel(
        val loading: Boolean,
        val oauthAuthorizeUrl: String = "",
        val redirectUri: String = "",
        val error: String? = null,
        val signedIn: Boolean = false,
        val clientId: String = "",
        val clientSecret: String = "",
        val accessTokenRequest: AccessTokenRequest? = null

    )

    sealed interface SignInEffect

    abstract fun shouldCancelLoadingUrl(url: String, scope: CoroutineScope): Boolean
}

@ContributesBinding(AppScope::class, boundType = SignInPresenter::class)
@SingleIn(AppScope::class)
class RealSignInPresenter @Inject constructor(
    val appTokenRepository: AppTokenRepository,
    val api: Api,
    private val dataStore: DataStore<Preferences>
) : SignInPresenter() {

    init {


    }

    override suspend fun eventHandler(event: SignInEvent, scope: CoroutineScope) {
        when (event) {
            is SetServer -> {
                val params = ApplicationBody(baseUrl = event.domain)
                val result: Result<NewOauthApplication> = kotlin.runCatching {
                    appTokenRepository.getAppToken(
                        AppTokenRequest(
                            "https://${event.domain}/api/v1/apps",
                            params.scopes,
                            params.clientName,
                            params.redirectUris()
                        )
                    )
                }
                if (result.isSuccess) {
                    val value = result.getOrThrow()
                    val userKey = dataStore.data.map { preferences ->
                        preferences[stringPreferencesKey(USER_KEY_PREFIX + event.domain)]
                    }.first()
                    if (userKey != null)
                       model= model.copy(
                        accessTokenRequest = AccessTokenRequest(
                            //since we already have a token in datastore we don't need to get a new code
                            code = "STUBBEDCODE",
                            clientId = value.clientId,
                            clientSecret = value.clientSecret,
                            redirectUri = value.redirectUri,
                            domain = event.domain
                        )
                    )
                    else {
                        model = model.copy(
                            redirectUri = value.redirectUri,
                            oauthAuthorizeUrl = createOAuthAuthorizeUrl(value, params.baseUrl),
                            clientId = value.clientId,
                            clientSecret = value.clientSecret
                        )
                    }

                } else {
                    result.exceptionOrNull()
                }
            }
        }
    }

    private fun createOAuthAuthorizeUrl(token: NewOauthApplication, server: String): String {
        val b = StringBuilder().apply {
            append("https://${server}")
            append("/oauth/authorize?client_id=${token.clientId}")
            append("&scope=${"read write follow push".encode()}")
            append("&redirect_uri=${token.redirectUri.encode()}")
            append("&response_type=code")
        }
        return b.toString()
    }

    private fun displayErrorWithDuration(error: String) {
        model = model.copy(error = error)
    }


    override fun shouldCancelLoadingUrl(url: String, scope: CoroutineScope): Boolean {
        model = model.copy(oauthAuthorizeUrl = "")
        val uri = URI(url)
        val query = uri.query

        if (!url.contains(model.redirectUri) || query.isNullOrEmpty()) {
            return false
        }

        return when {
            query.contains("error=") -> {
                val error = query.replace("error=", "")
                displayErrorWithDuration(error)
                true
            }

            query.contains("code=") -> {
                val code = query.replace("code=", "")
                scope.launch {
                    val accessTokenRequest = AccessTokenRequest(
                        code = code,
                        clientId = model.clientId,
                        clientSecret = model.clientSecret,
                        redirectUri = model.redirectUri,
                    )
                    model = model.copy(accessTokenRequest = accessTokenRequest)
                }
                true
            }

            else -> false
        }
    }
}

fun String.encode(): String = URLEncoder.encode(this, "UTF-8")
