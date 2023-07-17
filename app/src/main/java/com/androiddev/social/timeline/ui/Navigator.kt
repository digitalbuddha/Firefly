package com.androiddev.social.timeline.ui

import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
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
    return userManager.userComponentFor(accessTokenRequest = accessTokenRequest)
}

@Composable
fun getUserComponent(
    code: String,
    navBackStackEntry: NavBackStackEntry,
): UserComponent {
    val userManager =
        ((LocalContext.current.applicationContext as FireflyApp).component as UserManagerProvider).getUserManager()
    return userManager.userComponentFor(
        code = code
    ) ?: run {
        getUserComponent(accessTokenRequest = accessTokenRequest(navBackStackEntry))
    }
}

@Composable
fun AuthScoped(
    code: String,
    navBackStackEntry: NavBackStackEntry,
    content: @Composable (userComponent: UserComponent, component: AuthRequiredInjector) -> Unit
) {
    val userComponent = getUserComponent(code = code, navBackStackEntry = navBackStackEntry)
    CompositionLocalProvider(LocalUserComponent provides userComponent) {
        val component = retain(
            key = userComponent.request().code
        ) { (userComponent as AuthRequiredComponent.ParentComponent).createAuthRequiredComponent() } as AuthRequiredInjector
        CompositionLocalProvider(LocalAuthComponent provides component) {
            content(userComponent, component)
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class
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
        modifier = Modifier,
    ) {
        navigation(
            startDestination = "timeline",
            route = "home/{server}/{clientId}/{clientSecret}/{redirectUri}/{code}"
        ) {
            composable("timeline", enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
                val accessTokenRequest = accessTokenRequest(it)
                val code = accessTokenRequest.code
                val userComponent = getUserComponent(accessTokenRequest = accessTokenRequest)
                CompositionLocalProvider(LocalUserComponent provides userComponent) {

                    TimelineScreen(
                        navController = navController,
                        accessTokenRequest = accessTokenRequest,
                        userComponent,
                        onChangeTheme,
                        onNewAccount = { navController.navigate("selectServer") },
                        onProfileClick = { accountId, isCurrent ->
                            if (isCurrent)
                                navController.navigate(
                                    "profile/$code/${accountId}"
                                )
                            else
                                navController.navigate("login/${it.arguments?.getString("server")}")
                        },
                        goToMentions = {
                            navController.navigate("mentions/$code")
                        },
                        goToNotifications = {
                            navController.navigate("notifications/$code")
                        }, goToSearch = {
                            navController.navigate("search/$code")
                        },
                        goToConversation = { status: UI ->
                            navController.navigate("conversation/$code/${status.remoteId}/${status.type.type}")
                        },
                        goToProfile =
                        { accountId: String ->
                            navController.navigate("profile/$code/${accountId}")
                        },
                        goToTag = { tag: String ->
                            navController.navigate("tag/$code/${tag}")
                        },
                    )
                }
            }
            composable(
                route = "mentions/{code}",
            ) {
                it.arguments?.getString("code")?.let { code ->
                    AuthScoped(code, it) { userComponent, _ ->
                        MentionsScreen(
                            navController = navController,
                            code = code,
                            goToConversation = { status ->
                                navController.navigate("conversation/$code/${status.remoteId}/${status.type.type}")
                            },
                            showBackBar = true,
                            goToProfile = { accountId: String ->
                                navController.navigate("profile/$code/${accountId}")
                            },
                            goToTag = { tag: String ->
                                navController.navigate("tag/$code/${tag}")
                            },
                        )
                    }
                }
            }
            composable(
                route = "tag/{code}/{tag}",
            ) {
                it.arguments?.getString("code")?.let { code ->
                    AuthScoped(code, it) { userComponent, _ ->
                        TagScreen(
                            navController,
                            code = code,
                            tag = it.arguments?.getString("tag")!!,
                            goToConversation = { status ->
                                navController.navigate("conversation/$code/${status.remoteId}/${status.type.type}")
                            },
                            showBackBar = true,
                            goToProfile = { accountId: String ->
                                navController.navigate("profile/$code/${accountId}")
                            },
                            goToTag = { tag: String ->
                                navController.navigate("tag/$code/${tag}")
                            },
                        )
                    }
                }
            }
        }


        composable(
            route = "profile/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            it.arguments?.getString("code")?.let { code ->
                AuthScoped(code, it) { userComponent, component ->
                    ProfileScreen(
                        component = component,
                        navController = navController,
                        code = code,
                        accountId = accountId,
                        goToFollowers = {
                            navController.navigate("followers/$code/$accountId")
                        },
                        goToFollowing = {
                            navController.navigate("following/$code/$accountId")
                        },
                    )
                }
            }

        }

        composable(
            route = "following/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            it.arguments?.getString("code")?.let { code ->
                AuthScoped(code, it) { _, component ->
                    val followerPresenter = component.followerPresenter()

                    LaunchedEffect(key1 = accountId) {
                        followerPresenter.start()
                    }
                    LaunchedEffect(key1 = accountId) {
                        followerPresenter.handle(FollowerPresenter.Load(accountId, true))
                    }

                    followerPresenter.model.accounts?.let { account ->
                        FollowerScreen(account, navController = navController, code)
                    }

                }
            }

        }

        composable(
            route = "followers/{code}/{accountId}",
        ) {
            val accountId = it.arguments?.getString("accountId")!!
            it.arguments?.getString("code")?.let { code ->
                AuthScoped(code, it) { _, component ->
                    val followerPresenter = component.followerPresenter()

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

                    followerPresenter.model.accounts?.let { account ->
                        FollowerScreen(account, navController = navController, code)
                    }
                }
            }

        }

        composable(
            route = "search/{code}",
        ) {
            it.arguments?.getString("code")?.let { code ->
                AuthScoped(code, it) { userComponent, component: AuthRequiredInjector ->
                    val searchPresenter = component.searchPresenter()
                    val searchScope = rememberCoroutineScope()
                    LaunchedEffect(key1 = code) {
                        searchPresenter.start(searchScope)
                    }
                    val colorScheme = MaterialTheme.colorScheme
                    LaunchedEffect(key1 = code) {
                        searchPresenter.handle(SearchPresenter.Init(colorScheme))
                    }
                    val uriPresenter = remember { component.urlPresenter().get() }
                    LaunchedEffect(key1 = code) {
                        uriPresenter.start()
                    }
                    OpenHandledUri(uriPresenter, navController, code)

                    SearchScreen(
                        searchPresenter.model,
                        navController = navController,
                        uriPresenter = uriPresenter,
                        onQueryChange = { searchTerm: String ->
                            searchPresenter.onQueryTextChange(searchTerm)
                        },
                        goToProfile = { accountId: String ->
                            navController.navigate("profile/$code/${accountId}")
                        },
                        goToTag = { tag: String ->
                            navController.navigate("tag/$code/${tag}")
                        },
                        goToConversation = { status ->
                            navController.navigate("conversation/$code/${status.remoteId}/${status.type.type}")
                        },
                    )
                }
            }

        }

        composable(
            route = "conversation/{code}/{statusId}/{type}",
        ) {
            it.arguments?.getString("code")?.let { code ->
                val statusId = it.arguments?.getString("statusId")!!
                val type = it.arguments?.getString("type")!!
                AuthScoped(code, it) { userComponent, _ ->
                    ConversationScreen(
                        navController = navController,
                        code = code,
                        statusId = statusId,
                        type = type,
                        goToConversation = { status ->
                            if (statusId != status.remoteId) {
                                navController.navigate("conversation/$code/${status.remoteId}/${status.type.type}")
                            }
                        },
                        goToProfile = { accountId ->
                            navController.navigate("profile/$code/${accountId}")
                        },
                        goToTag = { tag ->
                            navController.navigate("tag/$code/${tag}")
                        },
                    )
                }
            }
        }
        composable(
            route = "notifications/{code}",
        ) {
            it.arguments?.getString("code")?.let { code ->
                AuthScoped(code, it) { userComponent, _ ->
                    NotificationsScreen(
                        navController = navController,
                        code = code,
                        goToConversation = { status: UI ->
                            navController.navigate("conversation/${code}/${status.remoteId}/${status.type}") {
                            }
                        },
                        goToProfile = { accountId: String ->
                            navController.navigate("profile/$code/${accountId}")
                        },
                        goToTag = { tag ->
                            navController.navigate("tag/$code/${tag}")
                        },
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
            it.users.values.forEach { user ->
                add(user.accessTokenRequest)
            }
        }
    }
}

@Composable
fun accessTokenRequest(navBackStackEntry: NavBackStackEntry) = AccessTokenRequest(
    code = navBackStackEntry.arguments?.getString("code")!!,
    clientId = navBackStackEntry.arguments?.getString("clientId")!!,
    clientSecret = navBackStackEntry.arguments?.getString("clientSecret")!!,
    redirectUri = navBackStackEntry.arguments?.getString("redirectUri")!!,
    domain = navBackStackEntry.arguments?.getString("server")!!
)

