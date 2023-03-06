package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.EbonyApp
import com.androiddev.social.UserComponent
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.UserManagerProvider
import com.androiddev.social.timeline.data.dataStore
import com.androiddev.social.timeline.ui.model.UI
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun Navigator(
    navController: NavHostController,
    scope: CoroutineScope,
    sheetState: ModalBottomSheetState,
    onChangeTheme: () -> Unit
) {


    AnimatedNavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Start) },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.End) },
//        popEnterTransition = { defaultTiviPopEnterTransition() },
//        popExitTransition = { defaultTiviPopExitTransition() },
        modifier = Modifier,
    ){
    navigation(
            startDestination = "timeline",
            route = "home/{server}/{clientId}/{clientSecret}/{redirectUri}/{code}"
        ) {
            composable("timeline", enterTransition = {  fadeIn()}, exitTransition = { fadeOut() }) {
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
                        },
                        goToConversation = { status: UI ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                        }
                    )
                }
            }
            bottomSheet(
                "mentions/{code}",
//                dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        MentionsScreen(navController, goToConversation = { status ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                        })
                    }
                }
            }
        bottomSheet(
                "conversation/{code}/{statusId}/{type}",
            ) {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                val statusId = it.arguments?.getString("statusId")!!
                val type = it.arguments?.getString("type")!!
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        ConversationScreen(
                            navController, statusId, type
                        )
                    }
                }
            }
        bottomSheet(
                "notifications/{code}",
            ) {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        NotificationsScreen(navController) { status: UI ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type}")
                        }
                    }
                }
            }
        }
        composable("splash",  enterTransition = {  fadeIn()}, exitTransition = {slideOutOfContainer(AnimatedContentScope.SlideDirection.End) }) {
            SplashScreen(navController)
        }

        bottomSheet("selectServer") {
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

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviEnterTransition(): EnterTransition {
//    val initialNavGraph = initial.destination.hostNavGraph
//    val targetNavGraph = target.destination.hostNavGraph
//    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
//    if (initialNavGraph.id != targetNavGraph.id) {
//        return fadeIn()
//    }
//    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviExitTransition(): ExitTransition {
//    val initialNavGraph = initial.destination.hostNavGraph
//    val targetNavGraph = target.destination.hostNavGraph
//    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
//    if (initialNavGraph.id != targetNavGraph.id) {
//        return fadeOut()
//    }
//    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.Start)
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.End)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.End)
}

