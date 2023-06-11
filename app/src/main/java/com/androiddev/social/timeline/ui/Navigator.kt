@file:OptIn(ExperimentalPagerApi::class)

package com.androiddev.social.timeline.ui

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.AuthRequiredComponent
import com.androiddev.social.FireflyApp
import com.androiddev.social.UserComponent
import com.androiddev.social.accounts.AccountTab
import com.androiddev.social.auth.data.AccessTokenRequest
import com.androiddev.social.auth.data.UserManagerProvider
import com.androiddev.social.search.SearchPresenter
import com.androiddev.social.search.SearchScreen
import com.androiddev.social.timeline.data.Account
import com.androiddev.social.timeline.data.dataStore
import com.androiddev.social.timeline.ui.model.UI
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.pager.ExperimentalPagerApi
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun getUserComponent(accessTokenRequest: AccessTokenRequest): UserComponent {
    val userManager =
        ((LocalContext.current.applicationContext as FireflyApp).component as UserManagerProvider).getUserManager()
    return userManager.userComponentFor(
        accessTokenRequest = accessTokenRequest
    )
}

@Composable
fun getUserComponent(code: String): UserComponent {
    val userManager =
        ((LocalContext.current.applicationContext as FireflyApp).component as UserManagerProvider).getUserManager()
    return userManager.userComponentFor(
        code = code
    )
}

@Composable
fun AuthScoped(
    arguments: Bundle?,
    code: String?,
    content: @Composable (component: AuthRequiredInjector, code: String) -> Unit
) {
    val userComponent = getUserComponent(code = code!!)
    CompositionLocalProvider(LocalUserComponent provides userComponent) {
        val userComponent: UserComponent = LocalUserComponent.current

        val component = retain(
            key = userComponent.request().code ?: ""
        ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
        CompositionLocalProvider(LocalAuthComponent provides component) {
            content(component, code)
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun Navigator(
    navController: NavHostController,
    scope: CoroutineScope,
    onChangeTheme: () -> Unit,
) {

    AnimatedNavHost(
        navController = navController,
        startDestination = "splash",
        enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Start) },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.End) },
//        popEnterTransition = { defaultTiviPopEnterTransition() },
//        popExitTransition = { defaultTiviPopExitTransition() },
        modifier = Modifier,
    ) {
        navigation(
            startDestination = "timeline",
            route = "home/{server}/{clientId}/{clientSecret}/{redirectUri}/{code}"
        ) {
            composable("timeline", enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
                val accessTokenRequest = accessTokenRequest(it)
                val userComponent = getUserComponent(accessTokenRequest = accessTokenRequest)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {

                    TimelineScreen(
                        accessTokenRequest,
                        userComponent,
                        onChangeTheme,
                        onNewAccount = { navController.navigate("selectServer") },
                        onProfileClick = { accountId, isCurrent ->
                            if (isCurrent)
                                navController.navigate(
                                    "profile/${it.arguments?.getString("code")}/${accountId}"
                                )
                            else
                                navController.navigate("login/${it.arguments?.getString("server")}")
                        },
                        goToMentions = {
                            navController.navigate("mentions/${it.arguments?.getString("code")}")
                        },
                        goToNotifications = {
                            navController.navigate("notifications/${it.arguments?.getString("code")}")
                        }, goToSearch = {
                            navController.navigate("search/${it.arguments?.getString("code")}")
                        },
                        goToConversation = { status: UI ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                        },
                        goToProfile =
                        { accountId: String ->
                            navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                        },
                        goToTag = { tag: String ->
                            navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")
                        },
                    )
                }
            }
            composable(
                route = "mentions/{code}",
            ) {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {
                    val userComponent: UserComponent = LocalUserComponent.current

                    val component = retain(
                        key = userComponent.request().domain ?: ""
                    ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                    CompositionLocalProvider(LocalAuthComponent provides component) {
                        MentionsScreen(
                            navController,
                            goToConversation = { status ->
                                navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                            },
                            true,
                            goToProfile = { accountId: String ->
                                navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                            },
                            goToTag = { tag: String ->
                                navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")
                            },

                            )
                    }
                }
            }
            composable(
                route = "tag/{code}/{tag}",
            ) {
                val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
                AuthScoped(it.arguments, it.arguments?.getString("code")) { component, code ->
                    TagScreen(
                        navController,
                        it.arguments?.getString("tag")!!,
                        goToConversation = { status ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                        },
                        true,
                        goToProfile = { accountId: String ->
                            navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                        },

                        ) { tag: String ->
                        navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")
                    }
                }
            }
        }


        composable(
            route = "profile/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            AuthScoped(it.arguments, it.arguments?.getString("code")) { component, code ->
                ProfileScreen(
                    component,
                    navController,
                    code,
                    accountId = accountId,
                    {
                        navController.navigate("followers/${it.arguments?.getString("code")}/$accountId")
                    },
                    {
                        navController.navigate("following/${it.arguments?.getString("code")}/$accountId")
                    },
                )
            }

        }

//            composable(
//                route = "following/{code}/{accountId}",
//            ) {
//                AuthScoped(it.arguments, it.arguments?.getString("code")) { component, code ->
//                    ProfileScreen(
//                        component,
//                        navController,
//                        code,
//                        accountId = it.arguments?.getString("accountId")!!,
//                        goToFollowers = {
//
//                        }
//                    )
//                }
//
//            }
        composable(
            route = "following/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            AuthScoped(it.arguments, it.arguments?.getString("code")!!) { component, code ->
                val followerPresenter = component.followerPresenter()
                val scope = rememberCoroutineScope()

                LaunchedEffect(key1 = accountId) {
                    followerPresenter.start()
                }
                LaunchedEffect(key1 = accountId) {
                    followerPresenter.handle(FollowerPresenter.Load(accountId, true))
                }

                followerPresenter.model.accounts?.let {
                    FollowerScreen(it, navController = navController, code, "Following")
                }

            }

        }

        composable(
            route = "followers/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            AuthScoped(it.arguments, it.arguments?.getString("code")!!) { component, code ->
                val followerPresenter = component.followerPresenter()
                val scope = rememberCoroutineScope()

                LaunchedEffect(key1 = accountId) {
                    followerPresenter.start()
                }
                LaunchedEffect(key1 = accountId) {
                    followerPresenter.handle(
                        FollowerPresenter.Load(
                            accountId,
                            following = false
                        )
                    )
                }

                followerPresenter.model.accounts?.let {
                    FollowerScreen(it, navController = navController, code, "Followers")
                }
            }

        }

        composable(
            route = "search/{code}",
//                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),

        ) {
            AuthScoped(
                it.arguments,
                it.arguments?.getString("code")
            ) { component: AuthRequiredInjector, code ->
                val searchPresenter = component.searchPresenter()
                val scope = rememberCoroutineScope()
                LaunchedEffect(key1 = code) {
                    searchPresenter.start(scope)
                }
                val colorScheme = MaterialTheme.colorScheme
                LaunchedEffect(key1 = code) {
                    searchPresenter.handle(SearchPresenter.Init(colorScheme))
                }
                SearchScreen(
                    searchPresenter.model,
                    navController = navController,
                    onQueryChange = { searchTerm: String ->
                        searchPresenter.onQueryTextChange(searchTerm)
                    },
                    goToProfile = { accountId: String ->
                        navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                    },
                    goToTag = { tag: String ->
                        navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")
                    },
                    goToConversation = { status ->
                        navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type.type}")
                    },
                )
            }

        }

        composable(
//                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            route = "conversation/{code}/{statusId}/{type}",
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
                        navController = navController,
                        statusId = statusId,
                        type = type, goToProfile = { accountId ->
                            navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                        },
                        goToTag = { tag ->
                            navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")

                        }
                    )
                }
            }
        }
        composable(
//                dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
            route = "notifications/{code}",
        ) {
            val userComponent = getUserComponent(code = it.arguments?.getString("code")!!)
            CompositionLocalProvider(LocalUserComponent provides userComponent) {
                val userComponent: UserComponent = LocalUserComponent.current

                val component = retain(
                    key = userComponent.request().domain ?: ""
                ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
                CompositionLocalProvider(LocalAuthComponent provides component) {
                    NotificationsScreen(
                        navController,
                        { status: UI ->
                            navController.navigate("conversation/${it.arguments?.getString("code")}/${status.remoteId}/${status.type}") {
                                //                                popUpTo("timeline")
                            }
                        },
                        { accountId: String ->
                            navController.navigate("profile/${it.arguments?.getString("code")}/${accountId}")
                        },
                        goToTag = { tag ->
                            navController.navigate("tag/${it.arguments?.getString("code")}/${tag}")

                        }

                    )
                }
            }
        }
        composable(
            "splash",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }

        ) {
            SplashScreen(navController)
        }

        composable("selectServer",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            ServerSelectScreen { server ->
                scope.launch {
                    navController.navigate("login/$server")
                }
            }

        }

        composable("login/{server}",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }) {
            val server = it.arguments?.getString("server")!!
            SignInScreen(navController, scope, server)
        }
    }
}

@Composable
fun FollowerScreen(
    accounts: Flow<PagingData<Account>>,
    navController: NavHostController,
    code: String,
    s: String
) {
    Column {
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Followers",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                if (navController.previousBackStackEntry != null) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            tint = MaterialTheme.colorScheme.onSurface,
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "search"
                        )
                    }
                } else {
                    null
                }
            }
        )
        val pagingItems = accounts.collectAsLazyPagingItems()
        AccountTab(results = null, resultsPaging = pagingItems, goToProfile = { accountId: String ->
            navController.navigate("profile/$code/$accountId")
        })

    }


}


suspend fun Context.getAccounts(): List<AccessTokenRequest> = withContext(Dispatchers.IO) {
    buildList {
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

