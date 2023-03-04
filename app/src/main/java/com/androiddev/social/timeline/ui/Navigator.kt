package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.EbonyApp
import com.androiddev.social.UserComponent
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.UserManagerProvider
import com.androiddev.social.timeline.data.dataStore
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Composable
fun getUserComponent(accessTokenRequest: AccessTokenRequest): UserComponent {
    val userManager =
        ((LocalContext.current.applicationContext as EbonyApp).component as UserManagerProvider).getUserManager()
    return userManager.userComponentFor(
        accessTokenRequest = accessTokenRequest
    )
}

@Composable
fun getUserComponent(code: String): UserComponent {
    val userManager =
        ((LocalContext.current.applicationContext as EbonyApp).component as UserManagerProvider).getUserManager()
    return userManager.userComponentFor(
        code = code
    )
}

@Composable
fun Navigator(
    navController: NavHostController,
    scope: CoroutineScope,
    onChangeTheme: () -> Unit
) {


    NavHost(navController = navController, startDestination = "splash") {
        navigation(
            startDestination = "timeline",
            route = "home/{server}/{clientId}/{clientSecret}/{redirectUri}/{code}"
        ) {
            composable("timeline") {
                val accessTokenRequest = accessTokenRequest(it)
                val userComponent = getUserComponent(accessTokenRequest = accessTokenRequest)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    TimelineScreen(
                        accessTokenRequest,
                        userComponent,
                        onChangeTheme,
                        onNewAccount = { navController.navigate("selectServer") },
                        onProfileSelected = { account ->
                            navController.navigate("login/${account.domain}")
                        },
                        goToMentions = {
                            navController.navigate("mentions/${it.arguments?.getString("code")}")
                        },
                        goToNotifications = {
                            navController.navigate("notifications/${it.arguments?.getString("code")}")
                        }
                    )
                }
            }
            composable("mentions/{code}") {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        MentionsScreen(navController)
                    }
                }
            }
            composable("notifications/{code}") {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        NotificationsScreen(navController)
                    }
                }
            }
        }
        composable("splash") {
            SplashScreen(navController)
        }


        composable("selectServer") {
            ServerSelectScreen { server ->
                scope.launch {
                    navController.navigate("login/$server")
                }
            }

        }



        composable("login/{server}") {
            val server = it.arguments?.getString("server")!!
            SignInScreen(navController, scope, server)
        }
    }
}


suspend fun Context.getAccounts(): List<AccessTokenRequest> {
    return buildList {
        val current = dataStore.data.first()
        current.servers.values.forEach {
            it.users.values.forEach {
                add(it.accessTokenRequest)
            }
        }
    }
}

@Composable
fun accessTokenRequest(it: NavBackStackEntry) = AccessTokenRequest(
    code = it.arguments?.getString("code")!!,
    clientId = it.arguments?.getString("clientId")!!,
    clientSecret = it.arguments?.getString("clientSecret")!!,
    redirectUri = it.arguments?.getString("redirectUri")!!,
    domain = it.arguments?.getString("server")!!
)

