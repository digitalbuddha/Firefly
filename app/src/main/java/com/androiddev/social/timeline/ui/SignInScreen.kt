package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.androiddev.social.AuthOptionalComponent
import com.androiddev.social.FireflyApp
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
    val context = LocalContext.current
    val component = retain(key = server) { NoAuthComponent(context) } as AuthOptionalInjector
    val signInPresenter = component.signInPresenter()
    val accessToken = signInPresenter.model.accessTokenRequest

    LaunchedEffect(server) {
        signInPresenter.start()
    }
    LaunchedEffect(server, accessToken) {
        if (accessToken != null) {
            val domain = URLEncoder.encode(accessToken.domain, StandardCharsets.UTF_8.toString())
            val clientId =
                URLEncoder.encode(accessToken.clientId, StandardCharsets.UTF_8.toString())
            val clientSecret =
                URLEncoder.encode(accessToken.clientSecret, StandardCharsets.UTF_8.toString())
            val redirectUri =
                URLEncoder.encode(accessToken.redirectUri, StandardCharsets.UTF_8.toString())
            val code = URLEncoder.encode(accessToken.code, StandardCharsets.UTF_8.toString())
            navController.navigate("home/${domain}/${clientId}/${clientSecret}/${redirectUri}/${code}") {
                popUpTo(0)
            }
        } else {
            signInPresenter.handle(SignInPresenter.SignIn(server))
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
            signInPresenter.shouldCancelLoadingUrl(it, scope, server)
        }

    )
}

fun NoAuthComponent(context: Context) =
    ((context.applicationContext as FireflyApp).component as AuthOptionalComponent.ParentComponent).createAuthOptionalComponent()