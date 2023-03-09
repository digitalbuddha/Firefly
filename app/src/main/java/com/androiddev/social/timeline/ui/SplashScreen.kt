package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.androiddev.social.auth.data.AccessTokenRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SplashScreen(navController: NavHostController) {
    remember { Animatable(0f) }
    val current: Context = LocalContext.current

    LaunchedEffect(Unit) {
        val accounts: List<AccessTokenRequest> = current.getAccounts()
        val firstAccount = accounts.firstOrNull()
        if (firstAccount == null) {
            navController.navigate("selectServer")
        } else {
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        ) {

        }
    }
}