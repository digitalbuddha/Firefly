package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.UserManagerProvider
import com.androiddev.social.timeline.data.dataStore
import kotlinx.coroutines.CoroutineScope

@Composable
fun Navigator(
    navController: NavHostController,
    scope: CoroutineScope
) {
    NavHost(navController = navController, startDestination = "selectServer") {
        composable("timeline/{server}/{clientId}/{clientSecret}/{redirectUri}/{code}") {
            val accessTokenRequest = AccessTokenRequest(
                code = it.arguments?.getString("code")!!,
                clientId = it.arguments?.getString("clientId")!!,
                clientSecret = it.arguments?.getString("clientSecret")!!,
                redirectUri = it.arguments?.getString("redirectUri")!!,
                domain = it.arguments?.getString("server")!!
            )

            val userManager =
                ((LocalContext.current.applicationContext as EbonyApp).component as UserManagerProvider).getUserManager()
            TimelineScreen(userManager.userComponentFor(accessTokenRequest = accessTokenRequest))
        }

        dialog("selectServer") {
//            val dataStore = ((LocalContext.current.applicationContext as EbonyApp).dat

            val current: Context = LocalContext.current
            ServerSelectScreen(scope, navController, current.dataStore)
        }
        composable("login/{server}") {
            val server = it.arguments?.getString("server")!!
            SignInScreen(navController, scope, server)
        }
    }
}