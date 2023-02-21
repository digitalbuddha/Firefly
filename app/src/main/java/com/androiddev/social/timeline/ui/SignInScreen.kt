package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.androiddev.social.AuthOptionalComponent
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.ui.SignInContent
import com.androiddev.social.auth.ui.SignInPresenter
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.CoroutineScope
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SignInScreen(
    navController: NavHostController,
    scope: CoroutineScope,
    server: String
) {
    val current = LocalContext.current
    val component = retain { NoAuthComponent(current) } as AuthOptionalInjector
    val signInPresenter = component.signInPresenter()
    val accessToken = signInPresenter.model.accessTokenRequest?.copy(domain = server)

    if (accessToken != null)
        LaunchedEffect("signIn") {
            val domain = URLEncoder.encode(accessToken.domain, StandardCharsets.UTF_8.toString())
            val clientId =
                URLEncoder.encode(accessToken.clientId, StandardCharsets.UTF_8.toString())
            val clientSecret =
                URLEncoder.encode(accessToken.clientSecret, StandardCharsets.UTF_8.toString())
            val redirectUri =
                URLEncoder.encode(accessToken.redirectUri, StandardCharsets.UTF_8.toString())
            val code = URLEncoder.encode(accessToken.code, StandardCharsets.UTF_8.toString())
            navController.navigate("timeline/${domain}/${clientId}/${clientSecret}/${redirectUri}/${code}") {
                popUpTo(0)
            }
        }
    LaunchedEffect("signIn") {
        signInPresenter.start()
    }
    LaunchedEffect("signIn") {
        if (accessToken != null) {
            val domain = URLEncoder.encode(accessToken.domain, StandardCharsets.UTF_8.toString())
            val clientId =
                URLEncoder.encode(accessToken.clientId, StandardCharsets.UTF_8.toString())
            val clientSecret =
                URLEncoder.encode(accessToken.clientSecret, StandardCharsets.UTF_8.toString())
            val redirectUri =
                URLEncoder.encode(accessToken.redirectUri, StandardCharsets.UTF_8.toString())
            val code = URLEncoder.encode(accessToken.code, StandardCharsets.UTF_8.toString())
            navController.navigate("timeline/${domain}/${clientId}/${clientSecret}/${redirectUri}/${code}") {
                popUpTo(0)
            }
        } else {
            signInPresenter.handle(SignInPresenter.SetServer(server))
        }
    }
    SignInContent(
        oauthAuthorizeUrl = signInPresenter.model.oauthAuthorizeUrl,
        error = signInPresenter.model.error,
        onErrorFromOAuth = {
            //                                TODO("Implement onError")
        },
        onCloseClicked = {

        },
        shouldCancelLoadingUrl = {
            signInPresenter.shouldCancelLoadingUrl(it, scope)
        }

    )
}

fun NoAuthComponent(context: Context) =
    ((context.applicationContext as EbonyApp).component as AuthOptionalComponent.ParentComponent).createAuthOptionalComponent()