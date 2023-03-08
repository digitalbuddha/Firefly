package com.androiddev.social.auth.ui

import androidx.datastore.core.DataStore
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.*
import com.androiddev.social.shared.Api
import com.androiddev.social.timeline.data.NewOauthApplication
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.net.URI
import java.net.URLEncoder
import javax.inject.Inject


abstract class SignInPresenter :
    Presenter<SignInPresenter.SignInEvent, SignInPresenter.SignInModel, SignInPresenter.SignInEffect>(
        SignInModel(false)
    ) {
    sealed interface SignInEvent
    data class SignIn(val domain: String) : SignInEvent


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

    abstract fun shouldCancelLoadingUrl(url: String, scope: CoroutineScope, server: String): Boolean
}

@ContributesBinding(AuthOptionalScope::class, boundType = SignInPresenter::class)
@SingleIn(AuthOptionalScope::class)
class RealSignInPresenter @Inject constructor(
    val appTokenRepository: AppTokenRepository,
    val api: Api,
    private val dataStore: DataStore<LoggedInAccounts>
) : SignInPresenter() {

    init {


    }

    override suspend fun eventHandler(event: SignInEvent, scope: CoroutineScope): Unit =
        withContext(Dispatchers.IO) {
            when (event) {
                is SignIn -> {
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
                        val value: NewOauthApplication = result.getOrThrow()
                        val request: AccessTokenRequest? = dataStore.data.map {
                            val server = it.servers[event.domain]
                            val isAUserLoggedIn =
                                server?.users?.values?.firstOrNull { it.accessToken != null }
                            isAUserLoggedIn?.accessTokenRequest
                        }.firstOrNull()
                        if (request != null) {
                            model = model.copy(accessTokenRequest = request)
                        } else {
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


    override fun shouldCancelLoadingUrl(
        url: String,
        scope: CoroutineScope,
        server: String
    ): Boolean {
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
                        domain = server
                    )
                    saveNewAccessTokenRequest(accessTokenRequest)
                }
                true
            }

            else -> false
        }
    }

    suspend fun saveNewAccessTokenRequest(accessTokenRequest: AccessTokenRequest) {
        val current: LoggedInAccounts = dataStore.data.first()
        val servers = current.servers.toMutableMap()
        val server: Server = servers.getOrDefault(
            accessTokenRequest.domain,
            defaultValue = Server(domain = accessTokenRequest.domain!!)
        )
        val users = server.users.toMutableMap()
        users.put(
            accessTokenRequest.code,
            User(accessToken = null, accessTokenRequest = accessTokenRequest)
        )
        server.copy(users = users)


        servers[accessTokenRequest.domain] = server
        dataStore.updateData { it.copy(servers = servers) }
        model = model.copy(accessTokenRequest = accessTokenRequest)
    }
}

fun String.encode(): String = URLEncoder.encode(this, "UTF-8")
