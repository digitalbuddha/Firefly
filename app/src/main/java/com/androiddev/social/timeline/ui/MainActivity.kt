@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.FabPosition
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.AuthOptionalComponent.ParentComponent
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.ui.SignInContent
import com.androiddev.social.auth.ui.SignInPresenter
import com.androiddev.social.timeline.ui.model.UI
import com.androiddev.social.timeline.ui.theme.EbonyTheme
import com.squareup.anvil.annotations.ContributesTo
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow


@OptIn(ExperimentalMaterial3Api::class)
@ContributesTo(AuthOptionalScope::class)
interface Injector {
    fun signInPresenter(): SignInPresenter
    fun avatarPresenter(): AvatarPresenter
    fun homePresenter(): HomePresenter
}

@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EbonyTheme {
                val scope = rememberCoroutineScope()
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("timeline") {
                        TimelineScreen()

                        /*...*/
                    }
                    composable("login") {
                        SignedInScreen(navController, scope)
                    }
                }
            }
        }
    }

    @Composable
    private fun TimelineScreen() {
        val component: Injector = retain { noAuthComponent() } as Injector
        val homePresenter = component.homePresenter()
        val avatarPresenter = component.avatarPresenter()
        LaunchedEffect(key1 = "start") {
            homePresenter.start()
        }
        LaunchedEffect(key1 = "start") {
            avatarPresenter.start()
        }
        androidx.compose.material.Scaffold(
            bottomBar = {
                androidx.compose.material.BottomAppBar(
                    modifier = Modifier.height(60.dp),
                    contentPadding = PaddingValues(0.dp, 0.dp),
                    elevation = 0.dp,
                    //                            cutoutShape = CutCornerShape(50),
                    backgroundColor = colorScheme.surface.copy(alpha = .9f),
                ) {
                    BottomBar()
                }
            },
            topBar = {
                SmallTopAppBar(
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = colorScheme.surface.copy(
                            alpha = .9f
                        )
                    ),
                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LaunchedEffect(key1 = "avatar") {
                                avatarPresenter.start()
                            }
                            LaunchedEffect(key1 = "avatar") {
                                avatarPresenter.events.tryEmit(AvatarPresenter.Load)
                            }

                            Box {
                                Profile(
                                    account = avatarPresenter.model.account
                                )
                            }
                            Box(Modifier.align(Alignment.CenterVertically)) { TabSelector() }
                            NotifIcon()
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            floatingActionButton = {
                FAB(colorScheme)
            },
            content = { padding ->
                timelineScreen(padding, homePresenter.events, homePresenter.model.statuses)
            }
        )
    }

    @Composable
    private fun SignedInScreen(
        navController: NavHostController,
        scope: CoroutineScope
    ) {
        val component: Injector = retain { noAuthComponent() } as Injector
        val signInPresenter = component.signInPresenter()
        val signedIn = signInPresenter.model.signedIn
        if (signedIn)
            LaunchedEffect("signIn") {
                navController.navigate("timeline") {
                    popUpTo(0)
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
                signInPresenter.shouldCancelLoadingUrl(it, scope)
            }

        )
    }

    @Composable
    private fun timelineScreen(
        padding: PaddingValues,
        events: MutableSharedFlow<HomePresenter.HomeEvent>,
        statuses: Flow<PagingData<UI>>?
    ) {
        LaunchedEffect(key1 = HomePresenter.LoadSomething) {
            events.tryEmit(HomePresenter.LoadSomething)
        }
        Column(
            Modifier
                .padding(paddingValues = padding)
                .fillMaxSize()
        ) {
           statuses?.let {
                TimelineScreen(
                    it.collectAsLazyPagingItems()
                )
            }
        }
    }

    fun noAuthComponent() =
        ((applicationContext as EbonyApp).component as ParentComponent).createAuthOptionalComponent()
}




