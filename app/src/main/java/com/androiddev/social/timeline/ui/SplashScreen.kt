package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.androiddev.social.auth.data.AccessTokenRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SplashScreen(
    navController: NavHostController,
    accessTokenRequestState: MutableState<AccessTokenRequest?>,
) {
    val current: Context = LocalContext.current

    LaunchedEffect(Unit) {
        val accounts: List<AccessTokenRequest> = current.getAccounts()
        val firstAccount = accounts.firstOrNull()
        if (firstAccount == null) {
            navController.navigate("selectServer")
        } else {
            accessTokenRequestState.value = firstAccount

            val domain = URLEncoder.encode(firstAccount.domain, StandardCharsets.UTF_8.toString())
            val clientId =
                URLEncoder.encode(firstAccount.clientId, StandardCharsets.UTF_8.toString())
            val clientSecret =
                URLEncoder.encode(firstAccount.clientSecret, StandardCharsets.UTF_8.toString())
            val redirectUri =
                URLEncoder.encode(firstAccount.redirectUri, StandardCharsets.UTF_8.toString())
            val code = URLEncoder.encode(firstAccount.code, StandardCharsets.UTF_8.toString())

            navController.navigate("home/${domain}/${clientId}/${clientSecret}/${redirectUri}/${code}") {
                popUpTo(0)
            }
        }
    }
}