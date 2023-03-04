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
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer

@Composable
fun SplashScreen(navController: NavHostController) {
    val scale = remember { Animatable(0f) }
    val current: Context = LocalContext.current

    LaunchedEffect(Unit) {
        val accounts: List<AccessTokenRequest> = current.getAccounts()
        val firstLoggedInAccount: String? = accounts.map { it.domain }.firstOrNull()
        if (firstLoggedInAccount == null) {
            navController.navigate("selectServer")
        } else {
            navController.navigate("login/$firstLoggedInAccount")
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .placeholder(
                visible = true,
                highlight = PlaceholderHighlight.shimmer(),
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        ) {

        }
    }
}