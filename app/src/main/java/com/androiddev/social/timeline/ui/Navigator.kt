package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.USER_KEY_PREFIX
import com.androiddev.social.auth.data.UserManagerProvider
import com.androiddev.social.timeline.data.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

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
            val current: Context = LocalContext.current
            var needToSelectServer by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val accounts: Map<Preferences.Key<*>, Any>? =
                    current.dataStore.data.map { preferences ->
                        preferences.asMap()
                    }.firstOrNull()

                val value: String? = accounts?.keys?.firstOrNull()?.name
                val loggedInAccount: String? = value?.removePrefix(USER_KEY_PREFIX)
                if (loggedInAccount == null) {
                    needToSelectServer = true
                } else {
                    navController.navigate("login/$loggedInAccount")
                }
            }
                ServerSelectScreen(scope, navController, current.dataStore, needToSelectServer)

        }
        composable("login/{server}") {
            val server = it.arguments?.getString("server")!!
            SignInScreen(navController, scope, server)
        }
    }
}