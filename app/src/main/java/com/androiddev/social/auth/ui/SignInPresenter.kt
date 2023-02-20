/*
 * This file is part of Dodo.
 *
 * Dodo is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Dodo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Dodo.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.androiddev.social.auth.ui

import com.androiddev.social.AppScope
import com.androiddev.social.SingleIn
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.AppTokenRepository
import com.androiddev.social.auth.data.AppTokenRequest
import com.androiddev.social.auth.data.ApplicationBody
import com.androiddev.social.timeline.data.NewOauthApplication
import com.androiddev.social.ui.util.Presenter
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.*
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
        val server: String = "",
        val oauthAuthorizeUrl: String = "",
        val redirectUri: String = "",
        val error: String? = null,
        val signedIn: Boolean = false,
        val clientId: String = "",
        val clientSecret: String = ""

    )

    sealed interface SignInEffect

    abstract fun shouldCancelLoadingUrl(url: String, scope: CoroutineScope): Boolean
}

@ContributesBinding(AppScope::class, boundType = SignInPresenter::class)
@SingleIn(AppScope::class)
class RealSignInPresenter @Inject constructor(
    val appTokenRepository: AppTokenRepository,
) : SignInPresenter() {

    init {


    }

    override suspend fun eventHandler(event: SignInEvent) {
        when (event) {
            is SetServer -> {
                val params = ApplicationBody()
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
                    model = model.copy(
                        redirectUri = value.redirectUri,
                        oauthAuthorizeUrl = createOAuthAuthorizeUrl(value, params.baseUrl),
                        server = params.baseUrl,
                        clientId = value.clientId,
                        clientSecret = value.clientSecret
                    )
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
                    val token = appTokenRepository.getUserToken(
                        AccessTokenRequest(
                            code = code,
                            domain = model.server,
                            clientId = model.clientId,
                            clientSecret = model.clientSecret,
                            redirectUri = model.redirectUri,
                        )

                    )
                    if (token != null) {
                        model = model.copy(signedIn = true)
                    } else {
                        displayErrorWithDuration("An error occurred.")
                    }
                }
                true
            }

            else -> false
        }
    }
}

fun String.encode(): String = URLEncoder.encode(this, "UTF-8")
