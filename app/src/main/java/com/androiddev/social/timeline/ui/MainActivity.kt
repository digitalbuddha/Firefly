@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)

package com.androiddev.social.timeline.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.androiddev.social.AuthOptionalComponent.ParentComponent
import com.androiddev.social.AuthOptionalScope
import com.androiddev.social.EbonyApp
import com.androiddev.social.auth.data.AppTokenRepository
import com.androiddev.social.auth.ui.SignInContent
import com.androiddev.social.auth.ui.SignInPresenter
import com.androiddev.social.theme.*
import com.androiddev.social.timeline.ui.model.UI
import com.squareup.anvil.annotations.ContributesTo
import dev.marcellogalhardo.retained.compose.retain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@ContributesTo(AuthOptionalScope::class)
interface Injector {
    fun signInPresenter(): SignInPresenter
    fun avatarPresenter(): AvatarPresenter
    fun homePresenter(): HomePresenter
    fun repository(): AppTokenRepository
}

@ExperimentalTextApi
@ExperimentalMaterialApi
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(
                                alpha = .8f
                            )
                        )
                ) {
//                    androidx.compose.foundation.Image(
//                        colorFilter = ColorFilter.tint(
//                            androidx.compose.material3.MaterialTheme.colorScheme.tertiary.copy(
//                                alpha = .8f
//                            )
//                        ),
//                        painter = painterResource(id = R.drawable.rocket2),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .padding(32.dp)
//                            .fillMaxSize(),
//                    )
                    val scope = rememberCoroutineScope()
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "selectServer") {
                        composable("timeline") {
                            TimelineScreen()

                            /*...*/
                        }

                        dialog("selectServer") {
                            var server by remember { mutableStateOf("androiddev.social") }
                            EbonyTheme {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                                    color = colorScheme.surface.copy(alpha = .8f)
                                ) {
                                    val configuration = LocalConfiguration.current

                                    val screenHeight = configuration.screenHeightDp

                                    Column(
                                        Modifier
                                            .padding(
                                                PaddingSize2
                                            )
                                            .fillMaxWidth(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .height((screenHeight * .5f).dp)
                                        ,
                                        verticalArrangement = Arrangement.SpaceBetween


                                    ) {
                                        Text(
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(
                                                    PaddingSize1
                                                ),
                                            text = "Welcome!",
                                            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
                                        )
                                        Text(
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .padding(
                                                    PaddingSize1
                                                ),
                                            text = "Which Server should we connect to?",
                                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                                        )

                                        TextField(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .wrapContentWidth(),
                                            textStyle = LocalTextStyle.current.copy(
//                                        textAlign = TextAlign.Cewn
                                            ),
                                            colors = TextFieldDefaults.textFieldColors(
                                                backgroundColor = colorScheme.onSecondaryContainer,
                                                cursorColor = Color.Black,
                                                disabledLabelColor = colorScheme.onSecondaryContainer,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                textColor = colorScheme.secondaryContainer
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            value = server,
                                            onValueChange = {
                                                server = it
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "clear text",
                                                    modifier = Modifier
                                                        .clickable {
                                                            server = ""
                                                        }
                                                )
                                            })
                                        Box(
                                            modifier = Modifier
                                                .alpha(.8f)
                                                .fillMaxWidth()

                                        ) {
                                            ExtendedFloatingActionButton(
                                                backgroundColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .fillMaxWidth(.7f)
                                                    .align(Alignment.Center),
                                                text = {
                                                    Text("Continue to Server")
                                                },
                                                onClick =
                                                {
                                                    scope.launch {
                                                        navController.navigate("login/$server")
                                                    }
                                                })
                                        }
                                    }

                                }
                            }
                        }
                        composable("login/{server}") {
                            val server = it.arguments?.getString("server")!!
                            SignedInScreen(navController, scope, server)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
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
        var skipHalfExpanded by remember { mutableStateOf(true) }
        val state = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
        )
        Scaffold(
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.height(PaddingSize8),
                    contentPadding = PaddingValues(PaddingSizeNone, PaddingSizeNone),
                    elevation = BottomBarElevation,
                    //                            cutoutShape = CutCornerShape(50),
                    backgroundColor = colorScheme.surface.copy(alpha = .5f),
                ) {
                    BottomBar()
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            floatingActionButton = {
                val scope = rememberCoroutineScope()
                if (!state.isVisible) {
                    FAB(colorScheme) {
                        scope.launch {
                            state.show()
                        }
                    }
                }
            }
        ) { padding ->
            Box {
                ModalBottomSheetLayout(
                    sheetElevation = PaddingSize2,
                    sheetState = state,
                    sheetContent = {
                        UserInput(onMessageSent = {}, modifier = Modifier.padding(bottom = 20.dp))
                    }) {
                    timelineScreen(homePresenter.events, homePresenter.model.statuses)

                }
                TopAppBar(
                    modifier =Modifier.height(60.dp),
                    backgroundColor = colorScheme.surface.copy(
                        alpha = .9f
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
            }
        }
    }

    @Composable
    private fun SignedInScreen(
        navController: NavHostController,
        scope: CoroutineScope,
        server: String
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
        LaunchedEffect("signIn") {
            signInPresenter.start()
        }
        LaunchedEffect("signIn") {
            val userToken = component.repository().getUserToken(null)
            if (userToken != null) {
                navController.navigate("timeline") {
                    popUpTo(0)
                }
            } else {
                signInPresenter.handle(SignInPresenter.SetServer(server))
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
        events: MutableSharedFlow<HomePresenter.HomeEvent>,
        statuses: Flow<PagingData<UI>>?,
    ) {
        LaunchedEffect(key1 = HomePresenter.LoadSomething) {
            events.tryEmit(HomePresenter.LoadSomething)
        }
        val items = statuses?.collectAsLazyPagingItems()
        val refreshing = items?.loadState?.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(refreshing, {
            items?.refresh()
        })


        Box(Modifier.pullRefresh(pullRefreshState).padding(top=60.dp)) {
            statuses?.let {
                TimelineScreen(
                    items!!
                )
            }
            CustomViewPullRefreshView(pullRefreshState, refreshTriggerDistance= 4.dp ,isRefreshing = refreshing)
//            PullRefreshIndicator(
//                refreshing,
//                pullRefreshState,
//                Modifier.align(Alignment.TopCenter)
//            )
        }
    }


    fun noAuthComponent() =
        ((applicationContext as EbonyApp).component as ParentComponent).createAuthOptionalComponent()
}
